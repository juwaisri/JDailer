package com.jdailer.feature.messages.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.jdailer.feature.messages.domain.usecase.SyncMessagesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get
import timber.log.Timber

class SmsMmsReceiver : BroadcastReceiver() {
    private val syncMessagesUseCase: SyncMessagesUseCase by lazy {
        get(SyncMessagesUseCase::class.java)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION,
            Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION,
            Telephony.Sms.Intents.SMS_DELIVER_ACTION,
            "android.provider.Telephony.WAP_PUSH_DELIVER" -> {
                Timber.d("SMS/MMS event received, scheduling background sync")
                CoroutineScope(Dispatchers.Default).launch {
                    runCatching { syncMessagesUseCase() }
                        .onFailure {
                            Timber.w("sms sync failed: ${it.message}")
                        }
                }
            }
        }
    }
}
