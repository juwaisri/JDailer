package com.jdailer.feature.quickactions.domain.repository

import com.jdailer.feature.contacts.domain.model.UnifiedContact
import com.jdailer.feature.quickactions.domain.model.SmartAction

interface SmartContactCardRepository {
    suspend fun resolveActions(contact: UnifiedContact): List<SmartAction>
    suspend fun resolveTags(contactId: Long): List<String>
    suspend fun resolveLatestNote(contactId: Long): String?
    suspend fun saveTag(contactId: Long, tag: String)
    suspend fun saveNote(contactId: Long, note: String)
}
