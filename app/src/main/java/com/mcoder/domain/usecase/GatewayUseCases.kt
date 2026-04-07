package com.mcoder.domain.usecase

import com.mcoder.domain.GatewayRepository

/**
 * Gateway use cases for agents.
 */
class GatewayUseCases(private val repository: GatewayRepository) {
    val observeAgents = repository::observeAgents
    val setActiveAgent = repository::setActiveAgent
    val startAgent = repository::startAgent
    val stopAgent = repository::stopAgent
}
