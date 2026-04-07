package com.mcoder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for server profiles.
 */
@Entity(tableName = "server_profiles")
data class ServerProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val token: String
)
