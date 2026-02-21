package com.jdailer.feature.messages.data.repository

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import com.jdailer.core.common.coroutines.DispatcherProvider
import com.jdailer.core.database.dao.CommunicationEventDao
import com.jdailer.core.database.dao.ContactDao
import com.jdailer.core.database.dao.MessageDao
import com.jdailer.core.database.entity.CommunicationEventEntity
import com.jdailer.core.database.entity.MessageItemEntity
import com.jdailer.core.database.entity.MessageThreadEntity
import com.jdailer.core.database.enums.HistoryDirection
import com.jdailer.core.database.enums.HistorySource
import com.jdailer.feature.messages.domain.model.MessageThreadMetadata
import com.jdailer.feature.messages.domain.model.SmsMmsSyncResult
import com.jdailer.feature.messages.domain.model.UnifiedMessageItem
import com.jdailer.feature.messages.domain.model.UnifiedMessageThread
import com.jdailer.feature.messages.domain.repository.UnifiedMessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

private const val SMS_THREAD_PREFIX = "sms"
private const val MMS_THREAD_PREFIX = "mms"
private const val RCS_MESSAGE_PROTOCOL_HINT = "rcs"
private const val RCS_PART_PROTOCOL_KEY = "proto"
private const val MMS_MSG_BOX_COLUMN = "msg_box"
private const val MMS_PROTOCOL_TYPE_INCOMING = 137
private const val MMS_PROTOCOL_TYPE_OUTGOING = 151

private const val META_IS_RCS = "isRcs"
private const val META_IS_GROUP = "isGroupConversation"
private const val META_PARTICIPANTS = "participantCount"
private const val META_HAS_MEDIA = "hasMedia"
private const val META_ATTACHMENT_COUNT = "attachmentCount"
private const val META_ATTACHMENT_TYPE = "attachmentType"
private const val META_SECURE_CHANNEL = "secureChannel"

