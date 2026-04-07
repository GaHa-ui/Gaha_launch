package com.mcoder.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Proot status.
 */
@Dao
interface ProotDao {
    @Query("SELECT * FROM proot_status WHERE id = 1")
    fun observeStatus(): Flow<ProotStatusEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProotStatusEntity)
}
