package com.mcoder.domain.model

/**
 * Remote server profile for quick switching.
 */
data class ServerProfile(
    val id: Long,
    val name: String,
    val url: String,
    val token: String
)
