package com.jdailer.feature.contacts.domain.usecase

import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResolveContactEmailsUseCase(
    private val context: Context
) {
    suspend operator fun invoke(contactId: Long): List<String> = withContext(Dispatchers.IO) {
        if (contactId <= 0) return@withContext emptyList()

        val uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)
        val selection = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?"
        val args = arrayOf(contactId.toString())
        val emails = ArrayList<String>()

        context.contentResolver.query(uri, projection, selection, args, null)?.use { cursor ->
            val addressIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            while (cursor.moveToNext()) {
                val address = cursor.getString(addressIndex).orEmpty().trim()
                if (address.isNotBlank()) {
                    emails.add(address)
                }
            }
        }

        emails.distinct()
    }
}
