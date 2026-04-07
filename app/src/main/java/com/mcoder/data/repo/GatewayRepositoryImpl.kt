package com.mcoder.data.repo

import com.mcoder.domain.GatewayRepository
import com.mcoder.domain.model.AgentInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory gateway repository for agent status.
 */
@Singleton
class GatewayRepositoryImpl @Inject constructor() : GatewayRepository {

    private val agentsFlow = MutableStateFlow(
        listOf(
            AgentInfo("opencode", "OpenCode", "Terminal agent", isInstalled = true, isActive = true),
            AgentInfo("codex", "Codex CLI", "OpenAI terminal agent", isInstalled = false, isActive = false),
            AgentInfo("openclaw", "OpenClaw", "Terminal agent", isInstalled = false, isActive = false),
            AgentInfo("cursor", "Cursor CLI", "API-based agent", isInstalled = false, isActive = false),
            AgentInfo("claude", "Claude Code", "Anthropic CLI", isInstalled = false, isActive = false),
            AgentInfo("gemini", "Gemini CLI", "Google CLI", isInstalled = false, isActive = false),
            AgentInfo("aider", "Aider", "Lightweight agent", isInstalled = false, isActive = false),
            AgentInfo("continue", "Continue", "IDE plugin", isInstalled = false, isActive = false),
            AgentInfo("tabby", "Tabby", "Local agent", isInstalled = false, isActive = false)
        )
    )

    override fun observeAgents(): Flow<List<AgentInfo>> = agentsFlow

    override suspend fun setActiveAgent(agentId: String) {
        agentsFlow.update { current ->
            current.map { agent ->
                agent.copy(isActive = agent.id == agentId)
            }
        }
    }
}
