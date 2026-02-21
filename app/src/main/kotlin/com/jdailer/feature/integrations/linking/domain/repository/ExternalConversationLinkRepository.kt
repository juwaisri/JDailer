package com.jdailer.feature.integrations.linking.domain.repository

import com.jdailer.core.database.entity.ExternalConversationLinkEntity

interface ExternalConversationLinkRepository {
    suspend fun getLink(contactId: Long, platform: String): ExternalConversationLinkEntity?
    suspend fun upsertLink(link: ExternalConversationLinkEntity)
    suspend fun clearLink(contactId: Long, platform: String)
    suspend fun clearAllForContact(contactId: Long)
    suspend fun linksForContact(contactId: Long): List<ExternalConversationLinkEntity>
}
