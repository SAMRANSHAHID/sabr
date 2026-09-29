package com.example.domain.manager

import com.example.data.local.DataStoreManager
import com.example.domain.model.ProtectionStatus
import com.example.domain.model.UnblockSessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface UnblockSessionManager {
    val sessionStateFlow: Flow<UnblockSessionState>
    suspend fun requestUnblock(): UnblockSessionState
    suspend fun startCurrentHour(now: Long = System.currentTimeMillis()): UnblockSessionState
    suspend fun syncTimerState(now: Long = System.currentTimeMillis()): UnblockSessionState
    suspend fun cancelUnblock(): UnblockSessionState
    suspend fun completeUnblockAndTurnOff(): UnblockSessionState
    suspend fun activateProtection(): UnblockSessionState
    suspend fun setStageDurationForTesting(durationMs: Long)
}

class UnblockSessionManagerImpl(
    private val dataStoreManager: DataStoreManager
) : UnblockSessionManager {

    override val sessionStateFlow: Flow<UnblockSessionState> = dataStoreManager.unblockSessionStateFlow

    override suspend fun activateProtection(): UnblockSessionState {
        val newState = UnblockSessionState(
            status = ProtectionStatus.PROTECTION_ACTIVE,
            currentStage = 1,
            completedStages = 0,
            stageStartTimestampEpochMs = 0L,
            unblockRequestTimestampEpochMs = 0L
        )
        dataStoreManager.saveUnblockSessionState(newState)
        return newState
    }

    override suspend fun requestUnblock(): UnblockSessionState {
        val current = dataStoreManager.unblockSessionStateFlow.first()
        if (current.status != ProtectionStatus.PROTECTION_ACTIVE) {
            return current
        }

        val updated = current.copy(
            status = ProtectionStatus.UNBLOCK_REQUESTED,
            unblockRequestTimestampEpochMs = System.currentTimeMillis()
        )
        dataStoreManager.saveUnblockSessionState(updated)
        return updated
    }

    override suspend fun startCurrentHour(now: Long): UnblockSessionState {
        val current = dataStoreManager.unblockSessionStateFlow.first()

        val nextStage = when (current.status) {
            ProtectionStatus.UNBLOCK_REQUESTED -> 1
            ProtectionStatus.HOUR_COMPLETE -> (current.completedStages + 1).coerceAtMost(24)
            else -> return current
        }

        val updated = current.copy(
            status = ProtectionStatus.HOUR_RUNNING,
            currentStage = nextStage,
            stageStartTimestampEpochMs = now
        )
        dataStoreManager.saveUnblockSessionState(updated)
        return updated
    }

    override suspend fun syncTimerState(now: Long): UnblockSessionState {
        val current = dataStoreManager.unblockSessionStateFlow.first()

        if (current.status == ProtectionStatus.HOUR_RUNNING) {
            val elapsed = now - current.stageStartTimestampEpochMs
            if (elapsed >= current.stageDurationMs) {
                // Hour is complete!
                val newCompleted = current.currentStage
                val newStatus = if (newCompleted >= 24) {
                    ProtectionStatus.UNBLOCK_AVAILABLE
                } else {
                    ProtectionStatus.HOUR_COMPLETE
                }

                val updated = current.copy(
                    status = newStatus,
                    completedStages = newCompleted
                )
                dataStoreManager.saveUnblockSessionState(updated)
                return updated
            }
        }

        return current
    }

    override suspend fun cancelUnblock(): UnblockSessionState {
        val current = dataStoreManager.unblockSessionStateFlow.first()
        val updated = current.copy(
            status = ProtectionStatus.PROTECTION_ACTIVE,
            currentStage = 1,
            completedStages = 0,
            stageStartTimestampEpochMs = 0L,
            unblockRequestTimestampEpochMs = 0L
        )
        dataStoreManager.saveUnblockSessionState(updated)
        return updated
    }

    override suspend fun completeUnblockAndTurnOff(): UnblockSessionState {
        val current = dataStoreManager.unblockSessionStateFlow.first()
        if (current.status != ProtectionStatus.UNBLOCK_AVAILABLE) {
            return current
        }

        val updated = UnblockSessionState(
            status = ProtectionStatus.PROTECTION_OFF,
            currentStage = 1,
            completedStages = 0,
            stageStartTimestampEpochMs = 0L,
            unblockRequestTimestampEpochMs = 0L
        )
        dataStoreManager.saveUnblockSessionState(updated)
        return updated
    }

    override suspend fun setStageDurationForTesting(durationMs: Long) {
        dataStoreManager.setStageDurationForTesting(durationMs)
    }
}
