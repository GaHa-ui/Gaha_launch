package com.mcoder.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for server profiles.
 */
@Dao
interface ServerProfileDao {
    @Query("SELECT * FROM server_profiles ORDER BY id ASC")
    fun observeProfiles(): Flow<List<ServerProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ServerProfileEntity)

    @Delete
    suspend fun delete(profile: ServerProfileEntity)
}
