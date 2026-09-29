package com.example.core.dns

import java.nio.ByteBuffer

/**
 * Handles raw IPv4 and UDP packet parsing and assembly for the Android TUN interface.
 */
data class ParsedUdpPacket(
    val version: Int,
    val sourceIp: ByteArray,
    val destIp: ByteArray,
    val sourcePort: Int,
    val destPort: Int,
    val udpPayload: ByteArray,
    val rawPacket: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ParsedUdpPacket
        return sourcePort == other.sourcePort &&
                destPort == other.destPort &&
                sourceIp.contentEquals(other.sourceIp) &&
                destIp.contentEquals(other.destIp) &&
                udpPayload.contentEquals(other.udpPayload)
    }

    override fun hashCode(): Int {
        var result = sourcePort
        result = 31 * result + destPort
        result = 31 * result + sourceIp.contentHashCode()
        result = 31 * result + destIp.contentHashCode()
        result = 31 * result + udpPayload.contentHashCode()
        return result
    }

    companion object {
        private const val PROTOCOL_UDP = 17

        fun parse(packet: ByteArray, length: Int): ParsedUdpPacket? {
            if (length < 28) return null // Minimum IPv4 header (20) + UDP header (8)

            val versionAndIhl = packet[0].toInt() and 0xFF
            val version = versionAndIhl shr 4
            if (version != 4) return null // Only IPv4 handled for local DNS sinkhole

            val ihl = (versionAndIhl and 0x0F) * 4
            if (ihl < 20 || length < ihl + 8) return null

            val protocol = packet[9].toInt() and 0xFF
            if (protocol != PROTOCOL_UDP) return null

            val sourceIp = ByteArray(4)
            val destIp = ByteArray(4)
            System.arraycopy(packet, 12, sourceIp, 0, 4)
            System.arraycopy(packet, 16, destIp, 0, 4)

            val udpOffset = ihl
            val buffer = ByteBuffer.wrap(packet, udpOffset, length - udpOffset)
            val sourcePort = buffer.short.toInt() and 0xFFFF
            val destPort = buffer.short.toInt() and 0xFFFF
            val udpLength = buffer.short.toInt() and 0xFFFF
            buffer.short // udpChecksum

            val payloadLength = udpLength - 8
            if (payloadLength < 0 || buffer.remaining() < payloadLength) return null

            val payload = ByteArray(payloadLength)
            buffer.get(payload)

            val copy = ByteArray(length)
            System.arraycopy(packet, 0, copy, 0, length)

            return ParsedUdpPacket(
                version = version,
                sourceIp = sourceIp,
                destIp = destIp,
                sourcePort = sourcePort,
                destPort = destPort,
                udpPayload = payload,
                rawPacket = copy
            )
        }

        /**
         * Assembles a response IPv4 + UDP packet with reversed source/dest IP and ports.
         */
        fun buildUdpResponsePacket(
            request: ParsedUdpPacket,
            newUdpPayload: ByteArray
        ): ByteArray {
            val ipHeaderLength = 20
            val udpHeaderLength = 8
            val totalLength = ipHeaderLength + udpHeaderLength + newUdpPayload.size
            val packet = ByteArray(totalLength)
            val buffer = ByteBuffer.wrap(packet)

            // IPv4 Header
            buffer.put(0x45.toByte()) // Version 4, IHL 5 (20 bytes)
            buffer.put(0x00.toByte()) // DSCP / ECN
            buffer.putShort(totalLength.toShort()) // Total Length
            buffer.putShort(0.toShort()) // Identification
            buffer.putShort(0x4000.toShort()) // Flags (Don't Fragment)
            buffer.put(64.toByte()) // TTL
            buffer.put(PROTOCOL_UDP.toByte()) // Protocol (UDP = 17)
            buffer.putShort(0.toShort()) // Checksum placeholder

            // Invert source and dest IPs: source becomes the DNS server IP that was queried
            buffer.put(request.destIp)
            buffer.put(request.sourceIp)

            // Calculate IPv4 Header Checksum
            val ipChecksum = computeIpChecksum(packet, 0, ipHeaderLength)
            buffer.putShort(10, ipChecksum.toShort())

            // UDP Header
            buffer.position(ipHeaderLength)
            buffer.putShort(request.destPort.toShort()) // Source Port (53)
            buffer.putShort(request.sourcePort.toShort()) // Destination Port (client)
            val udpTotalLength = udpHeaderLength + newUdpPayload.size
            buffer.putShort(udpTotalLength.toShort())
            buffer.putShort(0.toShort()) // Checksum (0 is valid in IPv4 UDP)

            // UDP Payload
            buffer.put(newUdpPayload)

            return packet
        }

        private fun computeIpChecksum(data: ByteArray, offset: Int, length: Int): Int {
            var sum = 0
            var i = offset
            while (i < offset + length) {
                val byte1 = data[i].toInt() and 0xFF
                val byte2 = if (i + 1 < offset + length) data[i + 1].toInt() and 0xFF else 0
                val word = (byte1 shl 8) or byte2
                sum += word
                if (sum > 0xFFFF) {
                    sum = (sum and 0xFFFF) + (sum shr 16)
                }
                i += 2
            }
            return sum.inv() and 0xFFFF
        }
    }
}
