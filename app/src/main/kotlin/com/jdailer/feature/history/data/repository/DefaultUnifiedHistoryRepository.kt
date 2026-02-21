package com.jdailer.feature.history.data.repository

import com.jdailer.core.common.coroutines.DispatcherProvider
import com.jdailer.core.database.dao.CommunicationEventDao
import com.jdailer.core.database.entity.CommunicationEventEntity
import com.jdailer.core.database.enums.HistorySource
import com.jdailer.feature.history.domain.model.UnifiedHistoryItem
import com.jdailer.feature.history.domain.repository.UnifiedHistoryRepository
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class DefaultUnifiedHistoryRepository(
    private val communicationEventDao: CommunicationEventDao,
    private val dispatchers: DispatcherProvider
) : UnifiedHistoryRepository {
    override fun observeHistory(
        sources: Set<HistorySource>,
        limit: Int
    ): Flow<PagingData<UnifiedHistoryItem>> {
        val isAll = sources.isEmpty()
        val sourceList = sources.toList()
        return Pager(
            config = PagingConfig(
                pageSize = limit.coerceAtLeast(20),
                initialLoadSize = limit.coerceAtLeast(20),
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                communicationEventDao.observeHistoryPaged(sourceList, isAll)
            }
        ).flow
            .map { pagingData -> pagingData.map { it.toUnifiedHistoryItem() } }
            .flowOn(dispatchers.io)
    }
}

private fun CommunicationEventEntity.toUnifiedHistoryItem(): UnifiedHistoryItem {
    return UnifiedHistoryItem(
        id = this.id,
        contactId = this.contactId,
        threadId = this.threadId,
        source = this.source,
        direction = this.direction,
        timestamp = this.timestamp,
        snippet = this.snippet,
        unreadCount = this.unreadCount,
        isRead = this.read
    )
}

