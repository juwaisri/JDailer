package com.jdailer.feature.integrations.linking.data.repository

import com.jdailer.core.database.dao.ExternalConversationLinkDao
import com.jdailer.core.database.entity.ExternalConversationLinkEntity
import com.jdailer.feature.integrations.linking.domain.repository.ExternalConversationLinkRepository

class RoomExternalConversationLinkRepository(
    private val dao: ExternalConversationLinkDao
) : ExternalConversationLinkRepository {
    override suspend fun getLink(contactId: Long, platform: String): ExternalConversationLinkEntity? {
        return dao.getByPlatform(contactId, platform)
    }

    override suspend fun upsertLink(link: ExternalConversationLinkEntity) {
        dao.upsert(link)
    }

    override suspend fun clearLink(contactId: Long, platform: String) {
        dao.clearByPlatform(contactId, platform)
    }

    override suspend fun clearAllForContact(contactId: Long) {
        dao.clear(contactId)
    }

    override suspend fun linksForContact(contactId: Long): List<ExternalConversationLinkEntity> {
        return dao.getByContact(contactId)
    }
}
