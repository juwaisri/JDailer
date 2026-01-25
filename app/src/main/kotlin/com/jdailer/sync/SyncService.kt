package com.jdailer.sync

import android.app.Service
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.Intent
import android.content.SyncResult
import android.os.Bundle
import android.os.IBinder

class SyncService : Service() {
    private lateinit var syncAdapter: AbstractThreadedSyncAdapter

    override fun onCreate() {
        super.onCreate()
        syncAdapter = JDialerSyncAdapter(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return syncAdapter.syncAdapterBinder
    }
}

private class JDialerSyncAdapter(context: Context) : AbstractThreadedSyncAdapter(context, true) {
    override fun onPerformSync(
        account: android.accounts.Account,
        extras: Bundle,
        authority: String,
        provider: ContentProviderClient,
        syncResult: SyncResult
    ) {
        // Placeholder: add sync implementation later.
    }
}
