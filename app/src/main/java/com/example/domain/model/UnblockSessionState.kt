package com.example.domain.model

data class UnblockSessionState(
    val status: ProtectionStatus = ProtectionStatus.PROTECTION_OFF,
    val currentStage: Int = 1,
    val completedStages: Int = 0,
    val stageStartTimestampEpochMs: Long = 0L,
    val unblockRequestTimestampEpochMs: Long = 0L,
    val stageDurationMs: Long = 60 * 60 * 1000L // 60 minutes per stage (3,600,000 ms)
) {
    val totalStages: Int = 24

    fun getRemainingTimeMs(now: Long = System.currentTimeMillis()): Long {
        if (status != ProtectionStatus.HOUR_RUNNING || stageStartTimestampEpochMs <= 0L) {
            return 0L
        }
        val elapsed = now - stageStartTimestampEpochMs
        return (stageDurationMs - elapsed).coerceAtLeast(0L)
    }

    fun isCurrentHourFinished(now: Long = System.currentTimeMillis()): Boolean {
        if (status != ProtectionStatus.HOUR_RUNNING || stageStartTimestampEpochMs <= 0L) {
            return false
        }
        return (now - stageStartTimestampEpochMs) >= stageDurationMs
    }

    fun formatRemainingTime(now: Long = System.currentTimeMillis()): String {
        val remainingMs = getRemainingTimeMs(now)
        val totalSec = remainingMs / 1000
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
    }

    val overallProgress: Float
        get() = (completedStages.toFloat() / totalStages.toFloat()).coerceIn(0f, 1f)
}
