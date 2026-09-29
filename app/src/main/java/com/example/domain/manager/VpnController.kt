package com.example.domain.manager

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import com.example.service.ProtectionService

interface VpnController {
    fun isVpnPermissionGranted(context: Context): Boolean
    fun getVpnPrepareIntent(context: Context): Intent?
    fun startProtectionService(context: Context)
    fun stopProtectionService(context: Context)
}

class VpnControllerImpl : VpnController {

    override fun isVpnPermissionGranted(context: Context): Boolean {
        return VpnService.prepare(context) == null
    }

    override fun getVpnPrepareIntent(context: Context): Intent? {
        return VpnService.prepare(context)
    }

    override fun startProtectionService(context: Context) {
        val intent = Intent(context, ProtectionService::class.java).apply {
            action = ProtectionService.ACTION_START_PROTECTION
        }
        try {
            ContextCompat.startForegroundService(context, intent)
        } catch (_: Exception) {
            context.startService(intent)
        }
    }

    override fun stopProtectionService(context: Context) {
        val intent = Intent(context, ProtectionService::class.java).apply {
            action = ProtectionService.ACTION_STOP_PROTECTION
        }
        context.startService(intent)
    }
}
