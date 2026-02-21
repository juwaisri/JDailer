package com.jdailer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.jdailer.core.database.entity.ContactNoteEntity

@Dao
interface ContactNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: ContactNoteEntity)

    @Query("SELECT * FROM contact_notes WHERE contactId = :contactId ORDER BY updatedAt DESC")
    suspend fun observeByContact(contactId: Long): List<ContactNoteEntity>

    @Query("DELETE FROM contact_notes WHERE noteId = :noteId")
    suspend fun delete(noteId: Long)

    @Query("DELETE FROM contact_notes WHERE contactId = :contactId")
    suspend fun deleteByContact(contactId: Long)

    @Delete
    suspend fun delete(note: ContactNoteEntity)
}
