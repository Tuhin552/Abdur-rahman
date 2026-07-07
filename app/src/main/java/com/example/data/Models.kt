package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val salt: String,
    val heightFt: Int,
    val heightIn: Int,
    val targetWeight: Double? = null,
    val targetBmi: Double? = null
)

@Entity(tableName = "bmi_entries")
data class BmiEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val weightKg: Double,
    val heightFt: Int,
    val heightIn: Int,
    val bmi: Double,
    val timestamp: Long,
    val note: String? = null
)