class DefaultUnifiedMessageRepository(
    private val context: Context,
    private val messageDao: MessageDao,
    private val communicationEventDao: CommunicationEventDao,
    private val contactDao: ContactDao,
    private val dispatchers: DispatcherProvider
) : UnifiedMessageRepository {
    private val contentResolver: ContentResolver = context.contentResolver

    override fun observeThreads(
        query: String?,
        sources: Set<HistorySource>,
        pageSize: Int
    ): Flow<PagingData<UnifiedMessageThread>> {
        val effectiveSources = sources.toList()
        val includeAllSources = effectiveSources.isEmpty()
        val safeQuery = query
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { "%${it.replace("%", "\\%")}" }

        return androidx.paging.Pager(
            config = PagingConfig(
                pageSize = pageSize.coerceAtLeast(20),
                initialLoadSize = pageSize.coerceAtLeast(20),
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                messageDao.observeThreads(
                    sources = effectiveSources,
                    isAllSources = includeAllSources,
                    q = safeQuery
                )
            }
        ).flow
            .map { pagingData -> pagingData.map { it.toDomainThread() } }
            .flowOn(dispatchers.io)
    }

    override fun observeThreadItems(
        threadId: String,
        pageSize: Int
    ): Flow<PagingData<UnifiedMessageItem>> {
        return androidx.paging.Pager(
            config = PagingConfig(pageSize = pageSize.coerceAtLeast(30), enablePlaceholders = false),
            pagingSourceFactory = { messageDao.observeThreadItems(threadId = threadId) }
        ).flow
            .map { pagingData -> pagingData.map { it.toDomainItem() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun syncFromDevice(limitPerType: Int): Result<SmsMmsSyncResult> {
        return withContext(dispatchers.io) {
            runCatching {
                requirePermission(Manifest.permission.READ_SMS)
                val smsResult = syncSmsInbox(limitPerType.coerceAtLeast(1))
                val mmsResult = syncMmsInbox(limitPerType.coerceAtLeast(1))

                SmsMmsSyncResult(
                    syncedAt = System.currentTimeMillis(),
                    syncedMessages = smsResult.items + mmsResult.items,
                    syncedThreads = smsResult.threads + mmsResult.threads,
                    sourceCounts = mapOf(
                        HistorySource.SMS to smsResult.items,
                        HistorySource.MMS to mmsResult.items
                    ),
                    hasRcsCapability = hasRcsCapability(),
                    isFullSync = true
                )
            }
        }
    }

    override suspend fun sendSms(to: String, text: String, threadId: String?): Result<String> {
        return runCatching {
            requirePermission(Manifest.permission.SEND_SMS)
            val target = normalizeNumber(to)
            if (target.isBlank()) {
                throw IllegalArgumentException("Recipient number is empty")
            }
            if (text.isBlank()) {
                throw IllegalArgumentException("Cannot send empty text")
            }

            val chunks = SmsManager.getDefault().divideMessage(text)
            val sender = if (chunks.size == 1) {
                SmsManager.getDefault().sendTextMessage(target, null, text, null, null)
                text
            } else {
                SmsManager.getDefault().sendMultipartTextMessage(target, null, chunks, null, null)
                text
            }

            val now = System.currentTimeMillis()
            val resolvedThreadId = threadId ?: buildThreadId(HistorySource.SMS, target)
            val existingContact = contactDao.findContactIdByNormalizedNumber(target)
            val metadata = MessageThreadMetadata(
                isRcs = false,
                isGroupConversation = false,
                participantCount = if (target.isNotBlank()) 2 else 0,
                hasMedia = false,
                attachmentCount = 0,
                attachmentType = null,
                secureChannel = false
            )

            val item = MessageItemEntity(
                itemId = "sms_send_${UUID.randomUUID()}",
                threadId = resolvedThreadId,
                source = HistorySource.SMS,
                direction = HistoryDirection.OUTGOING,
                contactId = existingContact,
                normalizedAddress = target,
                body = sender,
                mediaUri = null,
                timestamp = now,
                isRead = true,
                isRcs = false,
                metadata = null
            )

            messageDao.upsertItem(item)
            messageDao.upsertThread(
                MessageThreadEntity(
                    threadId = resolvedThreadId,
                    source = HistorySource.SMS,
                    contactId = existingContact,
                    normalizedAddress = target,
                    title = target,
                    snippet = sender,
                    lastMessageAt = now,
                    unreadCount = 0,
                    metadata = metadata.toStorageJson(),
                    isPinned = false,
                    isArchived = false,
                    isRcs = false
                )
            )
            communicationEventDao.insertEvent(item.toCommunicationEvent(resolvedThreadId, 0, read = true))
            sender
        }
    }

    override suspend fun sendMms(
        to: String,
        text: String?,
        attachmentUris: List<String>
    ): Result<String> {
        return runCatching {
            val target = normalizeNumber(to)
            if (target.isBlank()) {
                throw IllegalArgumentException("Recipient number is empty")
            }

            val hasAttachment = attachmentUris.isNotEmpty()
            val media = attachmentUris.map { Uri.parse(it) }
            val action = if (hasAttachment) {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = if (media.any { it.toString().contains("video", ignoreCase = true) }) {
                        "video/*"
                    } else {
                        "image/*"
                    }
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(media))
                }
            } else {
                Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$target")).apply {
                    putExtra("sms_body", text.orEmpty())
                }
            }

            action.putExtra(Intent.EXTRA_TEXT, text.orEmpty())
            action.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(Intent.createChooser(action, "Compose MMS"))

            val now = System.currentTimeMillis()
            val resolvedThreadId = buildThreadId(HistorySource.MMS, target)
            val metadata = MessageThreadMetadata(
                isRcs = false,
                isGroupConversation = false,
                participantCount = if (target.isNotBlank()) 2 else 0,
                hasMedia = hasAttachment,
                attachmentCount = media.size,
                attachmentType = if (hasAttachment) media.firstOrNull()?.toString() else null,
                secureChannel = false
            )
            val thread = MessageThreadEntity(
                threadId = resolvedThreadId,
                source = HistorySource.MMS,
                contactId = contactDao.findContactIdByNormalizedNumber(target),
                normalizedAddress = target,
                title = target,
                snippet = text.orEmpty().ifBlank { "Media attachment" },
                lastMessageAt = now,
                unreadCount = 0,
                isRcs = false,
                metadata = metadata.toStorageJson(),
                isPinned = false,
                isArchived = false
            )
            val item = MessageItemEntity(
                itemId = "mms_send_${UUID.randomUUID()}",
                threadId = resolvedThreadId,
                source = HistorySource.MMS,
                direction = HistoryDirection.OUTGOING,
                contactId = contactDao.findContactIdByNormalizedNumber(target),
                normalizedAddress = target,
                body = text,
                mediaUri = media.firstOrNull()?.toString(),
                timestamp = now,
                isRead = true,
                isRcs = false,
                metadata = media.joinToString(",")
            )
            messageDao.upsertThread(thread)
            messageDao.upsertItem(item)
            communicationEventDao.insertEvent(item.toCommunicationEvent(resolvedThreadId, 0, read = true))
            item.itemId
        }
    }

    private suspend fun syncSmsInbox(limitPerType: Int): MessageSyncBatch {
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.READ,
            Telephony.Sms.PROTOCOL
        )
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        ) ?: return MessageSyncBatch(0, 0)

        cursor.use {
            val items = mutableListOf<MessageItemEntity>()
            val threads = mutableMapOf<String, MessageThreadEntity>()
            val events = mutableListOf<CommunicationEventEntity>()

            while (it.moveToNext() && items.size < limitPerType) {
                val id = it.safeString(Telephony.Sms._ID)?.takeIf { value -> value.isNotBlank() }
                    ?: continue
                val rawAddress = it.safeString(Telephony.Sms.ADDRESS).orEmpty()
                val normalized = normalizeNumber(rawAddress)
                val threadRaw = it.safeString(Telephony.Sms.THREAD_ID).orEmpty()
                val threadId = buildThreadId(HistorySource.SMS, threadRaw.ifBlank { id })
                val direction = mapSmsDirection(it.safeLong(Telephony.Sms.TYPE).toInt())
                val isRead = it.safeLong(Telephony.Sms.READ) == 1L
                val date = it.safeLong(Telephony.Sms.DATE)
                val body = it.safeString(Telephony.Sms.BODY).orEmpty()
                val protocol = it.safeString(Telephony.Sms.PROTOCOL).orEmpty()
                val smsProtocol = it.safeString("protocol").orEmpty()
                val smsPartProtocol = it.safeString(RCS_PART_PROTOCOL_KEY).orEmpty()
                val isRcs = protocol.contains(RCS_MESSAGE_PROTOCOL_HINT, ignoreCase = true) ||
                    smsProtocol.contains(RCS_MESSAGE_PROTOCOL_HINT, ignoreCase = true) ||
                    smsPartProtocol.isNotBlank()
                val metadata = MessageThreadMetadata(
                    isRcs = isRcs,
                    isGroupConversation = false,
                    participantCount = if (normalized.isNotBlank()) 2 else 0,
                    hasMedia = false,
                    attachmentCount = 0,
                    attachmentType = null,
                    secureChannel = false
                )
                val contactId = normalized.takeIf { it.isNotBlank() }
                    ?.let { number -> contactDao.findContactIdByNormalizedNumber(number) }

                val item = MessageItemEntity(
                    itemId = "${threadId}_$id",
                    threadId = threadId,
                    source = HistorySource.SMS,
                    direction = direction,
                    contactId = contactId,
                    normalizedAddress = normalized,
                    body = body,
                    mediaUri = null,
                    timestamp = date,
                    isRead = isRead,
                    isRcs = isRcs,
                    metadata = null
                )
                val isUnread = !isRead && direction == HistoryDirection.INCOMING
                threads[threadId] = MessageThreadEntity(
                    threadId = threadId,
                    source = HistorySource.SMS,
                    contactId = contactId,
                    normalizedAddress = normalized,
                    title = normalized.ifBlank { "Unknown" },
                    snippet = body.ifBlank { "<Media>" },
                    lastMessageAt = date,
                    unreadCount = if (isUnread) 1 else 0,
                    isRcs = isRcs,
                    metadata = metadata.toStorageJson(),
                    isPinned = false,
                    isArchived = false
                )
                items.add(item)
                events.add(item.toCommunicationEvent(threadId, if (isUnread) 1 else 0, read = isRead))
            }

            if (items.isNotEmpty()) {
                messageDao.upsertItems(items)
                messageDao.upsertThreads(threads.values.toList())
                communicationEventDao.insertEvents(events)
                return MessageSyncBatch(items.size, threads.size)
            }
        }
        return MessageSyncBatch(0, 0)
    }

    private suspend fun syncMmsInbox(limitPerType: Int): MessageSyncBatch {
        val projection = arrayOf(
            Telephony.Mms._ID,
            Telephony.Mms.DATE,
            Telephony.Mms.THREAD_ID,
            MMS_MSG_BOX_COLUMN,
            Telephony.Mms.READ
        )
        val cursor = contentResolver.query(
            Telephony.Mms.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Mms.DATE} DESC"
        ) ?: return MessageSyncBatch(0, 0)

        cursor.use {
            val items = mutableListOf<MessageItemEntity>()
            val threads = mutableMapOf<String, MessageThreadEntity>()
            val events = mutableListOf<CommunicationEventEntity>()

            while (it.moveToNext() && items.size < limitPerType) {
                val id = it.safeString(Telephony.Mms._ID)?.takeIf { value -> value.isNotBlank() }
                    ?: continue
                val threadRaw = it.safeString(Telephony.Mms.THREAD_ID).orEmpty()
                val threadId = buildThreadId(HistorySource.MMS, threadRaw.ifBlank { id })
                val direction = mapMmsDirection(it.safeLong(MMS_MSG_BOX_COLUMN).toInt())
                val date = it.safeLong(Telephony.Mms.DATE)
                val isRead = it.safeLong(Telephony.Mms.READ) == 1L
                val rawAddress = resolveMmsAddress(id, direction)
                val normalized = normalizeNumber(rawAddress)
                val addressForTitle = normalized.ifBlank { "Unknown" }
                val body = resolveMmsBody(id).orEmpty()
                val contactId = normalized.takeIf { it.isNotBlank() }
                    ?.let { number -> contactDao.findContactIdByNormalizedNumber(number) }
                val mediaUris = resolveMmsMediaUris(id)
                val mediaStats = resolveMmsMediaStats(mediaUris)
                val metadata = MessageThreadMetadata(
                    isRcs = false,
                    isGroupConversation = resolveMmsParticipantCount(id) > 2,
                    participantCount = resolveMmsParticipantCount(id),
                    hasMedia = mediaStats.hasMedia,
                    attachmentCount = mediaStats.attachmentCount,
                    attachmentType = mediaStats.attachmentType,
                    secureChannel = false
                )

                val item = MessageItemEntity(
                    itemId = "${threadId}_$id",
                    threadId = threadId,
                    source = HistorySource.MMS,
                    direction = direction,
                    contactId = contactId,
                    normalizedAddress = normalized,
                    body = body,
                    mediaUri = mediaUris.firstOrNull(),
                    timestamp = date,
                    isRead = isRead || direction == HistoryDirection.OUTGOING,
                    isRcs = false,
                    metadata = mediaUris.joinToString(",")
                )
                val isIncoming = direction == HistoryDirection.INCOMING
                val isUnread = isIncoming && !isRead
                threads[threadId] = MessageThreadEntity(
                    threadId = threadId,
                    source = HistorySource.MMS,
                    contactId = contactId,
                    normalizedAddress = normalized,
                    title = addressForTitle,
                    snippet = body.ifBlank { "Multimedia message" },
                    lastMessageAt = date,
                    unreadCount = if (isUnread) 1 else 0,
                    metadata = metadata.toStorageJson(),
                    isRcs = false,
                    isPinned = false,
                    isArchived = false
                )
                items.add(item)
                events.add(item.toCommunicationEvent(threadId, if (isUnread) 1 else 0, read = !isUnread))
            }

            if (items.isNotEmpty()) {
                messageDao.upsertItems(items)
                messageDao.upsertThreads(threads.values.toList())
                communicationEventDao.insertEvents(events)
                return MessageSyncBatch(items.size, threads.size)
            }
        }
        return MessageSyncBatch(0, 0)
    }

    private fun hasRcsCapability(): Boolean {
        return runCatching {
            val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context).orEmpty().lowercase(Locale.US)
            defaultSmsPackage.isNotBlank() &&
                (defaultSmsPackage.contains("messaging") || defaultSmsPackage.contains("rcs"))
        }.getOrDefault(false)
    }

    private fun resolveMmsBody(messageId: String): String? {
        val baseUri = Uri.parse("content://mms/$messageId/part")
        val cursor = contentResolver.query(
            baseUri,
            arrayOf("_id", "ct", "text"),
            "ct='text/plain' OR ct='text'",
            null,
            "seq ASC"
        ) ?: return null
        cursor.use {
            return if (it.moveToFirst()) {
                it.safeString("text")
            } else {
                null
            }
        }
    }

    private fun resolveMmsMediaUris(messageId: String): List<String> {
        val baseUri = Uri.parse("content://mms/$messageId/part")
        val cursor = contentResolver.query(
            baseUri,
            arrayOf("_id", "ct", "_data"),
            "ct LIKE 'image/%' OR ct LIKE 'video/%' OR ct='application/octet-stream' OR ct LIKE 'audio/%'",
            null,
            "seq ASC"
        ) ?: return emptyList()

        val medias = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                val data = it.safeString("_data")
                val partId = it.safeString("_id")
                val partType = it.safeString("ct")
                val resolvedUri = when {
                    !data.isNullOrBlank() -> data
                    !partId.isNullOrBlank() -> Uri.parse("content://mms/part/$partId").toString()
                    else -> null
                }
                if (!resolvedUri.isNullOrBlank()) {
                    medias.add(resolvedUri)
                }
            }
        }
        return medias
    }

    private fun resolveMmsMediaStats(mediaUris: List<String>): MessageMediaStats {
        if (mediaUris.isEmpty()) return MessageMediaStats()
        val first = mediaUris.firstOrNull()?.lowercase(Locale.US) ?: return MessageMediaStats()
        val attachmentType = when {
            first.contains("image") -> "image"
            first.contains("video") -> "video"
            first.contains("audio") -> "audio"
            else -> "media"
        }
        return MessageMediaStats(
            hasMedia = true,
            attachmentCount = mediaUris.size,
            attachmentType = attachmentType
        )
    }

    private fun resolveMmsParticipantCount(messageId: String): Int {
        val cursor = contentResolver.query(
            Uri.parse("content://mms/$messageId/addr"),
            arrayOf("address"),
            "type IN ($MMS_PROTOCOL_TYPE_INCOMING, $MMS_PROTOCOL_TYPE_OUTGOING)",
            null,
            null
        ) ?: return 2

        cursor.use {
            val participants = hashSetOf<String>()
            while (it.moveToNext()) {
                val address = it.safeString("address").orEmpty().trim()
                if (address.isNotBlank()) {
                    participants.add(normalizeNumber(address))
                }
            }
            return participants.size.takeIf { it > 0 } ?: 2
        }
    }

    private fun resolveMmsAddress(messageId: String, direction: HistoryDirection): String {
        val type = when (direction) {
            HistoryDirection.INCOMING -> MMS_PROTOCOL_TYPE_INCOMING
            HistoryDirection.OUTGOING -> MMS_PROTOCOL_TYPE_OUTGOING
            HistoryDirection.UNKNOWN, HistoryDirection.MISSED -> MMS_PROTOCOL_TYPE_INCOMING
        }

        val cursor = contentResolver.query(
            Uri.parse("content://mms/$messageId/addr"),
            arrayOf("address"),
            "type=$type",
            null,
            null
        ) ?: return ""

        cursor.use {
            if (!it.moveToFirst()) return ""
            return it.safeString("address") ?: ""
        }
    }

    private fun requirePermission(permission: String) {
        if (ContextCompat.checkSelfPermission(context, permission) != PermissionChecker.PERMISSION_GRANTED) {
            throw SecurityException("Missing permission: $permission")
        }
    }

    private fun normalizeNumber(raw: String?): String =
        raw.orEmpty().filter { it.isDigit() || it == '+' }.ifBlank { raw.orEmpty().trim() }

    private fun buildThreadId(source: HistorySource, part: String): String {
        val normalizedPart = part.takeIf { it.isNotBlank() }?.lowercase(Locale.US) ?: "unknown"
        return when (source) {
            HistorySource.MMS -> "${MMS_THREAD_PREFIX}_$normalizedPart"
            else -> "${SMS_THREAD_PREFIX}_$normalizedPart"
        }
    }

    private fun mapSmsDirection(type: Int): HistoryDirection = when (type) {
        1 -> HistoryDirection.INCOMING
        2 -> HistoryDirection.OUTGOING
        else -> HistoryDirection.UNKNOWN
    }

    private fun mapMmsDirection(msgBox: Int): HistoryDirection = when (msgBox) {
        137, 1 -> HistoryDirection.INCOMING
        151, 2 -> HistoryDirection.OUTGOING
        else -> HistoryDirection.UNKNOWN
    }

    private fun Cursor.safeString(name: String): String? {
        val index = getColumnIndex(name)
        return if (index >= 0) getString(index) else null
    }

    private fun Cursor.safeLong(name: String): Long {
        val index = getColumnIndex(name)
        return if (index >= 0) getLong(index) else 0L
    }
}

