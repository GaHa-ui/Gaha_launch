package com.mcoder.domain

import com.mcoder.domain.model.AgentInfo
import kotlinx.coroutines.flow.Flow

/**
 * Gateway data for agent status and switching.
 */
interface GatewayRepository {
    fun observeAgents(): Flow<List<AgentInfo>>
    suspend fun setActiveAgent(agentId: String)
    suspend fun startAgent(agentId: String)
    suspend fun stopAgent(agentId: String)
}
