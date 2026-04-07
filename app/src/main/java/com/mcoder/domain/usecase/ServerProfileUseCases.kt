package com.mcoder.domain.usecase

import com.mcoder.domain.ServerProfileRepository

/**
 * Use cases for server profiles.
 */
class ServerProfileUseCases(private val repository: ServerProfileRepository) {
    val observeProfiles = repository::observeProfiles
    val addProfile = repository::addProfile
    val deleteProfile = repository::deleteProfile
}
