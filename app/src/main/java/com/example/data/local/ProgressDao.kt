package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ProgressLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress_logs WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getLogsForProfileFlow(profileId: Int): Flow<List<ProgressLog>>

    @Query("SELECT * FROM progress_logs WHERE profileId = :profileId ORDER BY timestamp DESC")
    suspend fun getLogsForProfile(profileId: Int): List<ProgressLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ProgressLog): Long

    @Query("DELETE FROM progress_logs WHERE profileId = :profileId")
    suspend fun deleteLogsForProfile(profileId: Int)
}
