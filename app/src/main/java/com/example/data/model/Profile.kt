package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val avatar: String, // "bunny", "fox", "bear", "lion", "owl", "panda"
    val age: Int,
    val difficulty: String = "EASY", // "EASY", "MEDIUM", "HARD"
    val language: String = "sq", // "sq" or "en"
    val coins: Int = 0,
    val unlockedBadges: String = "", // comma separated keys like: "addition_conqueror,speedy"
    val unlockedStickers: String = "", // comma-separated key IDs of purchased virtual stickers/toys
    val currentLevel: Int = 1,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
