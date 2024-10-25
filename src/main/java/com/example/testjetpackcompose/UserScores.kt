package com.example.testjetpackcompose

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserScores")
data class UserScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: String,
    val score: Int
)
