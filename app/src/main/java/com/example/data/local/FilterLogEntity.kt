package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_logs")
data class FilterLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val domain: String,
    val isBlocked: Boolean,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
