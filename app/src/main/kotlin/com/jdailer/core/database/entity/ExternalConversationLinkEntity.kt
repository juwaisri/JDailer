package com.jdailer.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "external_conversation_links",
    indices = [Index("contactId"), Index("platform")]
)
data class ExternalConversationLinkEntity(
    @PrimaryKey
    val linkId: String,
    val contactId: Long,
    val platform: String,
    val handle: String,
    val resolvedBy: String,
    val isEnabled: Boolean = true,
    val isBlocked: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
