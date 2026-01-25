package com.jdailer.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class CallRecordingService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
