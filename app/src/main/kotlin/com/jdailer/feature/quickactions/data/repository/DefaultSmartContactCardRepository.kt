package com.jdailer.feature.quickactions.data.repository

import android.content.Context
import com.jdailer.core.database.dao.ContactTagDao
import com.jdailer.core.database.dao.ContactNoteDao
import com.jdailer.core.database.entity.ContactNoteEntity
import com.jdailer.core.database.entity.ContactTagEntity
import android.content.ContentResolver
import android.provider.ContactsContract
import com.jdailer.feature.contacts.domain.model.UnifiedContact
import com.jdailer.feature.integrations.base.AdapterAction
import com.jdailer.feature.integrations.base.CommunicationAdapterRegistry
import com.jdailer.feature.integrations.base.CommunicationTarget
import com.jdailer.feature.quickactions.domain.model.SmartAction
import com.jdailer.feature.quickactions.domain.model.SmartActionType
import com.jdailer.feature.quickactions.domain.repository.SmartContactCardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultSmartContactCardRepository(
    private val context: Context,
    private val adapterRegistry: CommunicationAdapterRegistry,
    private val tagDao: ContactTagDao,
    private val noteDao: ContactNoteDao
) : SmartContactCardRepository {
    override suspend fun resolveActions(contact: UnifiedContact): List<SmartAction> {
        val actions = mutableListOf<SmartAction>()
        val number = contact.numbers.firstOrNull()
        val target = CommunicationTarget(
            contactId = contact.contactId,
            contactName = contact.displayName,
            phoneNumber = number
        )

        if (number != null) {
            actions.add(
                SmartAction(
                    type = SmartActionType.CALL,
                    actionId = "CALL",
                    label = "Call",
                    sortOrder = 0
                )
            )
            if (adapterRegistry.findAdapterFor(context, target, AdapterAction.MESSAGE) != null) {
                actions.add(
                    SmartAction(
                        type = SmartActionType.MESSAGE,
                        actionId = "MESSAGE",
                        label = "Message",
                        sortOrder = 1
                    )
                )
            }
            if (adapterRegistry.findAdapterFor(context, target, AdapterAction.CALL) != null) {
                actions.add(
                    SmartAction(
                        type = SmartActionType.APP,
                        actionId = "APP",
                        label = "App",
                        sortOrder = 2
                    )
                )
            }
            if (resolvePrimaryEmail(contact.contactId) != null) {
                actions.add(
                    SmartAction(
                        type = SmartActionType.EMAIL,
                        actionId = "EMAIL",
                        label = "Email",
                        sortOrder = 3
                    )
                )
            }
            actions.add(
                SmartAction(
                    type = SmartActionType.BLOCK,
                    actionId = "BLOCK",
                    label = "Block",
                    sortOrder = 4
                )
            )
        }

        if (!contact.notes.isNullOrBlank()) {
            actions.add(
                SmartAction(
                    type = SmartActionType.NOTE,
                    actionId = "VIEW_NOTE",
                    label = "View note",
                    sortOrder = 5
                )
            )
        }

        actions.add(
            SmartAction(
                type = SmartActionType.SHARE,
                actionId = "SHARE",
                label = "Share",
                sortOrder = 6
            )
        )

        return actions.sortedBy { it.sortOrder }
    }

    override suspend fun resolveTags(contactId: Long): List<String> {
        return tagDao.observeByContact(contactId).map { it.tag }.distinct().sorted()
    }

    override suspend fun resolveLatestNote(contactId: Long): String? {
        return noteDao.observeByContact(contactId).firstOrNull()?.note
    }

    override suspend fun saveTag(contactId: Long, tag: String) {
        tagDao.upsert(
            ContactTagEntity(
                contactId = contactId,
                tag = tag.trim(),
                value = null
            )
        )
    }

    override suspend fun saveNote(contactId: Long, note: String) {
        noteDao.upsert(
            ContactNoteEntity(
                contactId = contactId,
                note = note,
                createdByUser = true
            )
        )
    }

    private suspend fun resolvePrimaryEmail(contactId: Long): String? = withContext(Dispatchers.IO) {
        if (contactId <= 0) return@withContext null

        val uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)
        val selection = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?"
        val args = arrayOf(contactId.toString())
        var result: String? = null
        val contentResolver: ContentResolver = context.contentResolver

        contentResolver.query(uri, projection, selection, args, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                if (idx >= 0) {
                    result = cursor.getString(idx).orEmpty().trim()
                }
            }
        }
        result?.takeIf { it.isNotBlank() }
    }
}
