package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.SabrApplication
import com.example.domain.model.ProtectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as? SabrApplication ?: return
            val repository = app.repository
            val vpnController = app.vpnController

            CoroutineScope(Dispatchers.IO).launch {
                val state = repository.sessionState.first()
                // If protection was enabled prior to reboot, restore the protection service
                if (state.status.isProtectionEnabled && vpnController.isVpnPermissionGranted(context)) {
                    vpnController.startProtectionService(context)
                }
            }
        }
    }
}
