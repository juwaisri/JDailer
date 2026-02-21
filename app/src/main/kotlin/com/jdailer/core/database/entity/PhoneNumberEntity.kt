package com.jdailer.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "phone_numbers",
    primaryKeys = ["normalizedNumber"],
    indices = [
        Index("contactId"),
        Index("normalizedNumber"),
        Index("number"),
        Index("t9Digits")
    ]
)
data class PhoneNumberEntity(
    val normalizedNumber: String,
    val contactId: Long,
    val number: String,
    val label: String = "mobile",
    val t9Digits: String,
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
