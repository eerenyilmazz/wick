package com.wick.core.network.session

import com.wick.core.model.AppResult
import kotlinx.coroutines.flow.Flow

interface ChatSocketClient {

    suspend fun connect(): AppResult<Unit>

    fun observeConnectionState(): Flow<com.wick.core.model.ConnectionState>

    fun subscribeToMessages(sessionId: String): Flow<MessageResponseDto>

    fun subscribeToRemainingSeconds(sessionId: String): Flow<Long>

    fun subscribeToTyping(sessionId: String): Flow<String>

    suspend fun sendMessage(sessionId: String, content: String, clientMessageId: String): AppResult<Unit>

    suspend fun sendTyping(sessionId: String)

    suspend fun disconnect()
}
