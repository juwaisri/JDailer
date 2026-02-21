package com.jdailer.feature.messages.domain.repository

import androidx.paging.PagingData
import com.jdailer.core.database.enums.HistorySource
import com.jdailer.feature.messages.domain.model.UnifiedMessageItem
import com.jdailer.feature.messages.domain.model.UnifiedMessageThread
import com.jdailer.feature.messages.domain.model.SmsMmsSyncResult
import kotlinx.coroutines.flow.Flow

interface UnifiedMessageRepository {
    fun observeThreads(
        query: String?,
        sources: Set<HistorySource>,
        pageSize: Int
    ): Flow<PagingData<UnifiedMessageThread>>

    fun observeThreadItems(
        threadId: String,
        pageSize: Int
    ): Flow<PagingData<UnifiedMessageItem>>

    suspend fun syncFromDevice(limitPerType: Int = 250): Result<SmsMmsSyncResult>

    suspend fun sendSms(to: String, text: String, threadId: String? = null): Result<String>
    suspend fun sendMms(
        to: String,
        text: String?,
        attachmentUris: List<String>
    ): Result<String>
}
