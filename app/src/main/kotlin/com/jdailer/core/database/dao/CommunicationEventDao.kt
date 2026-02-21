package com.jdailer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jdailer.core.database.entity.CommunicationEventEntity
import com.jdailer.core.database.enums.HistorySource
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunicationEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CommunicationEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CommunicationEventEntity>)

    @Query(
        """
        SELECT * FROM communication_events
        WHERE :isAllSources = 1 OR source IN (:sources)
        ORDER BY timestamp DESC
        """
    )
    fun observeHistory(sources: List<HistorySource>, isAllSources: Boolean): Flow<List<CommunicationEventEntity>>

    @Query(
        """
        SELECT * FROM communication_events
        WHERE :isAllSources = 1 OR source IN (:sources)
        ORDER BY timestamp DESC
        """
    )
    fun observeHistoryPaged(
        sources: List<HistorySource>,
        isAllSources: Boolean
    ): PagingSource<Int, CommunicationEventEntity>
}
