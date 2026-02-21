package com.jdailer.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.jdailer.feature.call.spam.domain.usecase.EvaluateCallerProfileUseCase
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.get

class CallScreeningService : CallScreeningService() {
    private val evaluateCallerProfileUseCase: EvaluateCallerProfileUseCase by lazy {
        get(EvaluateCallerProfileUseCase::class.java)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart.orEmpty()
        if (number.isBlank()) {
            respondToCall(
                callDetails,
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                .build()
            )
            return
        }

        val decision = runBlocking { evaluateCallerProfileUseCase(number) }
        val blocked = !decision.shouldAllow || decision.isBlocked
        val response = if (blocked) {
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)
                .setSkipNotification(decision.shouldWarn.not())
                .build()
        } else {
            CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        }
        respondToCall(callDetails, response)
    }
}
