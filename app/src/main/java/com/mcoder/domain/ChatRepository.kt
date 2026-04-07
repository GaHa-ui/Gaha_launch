package com.mcoder.domain

import com.mcoder.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Chat data access contract.
 */
interface ChatRepository {
    fun observeMessages(): Flow<List<ChatMessage>>
    suspend fun insertMessage(role: String, content: String): Long
    suspend fun updateMessage(id: Long, role: String, content: String, createdAt: Long)
    suspend fun clearHistory()
}
