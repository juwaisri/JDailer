package com.jdailer.feature.voip.data.telecom

import android.net.Uri
import android.telecom.Connection
import android.telecom.DisconnectCause
import com.jdailer.feature.voip.domain.repository.VoipRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JDialerManagedConnection(
    private val callId: String,
    private val number: String,
    fullAddress: Uri?,
    private val voipRepository: VoipRepository
) : Connection() {
    private companion object {
        const val PRES_ALLOWED = 1
        const val CAPABILITY_SUPPORT_HOLDING = 4
    }

    init {
        setAddress(fullAddress ?: Uri.parse("tel:$number"), PRES_ALLOWED)
        setCallerDisplayName(number, PRES_ALLOWED)
        setInitializing()
        setConnectionProperties(PROPERTY_SELF_MANAGED)
        setAudioModeIsVoip(true)
    }

    fun setIncoming() {
        setConnectionCapabilities(CAPABILITY_SUPPORT_HOLDING)
        setRinging()
    }

    override fun onAnswer() {
        super.onAnswer()
        CoroutineScope(Dispatchers.IO).launch {
            voipRepository.accept(callId)
        }
        setActive()
    }

    override fun onReject() {
        super.onReject()
        CoroutineScope(Dispatchers.IO).launch {
            voipRepository.reject(callId)
        }
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
        destroy()
    }

    override fun onDisconnect() {
        super.onDisconnect()
        CoroutineScope(Dispatchers.IO).launch {
            voipRepository.hangUp(callId)
        }
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
    }

    override fun onHold() {
        super.onHold()
        setOnHold()
        CoroutineScope(Dispatchers.IO).launch {
            voipRepository.setHold(callId, true)
        }
    }

    override fun onUnhold() {
        super.onUnhold()
        setActive()
        CoroutineScope(Dispatchers.IO).launch {
            voipRepository.setHold(callId, false)
        }
    }

    override fun onMuteStateChanged(isMuted: Boolean) {
        super.onMuteStateChanged(isMuted)
        CoroutineScope(Dispatchers.IO).launch {
            voipRepository.setMute(callId, isMuted)
        }
    }
}
