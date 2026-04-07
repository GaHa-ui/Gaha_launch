package com.mcoder.domain

import com.mcoder.domain.model.ProotStatus
import kotlinx.coroutines.flow.Flow

/**
 * Proot status persistence.
 */
interface ProotRepository {
    fun observeStatus(): Flow<ProotStatus>
    suspend fun updateStatus(status: ProotStatus)
}
