package com.mcoder.data.repo

import com.mcoder.data.db.ServerProfileDao
import com.mcoder.data.db.ServerProfileEntity
import com.mcoder.domain.ServerProfileRepository
import com.mcoder.domain.model.ServerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository for server profiles backed by Room.
 */
class ServerProfileRepositoryImpl @Inject constructor(
    private val dao: ServerProfileDao
) : ServerProfileRepository {

    override fun observeProfiles(): Flow<List<ServerProfile>> {
        return dao.observeProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addProfile(profile: ServerProfile) {
        dao.insert(profile.toEntity())
    }

    override suspend fun deleteProfile(profile: ServerProfile) {
        dao.delete(profile.toEntity())
    }

    private fun ServerProfileEntity.toDomain(): ServerProfile {
        return ServerProfile(id = id, name = name, url = url, token = token)
    }

    private fun ServerProfile.toEntity(): ServerProfileEntity {
        return ServerProfileEntity(id = id, name = name, url = url, token = token)
    }
}
