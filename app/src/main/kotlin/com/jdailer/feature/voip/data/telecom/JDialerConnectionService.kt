package com.jdailer.feature.voip.data.telecom

import android.net.Uri
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import com.jdailer.feature.voip.domain.repository.VoipRepository
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.get
import java.util.Locale

class JDialerConnectionService : ConnectionService() {
    private val voipRepository: VoipRepository by lazy {
        get(VoipRepository::class.java)
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        val normalized = sanitizeAddress(request.address?.schemeSpecificPart.orEmpty())
        val callId = runBlocking {
            voipRepository.place(normalized)
        }

        return JDialerManagedConnection(
            callId = callId,
            number = normalized.ifBlank { "Unknown" },
            fullAddress = request.address,
            voipRepository = voipRepository
        )
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        val normalized = sanitizeAddress(request.address?.schemeSpecificPart.orEmpty())
        val callId = "incoming_${normalized.ifBlank { "unknown" }}_" +
            java.util.UUID.randomUUID().toString().replace("-", "").take(8).lowercase(Locale.US)

        val connection = JDialerManagedConnection(
            callId = callId,
            number = normalized.ifBlank { "Unknown" },
            fullAddress = request.address,
            voipRepository = voipRepository
        )
        connection.setIncoming()
        return connection
    }

    private fun sanitizeAddress(address: String): String {
        return address.filter { it.isDigit() || it == '+' }.ifBlank { "" }
    }
}