private data class MessageSyncBatch(val items: Int, val threads: Int)

private data class MessageMediaStats(
    val hasMedia: Boolean = false,
    val attachmentCount: Int = 0,
    val attachmentType: String? = null
)

private fun MessageItemEntity.toCommunicationEvent(
    threadId: String,
    unreadCount: Int,
    read: Boolean
): CommunicationEventEntity {
    return CommunicationEventEntity(
        id = "${threadId}_${itemId}",
        contactId = contactId,
        threadId = threadId,
        source = source,
        direction = direction,
        timestamp = timestamp,
        snippet = body,
        read = read,
        metadata = metadata,
        isPinned = false,
        unreadCount = unreadCount
    )
}

private fun MessageItemEntity.toDomainItem(): com.jdailer.feature.messages.domain.model.UnifiedMessageItem {
    return com.jdailer.feature.messages.domain.model.UnifiedMessageItem(
        itemId = itemId,
        threadId = threadId,
        source = source,
        direction = direction,
        contactId = contactId,
        address = normalizedAddress,
        body = body,
        mediaUri = mediaUri,
        timestamp = timestamp,
        isRead = isRead,
        isRcs = isRcs
    )
}

private fun MessageThreadEntity.toDomainThread(): UnifiedMessageThread {
    return UnifiedMessageThread(
        threadId = threadId,
        source = source,
        contactId = contactId,
        address = normalizedAddress,
        title = title,
        snippet = snippet,
        unreadCount = unreadCount,
        lastMessageAt = lastMessageAt,
        isRcs = isRcs,
        metadata = metadata.toDomainMessageThreadMetadata(),
        isPinned = isPinned,
        isArchived = isArchived
    )
}

