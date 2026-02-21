package com.jdailer.feature.contacts.domain.repository

import com.jdailer.core.common.result.AppResult
import com.jdailer.feature.contacts.domain.model.UnifiedContact
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
    fun observeContacts(query: String, limit: Int = 100): Flow<List<UnifiedContact>>
    suspend fun syncFromDevice(): AppResult<Unit>
    suspend fun upsertContact(contact: UnifiedContact): AppResult<Unit>
}
