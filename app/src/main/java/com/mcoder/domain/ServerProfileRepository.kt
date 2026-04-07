package com.mcoder.domain

import com.mcoder.domain.model.ServerProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository for saved server profiles.
 */
interface ServerProfileRepository {
    fun observeProfiles(): Flow<List<ServerProfile>>
    suspend fun addProfile(profile: ServerProfile)
    suspend fun deleteProfile(profile: ServerProfile)
}
