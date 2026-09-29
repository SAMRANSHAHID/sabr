package com.example.domain.manager

import android.content.Context
import com.example.domain.model.ProtectionStatus
import com.example.domain.model.UnblockSessionState
import kotlinx.coroutines.flow.Flow

interface ProtectionStateManager {
    val stateFlow: Flow<UnblockSessionState>
    suspend fun enableProtection(context: Context): Boolean
    suspend fun requestUnblock(): UnblockSessionState
    suspend fun startHour(): UnblockSessionState
    suspend fun cancelUnblock(): UnblockSessionState
    suspend fun finalizeUnblock(context: Context): Boolean
    suspend fun syncState(now: Long = System.currentTimeMillis()): UnblockSessionState
}

class ProtectionStateManagerImpl(
    private val unblockSessionManager: UnblockSessionManager,
    private val vpnController: VpnController
) : ProtectionStateManager {

    override val stateFlow: Flow<UnblockSessionState> = unblockSessionManager.sessionStateFlow

    override suspend fun enableProtection(context: Context): Boolean {
        if (!vpnController.isVpnPermissionGranted(context)) {
            return false
        }
        vpnController.startProtectionService(context)
        unblockSessionManager.activateProtection()
        return true
    }

    override suspend fun requestUnblock(): UnblockSessionState {
        return unblockSessionManager.requestUnblock()
    }

    override suspend fun startHour(): UnblockSessionState {
        return unblockSessionManager.startCurrentHour()
    }

    override suspend fun cancelUnblock(): UnblockSessionState {
        return unblockSessionManager.cancelUnblock()
    }

    override suspend fun finalizeUnblock(context: Context): Boolean {
        val updated = unblockSessionManager.completeUnblockAndTurnOff()
        if (updated.status == ProtectionStatus.PROTECTION_OFF) {
            vpnController.stopProtectionService(context)
            return true
        }
        return false
    }

    override suspend fun syncState(now: Long): UnblockSessionState {
        return unblockSessionManager.syncTimerState(now)
    }
}
