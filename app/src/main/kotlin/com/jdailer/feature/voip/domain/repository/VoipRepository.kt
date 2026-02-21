package com.jdailer.feature.voip.domain.repository

import com.jdailer.feature.voip.domain.model.VoipSession
import kotlinx.coroutines.flow.Flow

interface VoipRepository {
    fun observeSessions(): Flow<List<VoipSession>>
    suspend fun place(number: String): String
    suspend fun accept(callId: String)
    suspend fun reject(callId: String)
    suspend fun hangUp(callId: String)
    suspend fun setMute(callId: String, muted: Boolean)
    suspend fun setHold(callId: String, onHold: Boolean)
}
