package com.mcoder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for chat history.
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val content: String,
    val createdAt: Long
)
