package com.jdailer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.jdailer.core.database.entity.ContactTagEntity

@Dao
interface ContactTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tag: ContactTagEntity)

    @Query("SELECT * FROM contact_tags WHERE contactId = :contactId ORDER BY createdAt DESC")
    suspend fun observeByContact(contactId: Long): List<ContactTagEntity>

    @Query("DELETE FROM contact_tags WHERE contactId = :contactId")
    suspend fun deleteAllForContact(contactId: Long)

    @Delete
    suspend fun delete(tag: ContactTagEntity)
}
