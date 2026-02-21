package com.jdailer.core.permissions

import android.Manifest

enum class PermissionGroup(val permissions: List<String>) {
    PHONE(
        listOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.READ_PHONE_NUMBERS
        )
    ),
    CALL_LOG(
        listOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG)
    ),
    CONTACTS(
        listOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
    ),
    SMS(
        listOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS)
    ),
    TELEPHONY_MANAGE(Manifest.permission.MANAGE_OWN_CALLS.let(::listOf)),
    RECORDING(listOf(Manifest.permission.RECORD_AUDIO)),
    POST_NOTIFICATIONS(listOf(Manifest.permission.POST_NOTIFICATIONS)),
    FOREGROUND_SERVICE(listOf(Manifest.permission.FOREGROUND_SERVICE))
}

fun allRuntimePermissions(): List<String> {
    return PermissionGroup.entries.flatMap { it.permissions }
}
