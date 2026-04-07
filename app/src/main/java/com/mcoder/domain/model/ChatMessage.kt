package com.mcoder.domain.model

/**
 * Domain model for a chat message.
 */
data class ChatMessage(
    val id: Long,
    val role: String,
    val content: String,
    val createdAt: Long
)
