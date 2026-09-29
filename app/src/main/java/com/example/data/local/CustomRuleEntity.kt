package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_rules",
    indices = [Index(value = ["domain"], unique = true)]
)
data class CustomRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val domain: String,
    val isAllowed: Boolean, // true = whitelist, false = custom blocklist
    val category: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
