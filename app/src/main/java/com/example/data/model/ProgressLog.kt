package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress_logs")
data class ProgressLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int,
    val operationType: String, // "ADDITION", "SUBTRACTION", "MULTIPLICATION", "DIVISION"
    val difficulty: String, // "EASY", "MEDIUM", "HARD"
    val correctCount: Int,
    val totalCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
