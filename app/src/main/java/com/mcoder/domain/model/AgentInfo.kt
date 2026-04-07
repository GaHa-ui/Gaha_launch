package com.mcoder.domain.model

/**
 * Represents an AI agent available in the Gateway.
 */
data class AgentInfo(
    val id: String,
    val name: String,
    val description: String,
    val isInstalled: Boolean,
    val isActive: Boolean,
    val isRunning: Boolean
)