private fun String?.toDomainMessageThreadMetadata(): MessageThreadMetadata {
    if (isNullOrBlank()) return MessageThreadMetadata.Empty
    return runCatching {
        val payload = JSONObject(this)
        MessageThreadMetadata(
            isRcs = payload.optBoolean(META_IS_RCS),
            isGroupConversation = payload.optBoolean(META_IS_GROUP),
            participantCount = payload.optInt(META_PARTICIPANTS),
            hasMedia = payload.optBoolean(META_HAS_MEDIA),
            attachmentCount = payload.optInt(META_ATTACHMENT_COUNT),
            attachmentType = payload.optString(META_ATTACHMENT_TYPE).orEmpty().ifBlank { null },
            secureChannel = payload.optBoolean(META_SECURE_CHANNEL)
        )
    }.getOrDefault(MessageThreadMetadata.Empty)
}

private fun MessageThreadMetadata.toStorageJson(): String {
    return JSONObject().apply {
        put(META_IS_RCS, isRcs)
        put(META_IS_GROUP, isGroupConversation)
        put(META_PARTICIPANTS, participantCount)
        put(META_HAS_MEDIA, hasMedia)
        put(META_ATTACHMENT_COUNT, attachmentCount)
        put(META_ATTACHMENT_TYPE, attachmentType)
        put(META_SECURE_CHANNEL, secureChannel)
    }.toString()
}
