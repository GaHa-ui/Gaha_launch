package com.mcoder.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for chat history and settings.
 */
@Database(
    entities = [
        ChatMessageEntity::class,
        UserSettingsEntity::class,
        ProotStatusEntity::class,
        ServerProfileEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun settingsDao(): SettingsDao
    abstract fun prootDao(): ProotDao
    abstract fun serverProfileDao(): ServerProfileDao
}
