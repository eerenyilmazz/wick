package com.wick.core.data.session

import com.wick.core.model.AppResult
import com.wick.core.model.ChatMessage
import com.wick.core.model.ChatSession
import com.wick.core.model.ConnectionState
import com.wick.core.model.ConversationSummary
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    suspend fun listConversations(): AppResult<List<ConversationSummary>>

    suspend fun getActiveSession(matchId: String): AppResult<ChatSession>

    suspend fun getLatestSession(matchId: String): AppResult<ChatSession>

    suspend fun getMessageHistory(sessionId: String): AppResult<List<ChatMessage>>

    suspend fun getMissedMessages(sessionId: String, since: String): AppResult<List<ChatMessage>>

    suspend fun extendSession(sessionId: String): AppResult<ChatSession>

    suspend fun endSession(sessionId: String): AppResult<Unit>

    suspend fun connect(): AppResult<Unit>

    fun observeConnectionState(): Flow<ConnectionState>

    fun observeMessages(sessionId: String): Flow<ChatMessage>

    fun observeRemainingSeconds(sessionId: String): Flow<Long>

    fun observeTyping(sessionId: String): Flow<String>

    suspend fun sendMessage(sessionId: String, content: String, clientMessageId: String): AppResult<Unit>

    suspend fun sendTyping(sessionId: String)

    suspend fun disconnect()
}
