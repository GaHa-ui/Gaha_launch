package com.mcoder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity storing Proot distro status.
 */
@Entity(tableName = "proot_status")
data class ProotStatusEntity(
    @PrimaryKey val id: Long = 1,
    val distro: String,
    val installed: Boolean,
    val sizeBytes: Long,
    val lastUpdated: Long
)
