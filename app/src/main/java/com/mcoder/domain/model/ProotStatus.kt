package com.mcoder.domain.model

/**
 * Proot distro status information.
 */
data class ProotStatus(
    val id: Long = 1,
    val distro: String = "ubuntu",
    val installed: Boolean = false,
    val sizeBytes: Long = 0,
    val lastUpdated: Long = 0
)
