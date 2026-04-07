package com.mcoder.data.repo

import com.mcoder.data.db.ChatDao
import com.mcoder.data.db.ChatMessageEntity
import com.mcoder.domain.ChatRepository
import com.mcoder.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Chat repository backed by Room.
 */
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {
    override fun observeMessages(): Flow<List<ChatMessage>> {
        return chatDao.observeMessages().map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    role = entity.role,
                    content = entity.content,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun insertMessage(role: String, content: String): Long {
        val entity = ChatMessageEntity(
            role = role,
            content = content,
            createdAt = System.currentTimeMillis()
        )
        return chatDao.insert(entity)
    }

    override suspend fun updateMessage(id: Long, role: String, content: String, createdAt: Long) {
        chatDao.update(
            ChatMessageEntity(
                id = id,
                role = role,
                content = content,
                createdAt = createdAt
            )
        )
    }

    override suspend fun clearHistory() {
        chatDao.clear()
    }
}
