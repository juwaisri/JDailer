package com.jdailer.core.database.entity

data class ContactSuggestionRow(
    val contactId: Long,
    val displayName: String,
    val normalizedName: String,
    val isFavorite: Boolean,
    val phoneNumber: String,
    val normalizedNumber: String,
    val t9Digits: String,
)
