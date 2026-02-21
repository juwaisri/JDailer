package com.jdailer.feature.integrations.base

import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber

object IntegrationIntentLauncher {
    fun launchWithCandidates(
        context: Context,
        action: String,
        uri: Uri,
        preferredPackages: List<String>,
        chooserTitle: String,
        applyIntent: (Intent.() -> Unit)? = null,
    ): IntentResult {
        val candidates = preferredPackages
            .map { packageName ->
                Intent(action, uri).apply {
                    setPackage(packageName)
                    applyIntent?.invoke(this)
                }
            }
            .toMutableList()

        candidates.add(Intent(action, uri).apply { applyIntent?.invoke(this) })

        candidates.forEach { candidate ->
            if (candidate.resolveActivity(context.packageManager) != null) {
                return safeStart(context, candidate)
            }
        }

        if (chooserTitle.isBlank()) {
            return IntentResult.Unavailable("No app available for ${uri.scheme}")
        }

        val fallback = Intent.createChooser(candidates.last(), chooserTitle)
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        return safeStart(context, fallback)
    }

    fun launchSendTextIntent(
        context: Context,
        uri: Uri,
        preferredPackages: List<String>,
        chooserTitle: String,
        subject: String? = null,
        body: String? = null,
    ): IntentResult {
        val action = Intent.ACTION_SEND
        val mimeType = "text/plain"

        val candidates = preferredPackages
            .map { packageName ->
                Intent(action).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_SUBJECT, subject.orEmpty())
                    putExtra(Intent.EXTRA_TEXT, body.orEmpty())
                    setPackage(packageName)
                }
            }
            .toMutableList()

        candidates.add(
            Intent(action).apply {
                type = mimeType
                putExtra(Intent.EXTRA_SUBJECT, subject.orEmpty())
                putExtra(Intent.EXTRA_TEXT, body.orEmpty())
            }
        )

        candidates.forEach { candidate ->
            if (candidate.resolveActivity(context.packageManager) != null) {
                return safeStart(context, candidate)
            }
        }

        val chooser = Intent.createChooser(Intent(action, uri).apply {
            type = mimeType
            putExtra(Intent.EXTRA_SUBJECT, subject.orEmpty())
            putExtra(Intent.EXTRA_TEXT, body.orEmpty())
        }, chooserTitle).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        return safeStart(context, chooser)
    }

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return runCatching {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        }.getOrDefault(false)
    }

    private fun safeStart(context: Context, intent: Intent): IntentResult {
        return runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            IntentResult.Success
        }.getOrElse { exception ->
            Timber.w(exception, "Unable to launch platform intent")
            IntentResult.Failure("Platform launch failed", exception)
        }
    }
}

