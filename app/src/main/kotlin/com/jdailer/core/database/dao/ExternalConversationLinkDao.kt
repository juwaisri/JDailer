package com.jdailer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jdailer.core.database.entity.ExternalConversationLinkEntity

@Dao
interface ExternalConversationLinkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(link: ExternalConversationLinkEntity)

    @Query("SELECT * FROM external_conversation_links WHERE contactId = :contactId ORDER BY platform ASC")
    suspend fun getByContact(contactId: Long): List<ExternalConversationLinkEntity>

    @Query("SELECT * FROM external_conversation_links WHERE contactId = :contactId AND platform = :platform LIMIT 1")
    suspend fun getByPlatform(contactId: Long, platform: String): ExternalConversationLinkEntity?

    @Query("DELETE FROM external_conversation_links WHERE contactId = :contactId")
    suspend fun clear(contactId: Long)

    @Query("DELETE FROM external_conversation_links WHERE contactId = :contactId AND platform = :platform")
    suspend fun clearByPlatform(contactId: Long, platform: String)
}
