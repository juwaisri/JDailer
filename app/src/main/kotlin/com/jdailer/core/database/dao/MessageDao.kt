package com.jdailer.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jdailer.core.database.entity.MessageItemEntity
import com.jdailer.core.database.entity.MessageThreadEntity
import com.jdailer.core.database.enums.HistorySource

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThread(thread: MessageThreadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThreads(threads: List<MessageThreadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: MessageItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<MessageItemEntity>)

    @Query(
        """
        SELECT * FROM message_threads
        WHERE (:isAllSources = 1 OR source IN (:sources))
          AND (:q IS NULL OR title LIKE :q OR normalizedAddress LIKE :q)
        ORDER BY isPinned DESC, lastMessageAt DESC
        """
    )
    fun observeThreads(sources: List<HistorySource>, isAllSources: Boolean, q: String?): PagingSource<Int, MessageThreadEntity>

    @Query("SELECT * FROM message_items WHERE threadId = :threadId ORDER BY timestamp DESC")
    fun observeThreadItems(threadId: String): PagingSource<Int, MessageItemEntity>

    @Query("UPDATE message_threads SET unreadCount = 0 WHERE threadId = :threadId")
    suspend fun clearUnreadCount(threadId: String)

    @Query("UPDATE message_items SET isRead = 1 WHERE threadId = :threadId")
    suspend fun markItemsRead(threadId: String)

    @Query("DELETE FROM message_items WHERE threadId = :threadId")
    suspend fun deleteThreadItems(threadId: String)

    @Query("DELETE FROM message_threads WHERE threadId = :threadId")
    suspend fun deleteThread(threadId: String)
}
