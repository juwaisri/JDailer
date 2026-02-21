package com.jdailer.feature.voip.data.telecom

import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import android.app.role.RoleManager
import android.os.Build
import android.content.Intent
import android.telecom.TelecomManager

class TelecomRoleManager(
    private val context: Context
) {
    private val roleManager: RoleManager? = if (Build.VERSION.SDK_INT >= 29) {
        context.getSystemService(RoleManager::class.java)
    } else {
        null
    }

    val hasRoleManager: Boolean = roleManager != null

    fun isDefaultDialerRoleActive(): Boolean {
        return if (Build.VERSION.SDK_INT >= 29) {
            roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) == true
        } else {
            false
        }
    }

    fun buildDefaultDialerRequestIntent(): Intent? {
        if (!hasRoleManager) return null
        return roleManager!!.createRequestRoleIntent(RoleManager.ROLE_DIALER)
    }

    fun hasTelecomBindingPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.BIND_TELECOM_CONNECTION_SERVICE) == PackageManager.PERMISSION_GRANTED
    }

    fun canTelecomManagerRouteCalls(): Boolean {
        return context.getSystemService(TelecomManager::class.java) != null
    }
}
