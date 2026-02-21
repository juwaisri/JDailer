package com.jdailer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jdailer.core.database.entity.ContactEntity
import com.jdailer.core.database.entity.ContactSuggestionRow
import com.jdailer.core.database.entity.PhoneNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhoneNumbers(numbers: List<PhoneNumberEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE contactId = :contactId")
    suspend fun deleteContact(contactId: Long)

    @Query("DELETE FROM phone_numbers WHERE contactId = :contactId")
    suspend fun deletePhoneNumbers(contactId: Long)

    @Transaction
    suspend fun upsertContactWithPhones(contact: ContactEntity, numbers: List<PhoneNumberEntity>) {
        upsertContact(contact)
        upsertPhoneNumbers(numbers)
    }

    @Query(
        """
        SELECT
            c.contactId AS contactId,
            c.displayName AS displayName,
            c.normalizedName AS normalizedName,
            c.isFavorite AS isFavorite,
            p.number AS phoneNumber,
            p.normalizedNumber AS normalizedNumber,
            p.t9Digits AS t9Digits
        FROM contacts c
        INNER JOIN phone_numbers p ON p.contactId = c.contactId
        WHERE c.isDeleted = 0
          AND (
            c.normalizedName LIKE :query
            OR c.displayName LIKE :query
            OR p.number LIKE :query
            OR p.t9Digits LIKE :query
            OR p.normalizedNumber LIKE :query
          )
        ORDER BY c.isFavorite DESC, c.displayName ASC
        LIMIT :limit
        """
    )
    fun observeSuggestions(query: String, limit: Int): Flow<List<ContactSuggestionRow>>

    @Query("SELECT * FROM contacts WHERE isDeleted = 0 ORDER BY updatedAt DESC LIMIT :limit")
    fun observeRecentContacts(limit: Int): Flow<List<ContactEntity>>

    @Query(
        """
        SELECT
            c.contactId AS contactId,
            c.displayName AS displayName,
            c.normalizedName AS normalizedName,
            c.isFavorite AS isFavorite,
            p.number AS phoneNumber,
            p.normalizedNumber AS normalizedNumber,
            p.t9Digits AS t9Digits
        FROM contacts c
        INNER JOIN phone_numbers p ON p.contactId = c.contactId
        WHERE c.isDeleted = 0
        GROUP BY c.contactId, p.number
        ORDER BY c.updatedAt DESC, c.isFavorite DESC
        LIMIT :limit
        """
    )
    fun observeRecentSuggestions(limit: Int): Flow<List<ContactSuggestionRow>>

    @Query("SELECT * FROM phone_numbers WHERE contactId = :contactId")
    fun observeNumbers(contactId: Long): Flow<List<PhoneNumberEntity>>

    @Query(
        """
        UPDATE contacts
        SET isDeleted = 1
        WHERE source = :source
          AND isDeleted = 0
          AND contactId NOT IN (:keptContactIds)
        """
    )
    suspend fun markSourceContactsDeletedExcept(source: String, keptContactIds: List<Long>)

    @Query(
        """
        UPDATE contacts
        SET isDeleted = 1
        WHERE source = :source
          AND isDeleted = 0
        """
    )
    suspend fun markAllSourceContactsDeleted(source: String)

    @Query(
        """
        SELECT c.contactId
        FROM contacts c
        INNER JOIN phone_numbers p ON p.contactId = c.contactId
        WHERE c.isDeleted = 0
          AND p.normalizedNumber = :normalizedNumber
          LIMIT 1
        """
    )
    suspend fun findContactIdByNormalizedNumber(normalizedNumber: String): Long?

    @Query("UPDATE contacts SET notes = :notes WHERE contactId = :contactId")
    suspend fun updateNotes(contactId: Long, notes: String?)
}
