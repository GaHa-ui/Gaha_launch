package com.mcoder.domain.usecase

import com.mcoder.domain.ProotRepository

/**
 * Proot status use cases.
 */
class ProotUseCases(private val repository: ProotRepository) {
    val observeStatus = repository::observeStatus
    val updateStatus = repository::updateStatus
}
