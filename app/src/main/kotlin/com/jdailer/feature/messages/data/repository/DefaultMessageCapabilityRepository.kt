package com.jdailer.feature.messages.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Telephony
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.jdailer.feature.messages.domain.model.MessageAttachmentMeta
import com.jdailer.feature.messages.domain.model.MessageAttachmentPolicy
import com.jdailer.feature.messages.domain.model.RcsCapability
import com.jdailer.feature.messages.domain.repository.MessageCapabilityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val MIME_UNKNOWN = "application/octet-stream"

class DefaultMessageCapabilityRepository(
    private val context: Context
) : MessageCapabilityRepository {
    override suspend fun getRcsCapability(): RcsCapability = withContext(Dispatchers.IO) {
        val defaultSmsPackage = runCatching { Telephony.Sms.getDefaultSmsPackage(context) }.getOrDefault(null)
        val supportsDefaultSms = !defaultSmsPackage.isNullOrBlank()
        val supportsCarrierService = runCatching { defaultSmsPackage.orEmpty().isNotBlank() }
            .getOrDefault(false)

        val fallbackIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:+"))
        val canHandleFallback = context.packageManager.resolveActivity(fallbackIntent, 0) != null

        val rcsPackage = runCatching {
            if (!supportsDefaultSms) {
                null
            } else {
                context.packageManager.getPackageInfo(defaultSmsPackage.orEmpty(), 0)
                defaultSmsPackage
            }
        }.getOrNull()

        RcsCapability(
            isRcsEnabledByDefaultSmsApp = supportsDefaultSms,
            isRcsAvailableAsCarrierService = canHandleFallback && supportsCarrierService,
            defaultSmsPackageName = defaultSmsPackage,
            supportsRcsMmsFallback = supportsDefaultSms,
            rcsServicePackage = rcsPackage,
            detectedAtMs = System.currentTimeMillis()
        )
    }

    override suspend fun inspectAttachments(
        attachmentUris: List<String>,
        policy: MessageAttachmentPolicy
    ): List<MessageAttachmentMeta> = withContext(Dispatchers.IO) {
        attachmentUris
            .mapNotNull { rawUri ->
                runCatching {
                    val uri = rawUri.toUri()
                    val contentResolver = context.contentResolver
                    val cursor = contentResolver.query(
                        uri,
                        arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                        null,
                        null,
                        null
                    )

                    var fileName: String? = null
                    var bytes = 0L
                    var mimeType = contentResolver.getType(uri)

                    cursor?.use {
                        if (it.moveToFirst()) {
                            val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
                            if (nameIdx >= 0) fileName = it.getString(nameIdx)
                            if (sizeIdx >= 0) bytes = it.getLong(sizeIdx)
                        }
                    }

                    if (mimeType.isNullOrBlank()) {
                        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
                            ?: MIME_UNKNOWN
                    }

                    if (bytes <= 0L && rawUri.startsWith("file://")) {
                        bytes = runCatching { java.io.File(uri.path.orEmpty()).length() }
                            .getOrDefault(0L)
                        fileName = fileName ?: uri.lastPathSegment
                    }

                    val dimensions = detectDimensions(uri, mimeType)

                    MessageAttachmentMeta(
                        uri = rawUri,
                        fileName = fileName,
                        mimeType = mimeType,
                        bytes = if (bytes > 0L) bytes else 0L,
                        width = dimensions?.first,
                        height = dimensions?.second
                    )
                }.getOrNull()
            }
    }

    private fun detectDimensions(uri: Uri, mimeType: String?): Pair<Int, Int>? {
        val normalized = mimeType.orEmpty().lowercase()
        return when {
            normalized.startsWith("image/") -> runCatching { parseImageDimensions(uri) }.getOrNull()
            normalized.startsWith("video/") -> runCatching { parseVideoDimensions(uri) }.getOrNull()
            else -> null
        }
    }

    private fun parseImageDimensions(uri: Uri): Pair<Int, Int>? {
        val contentResolver = context.contentResolver
        contentResolver.openInputStream(uri)?.use { stream ->
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(stream, null, opts)
            val width = opts.outWidth
            val height = opts.outHeight
            if (width > 0 && height > 0) return Pair(width, height)
        }
        return null
    }

    private fun parseVideoDimensions(uri: Uri): Pair<Int, Int>? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val width = runCatching {
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            }.getOrNull()
            val height = runCatching {
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            }.getOrNull()
            if (width != null && height != null && width > 0 && height > 0) {
                Pair(width, height)
            } else {
                null
            }
        } catch (_: IOException) {
            null
        } finally {
            retriever.release()
        }
    }
}
