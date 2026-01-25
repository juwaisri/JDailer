package com.jdailer.sync

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.NetworkErrorException
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder

class AuthenticatorService : Service() {
    private lateinit var authenticator: JDialerAccountAuthenticator

    override fun onCreate() {
        super.onCreate()
        authenticator = JDialerAccountAuthenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return authenticator.iBinder
    }
}

private class JDialerAccountAuthenticator(
    private val context: Service
) : AbstractAccountAuthenticator(context) {
    override fun editProperties(
        response: AccountAuthenticatorResponse,
        accountType: String
    ): Bundle {
        return Bundle()
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        return Bundle()
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse,
        account: Account,
        options: Bundle?
    ): Bundle {
        return Bundle()
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle?
    ): Bundle {
        throw NetworkErrorException("Auth token not implemented")
    }

    override fun getAuthTokenLabel(authTokenType: String): String = ""

    override fun updateCredentials(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle?
    ): Bundle {
        return Bundle()
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse,
        account: Account,
        features: Array<out String>
    ): Bundle {
        return Bundle()
    }
}
