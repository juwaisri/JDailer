package com.jdailer.feature.contacts.data

import android.content.Context
import android.provider.ContactsContract
import com.jdailer.core.common.coroutines.DispatcherProvider
import com.jdailer.core.common.result.AppResult
import com.jdailer.core.database.dao.ContactDao
import com.jdailer.core.database.entity.ContactEntity
import com.jdailer.core.database.entity.ContactSuggestionRow
import com.jdailer.core.database.entity.PhoneNumberEntity
import com.jdailer.feature.contacts.domain.model.UnifiedContact
import com.jdailer.feature.contacts.domain.repository.ContactsRepository
import com.jdailer.feature.dialer.data.T9Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.text.Regex

private const val GOOGLE_ACCOUNT_TYPE = "com.google"
private const val GOOGLE_SOURCE = "google"

class ContactsSyncRepository(
    private val context: Context,
    private val contactDao: ContactDao,
    private val dispatchers: DispatcherProvider
) : ContactsRepository {
    override fun observeContacts(query: String, limit: Int): Flow<List<UnifiedContact>> {
        val queryPattern = if (query.isBlank()) "%%" else "%${T9Query.normalizeName(query)}%"
        return contactDao.observeSuggestions(queryPattern, limit).map { rows ->
            rows
                .distinctBy { it.contactId }
                .map { row -> row.toUnifiedContact() }
        }.flowOn(dispatchers.io)
    }

    override suspend fun syncFromDevice(): AppResult<Unit> = withContext(dispatchers.io) {
        return@withContext try {
            val resolver = context.contentResolver
            val now = System.currentTimeMillis()
            val startTimeMs = now
            val googleContactIds = readGoogleContactIds(resolver)
            val syncedBySource = mutableMapOf(
                GOOGLE_SOURCE to mutableSetOf<Long>(),
                "local" to mutableSetOf<Long>()
            )
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.STARRED
            )
            resolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                "${ContactsContract.Contacts.HAS_PHONE_NUMBER} = 1",
                null,
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
            )?.use { contactsCursor ->
                val idIndex = contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                val displayNameIndex = contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val starredIndex = contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts.STARRED)

                while (contactsCursor.moveToNext()) {
                    val contactId = contactsCursor.getLong(idIndex)
                    val displayName = contactsCursor.getString(displayNameIndex).orEmpty().ifBlank { "Unknown" }
                    val isFavorite = contactsCursor.getInt(starredIndex) == 1
                    val source = if (googleContactIds.contains(contactId)) GOOGLE_SOURCE else "local"
                    syncedBySource.getOrPut(source) { mutableSetOf() }.add(contactId)

                    val numbers = readPhoneNumbers(
                        resolver = resolver,
                        contactId = contactId
                    )
                    if (numbers.isEmpty()) continue
                    val contactEntity = ContactEntity(
                        contactId = contactId,
                        displayName = displayName,
                        normalizedName = normalizeName(displayName),
                        isFavorite = isFavorite,
                        source = source,
                        isDeleted = false,
                        createdAt = now,
                        updatedAt = now
                    )
                    val phoneEntities = numbers.mapIndexed { index, number ->
                        PhoneNumberEntity(
                            normalizedNumber = normalizePhone(number),
                            contactId = contactId,
                            number = number,
                            label = "mobile",
                            t9Digits = T9Query.toDigits(number),
                            isPrimary = index == 0,
                            createdAt = now,
                            updatedAt = now
                        )
                    }
                    if (phoneEntities.isNotEmpty()) {
                        contactDao.deletePhoneNumbers(contactId)
                        contactDao.upsertContactWithPhones(contactEntity, phoneEntities)
                    }
                }
            }

            syncBySource(
                GOOGLE_SOURCE,
                syncedBySource[GOOGLE_SOURCE]?.toList().orEmpty()
            )
            syncBySource("local", syncedBySource["local"]?.toList().orEmpty())
            val durationMs = System.currentTimeMillis() - startTimeMs
            Timber.d("Synced contacts in $durationMs ms")
            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(throwable = exception)
        }
    }

    override suspend fun upsertContact(contact: UnifiedContact): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            if (contact.displayName.isBlank()) {
                return@withContext AppResult.Error(
                    throwable = IllegalArgumentException("Contact name cannot be empty")
                )
            }
            val contactId = if (contact.contactId > 0) contact.contactId else System.currentTimeMillis()
            val now = System.currentTimeMillis()
            val contactEntity = ContactEntity(
                contactId = contactId,
                displayName = contact.displayName,
                normalizedName = normalizeName(contact.displayName),
                notes = contact.notes,
                isFavorite = contact.tags.contains("favorite"),
                source = "local",
                isDeleted = false,
                createdAt = now,
                updatedAt = now
            )
            val phoneEntities = contact.numbers.mapIndexed { index, rawNumber ->
                PhoneNumberEntity(
                    normalizedNumber = normalizePhone(rawNumber),
                    contactId = contactId,
                    number = rawNumber,
                    isPrimary = index == 0,
                    t9Digits = T9Query.toDigits(rawNumber),
                    createdAt = now,
                    updatedAt = now
                )
            }
            contactDao.deletePhoneNumbers(contactId)
            contactDao.upsertContactWithPhones(contactEntity, phoneEntities)
            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(throwable = exception)
        }
    }

    private suspend fun syncBySource(source: String, keptIds: List<Long>) {
        if (keptIds.isNotEmpty()) {
            contactDao.markSourceContactsDeletedExcept(source, keptIds)
        } else {
            contactDao.markAllSourceContactsDeleted(source)
        }
    }

    private fun readGoogleContactIds(resolver: android.content.ContentResolver): Set<Long> {
        val projection = arrayOf(
            ContactsContract.RawContacts.CONTACT_ID
        )
        val selection = "${ContactsContract.RawContacts.DELETED} = 0 AND ${ContactsContract.RawContacts.ACCOUNT_TYPE} = ?"
        val selectionArgs = arrayOf(GOOGLE_ACCOUNT_TYPE)
        val sourceIds = HashSet<Long>()
        resolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val contactId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.CONTACT_ID))
                if (contactId > 0) sourceIds.add(contactId)
            }
        }
        return sourceIds
    }

    private fun readPhoneNumbers(
        resolver: android.content.ContentResolver,
        contactId: Long
    ): List<String> {
        val numbers = mutableListOf<String>()
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            arrayOf(contactId.toString()),
            null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val raw = cursor.getString(numberIndex).orEmpty().trim()
                val normalized = normalizePhone(raw)
                if (raw.isNotBlank() && normalized.isNotBlank()) {
                    numbers.add(raw)
                }
            }
        }
        return numbers.distinct()
    }

    private fun normalizeName(value: String): String =
        value.trim().lowercase().replace(Regex("[^a-z0-9]+"), "")

    private fun normalizePhone(value: String): String {
        val digits = value.filter { it.isDigit() || it == '+' }
        return if (digits.startsWith("+")) digits else digits
    }
}

private fun ContactSuggestionRow.toUnifiedContact() = UnifiedContact(
    contactId = contactId,
    displayName = displayName,
    numbers = listOf(phoneNumber),
    notes = null,
    tags = if (isFavorite) listOf("favorite") else emptyList()
)
