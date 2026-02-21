package com.jdailer.feature.voip.data.repository

import com.jdailer.feature.voip.domain.model.VoipCallState
import com.jdailer.feature.voip.domain.model.VoipSession
import com.jdailer.feature.voip.domain.repository.VoipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class InMemoryVoipRepository : VoipRepository {
    private val sessions = MutableStateFlow<List<VoipSession>>(emptyList())

    override fun observeSessions(): Flow<List<VoipSession>> = sessions.asStateFlow()

    override suspend fun place(number: String): String {
        val id = UUID.randomUUID().toString()
        val current = sessions.value.toMutableList()
        current.add(
            VoipSession(
                callId = id,
                address = number,
                state = VoipCallState.CONNECTING
            )
        )
        sessions.value = current
        return id
    }

    override suspend fun accept(callId: String) {
        update(callId) { it.copy(state = VoipCallState.ACTIVE) }
    }

    override suspend fun reject(callId: String) {
        update(callId) { it.copy(state = VoipCallState.FAILED) }
    }

    override suspend fun hangUp(callId: String) {
        update(callId) { it.copy(state = VoipCallState.DISCONNECTED) }
    }

    override suspend fun setMute(callId: String, muted: Boolean) {
        update(callId) { it.copy(muted = muted) }
    }

    override suspend fun setHold(callId: String, onHold: Boolean) {
        update(callId) { it.copy(onHold = onHold) }
    }

    private fun update(callId: String, transform: (VoipSession) -> VoipSession) {
        val current = sessions.value.toMutableList()
        val index = current.indexOfFirst { it.callId == callId }
        if (index >= 0) {
            current[index] = transform(current[index])
            sessions.value = current
        }
    }
}
