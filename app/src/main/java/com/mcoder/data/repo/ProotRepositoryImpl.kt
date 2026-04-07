package com.mcoder.data.repo

import com.mcoder.data.db.ProotDao
import com.mcoder.data.db.ProotStatusEntity
import com.mcoder.domain.ProotRepository
import com.mcoder.domain.model.ProotStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Proot status repository backed by Room.
 */
class ProotRepositoryImpl @Inject constructor(
    private val prootDao: ProotDao
) : ProotRepository {

    override fun observeStatus(): Flow<ProotStatus> {
        return prootDao.observeStatus().map { entity ->
            entity?.toDomain() ?: ProotStatus()
        }
    }

    override suspend fun updateStatus(status: ProotStatus) {
        prootDao.upsert(status.toEntity())
    }

    private fun ProotStatusEntity.toDomain(): ProotStatus {
        return ProotStatus(
            id = id,
            distro = distro,
            installed = installed,
            sizeBytes = sizeBytes,
            lastUpdated = lastUpdated
        )
    }

    private fun ProotStatus.toEntity(): ProotStatusEntity {
        return ProotStatusEntity(
            id = id,
            distro = distro,
            installed = installed,
            sizeBytes = sizeBytes,
            lastUpdated = lastUpdated
        )
    }
}
