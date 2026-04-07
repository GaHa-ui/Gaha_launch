package com.mcoder.domain.usecase

import com.mcoder.domain.ChatRepository

/**
 * Chat-related use cases.
 */
class ChatUseCases(private val repository: ChatRepository) {
    val observeHistory = repository::observeMessages
    val clearHistory = repository::clearHistory
    suspend fun sendMessage(role: String, content: String): Long {
        return repository.insertMessage(role, content)
    }
    suspend fun updateMessage(id: Long, role: String, content: String, createdAt: Long) {
        repository.updateMessage(id, role, content, createdAt)
    }
}
