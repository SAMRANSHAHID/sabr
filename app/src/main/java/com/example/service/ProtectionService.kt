package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SabrApplication
import com.example.core.dns.DnsPacket
import com.example.core.dns.ParsedUdpPacket
import com.example.core.filter.DnsFilterResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

class ProtectionService : VpnService() {

    companion object {
        const val ACTION_START_PROTECTION = "com.example.sabr.START_PROTECTION"
        const val ACTION_STOP_PROTECTION = "com.example.sabr.STOP_PROTECTION"
        private const val NOTIFICATION_CHANNEL_ID = "sabr_protection_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "SabrProtectionService"

        private const val TUN_IP = "10.254.1.1"
        private const val VIRTUAL_DNS_IP = "10.254.1.2"
        private const val UPSTREAM_FAMILY_DNS = "1.1.1.3" // Cloudflare Family DNS (Blocks malware & adult)
        private const val UPSTREAM_FALLBACK_DNS = "1.0.0.3"
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var workerJob: Job? = null
    private val isRunning = AtomicBoolean(false)
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerNetworkMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_PROTECTION) {
            stopVpnService()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        if (!isRunning.get()) {
            startVpnFiltering()
        }
        return START_STICKY
    }

    private fun startVpnFiltering() {
        try {
            val builder = Builder()
                .setSession(getString(R.string.app_name))
                .setMtu(1500)
                .addAddress(TUN_IP, 32)
                .addDnsServer(VIRTUAL_DNS_IP)
                .addRoute(VIRTUAL_DNS_IP, 32)

            vpnInterface = builder.establish()
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface. Builder returned null.")
                stopSelf()
                return
            }

            isRunning.set(true)
            workerJob = serviceScope.launch {
                runPacketLoop()
            }
            Log.i(TAG, "Sabr VPN filtering engine active.")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN interface", e)
            stopSelf()
        }
    }

    private fun runPacketLoop() {
        val fd = vpnInterface?.fileDescriptor ?: return
        val inputStream = FileInputStream(fd)
        val outputStream = FileOutputStream(fd)
        val packetBuffer = ByteArray(4096)

        val app = applicationContext as? SabrApplication
        val dnsFilter = app?.dnsFilter
        val repository = app?.repository

        // Upstream socket for forwarding allowed queries
        val upstreamSocket = DatagramSocket()
        protect(upstreamSocket)
        upstreamSocket.soTimeout = 2500

        try {
            while (isRunning.get() && serviceScope.isActive) {
                val bytesRead = try {
                    inputStream.read(packetBuffer)
                } catch (e: IOException) {
                    if (!isRunning.get()) break
                    continue
                }

                if (bytesRead <= 0) continue

                val udpPacket = ParsedUdpPacket.parse(packetBuffer, bytesRead) ?: continue
                if (udpPacket.destPort != 53) continue

                val dnsQuery = DnsPacket.parse(udpPacket.udpPayload) ?: continue
                if (dnsQuery.questions.isEmpty()) continue

                val targetDomain = dnsQuery.questions.first().qName
                val filterResult = dnsFilter?.evaluate(targetDomain)

                when (filterResult) {
                    is DnsFilterResult.Blocked -> {
                        // Synthesize blocked response (NXDOMAIN / Sinkhole 0.0.0.0)
                        val blockedDnsPayload = DnsPacket.buildBlockedResponse(dnsQuery, filterResult.sinkholeIp)
                        val responsePacket = ParsedUdpPacket.buildUdpResponsePacket(udpPacket, blockedDnsPayload)
                        try {
                            outputStream.write(responsePacket)
                            outputStream.flush()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error writing blocked response to TUN", e)
                        }

                        // Record blocked log
                        serviceScope.launch {
                            repository?.logFilterEvent(
                                domain = targetDomain,
                                isBlocked = true,
                                category = filterResult.decision.category.displayName
                            )
                        }
                    }

                    is DnsFilterResult.SafeSearchRedirect -> {
                        val redirectDnsPayload = DnsPacket.buildBlockedResponse(dnsQuery, filterResult.targetIp)
                        val responsePacket = ParsedUdpPacket.buildUdpResponsePacket(udpPacket, redirectDnsPayload)
                        try {
                            outputStream.write(responsePacket)
                            outputStream.flush()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error writing safe search response", e)
                        }
                    }

                    is DnsFilterResult.Allowed, null -> {
                        // Forward query to upstream secure DNS and return answer
                        try {
                            val upstreamIp = InetAddress.getByName(UPSTREAM_FAMILY_DNS)
                            val outPacket = DatagramPacket(udpPacket.udpPayload, udpPacket.udpPayload.size, upstreamIp, 53)
                            upstreamSocket.send(outPacket)

                            val inBuffer = ByteArray(2048)
                            val inPacket = DatagramPacket(inBuffer, inBuffer.size)
                            upstreamSocket.receive(inPacket)

                            val receivedDnsPayload = ByteArray(inPacket.length)
                            System.arraycopy(inBuffer, 0, receivedDnsPayload, 0, inPacket.length)

                            val responsePacket = ParsedUdpPacket.buildUdpResponsePacket(udpPacket, receivedDnsPayload)
                            outputStream.write(responsePacket)
                            outputStream.flush()
                        } catch (e: Exception) {
                            // If primary upstream times out, try fallback
                            try {
                                val fallbackIp = InetAddress.getByName(UPSTREAM_FALLBACK_DNS)
                                val outPacket = DatagramPacket(udpPacket.udpPayload, udpPacket.udpPayload.size, fallbackIp, 53)
                                upstreamSocket.send(outPacket)

                                val inBuffer = ByteArray(2048)
                                val inPacket = DatagramPacket(inBuffer, inBuffer.size)
                                upstreamSocket.receive(inPacket)

                                val receivedDnsPayload = ByteArray(inPacket.length)
                                System.arraycopy(inBuffer, 0, receivedDnsPayload, 0, inPacket.length)

                                val responsePacket = ParsedUdpPacket.buildUdpResponsePacket(udpPacket, receivedDnsPayload)
                                outputStream.write(responsePacket)
                                outputStream.flush()
                            } catch (_: Exception) {
                                // Upstream timed out
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Packet processing loop encountered exception", e)
        } finally {
            try {
                upstreamSocket.close()
                inputStream.close()
                outputStream.close()
            } catch (_: Exception) {}
        }
    }

    private fun registerNetworkMonitoring() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Underlying network available: $network")
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Underlying network lost: $network")
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        networkCallback?.let { connectivityManager?.registerNetworkCallback(request, it) }
    }

    private fun stopVpnService() {
        isRunning.set(false)
        workerJob?.cancel()
        try {
            vpnInterface?.close()
        } catch (_: Exception) {}
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpnService()
        networkCallback?.let {
            try {
                connectivityManager?.unregisterNetworkCallback(it)
            } catch (_: Exception) {}
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Sabr Protection Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time active status of Sabr native content filtering"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Sabr Protection ACTIVE")
            .setContentText("Device-level adult content and explicit threat filtering enabled")
            .setSmallIcon(R.drawable.ic_sabr_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
