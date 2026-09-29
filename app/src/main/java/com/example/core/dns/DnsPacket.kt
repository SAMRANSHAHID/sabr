package com.example.core.dns

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

/**
 * Represents a DNS Question and minimal packet structure for DNS interception.
 */
data class DnsQuestion(
    val qName: String,
    val qType: Int,
    val qClass: Int
)

data class DnsPacket(
    val transactionId: Short,
    val isResponse: Boolean,
    val opCode: Int,
    val isAuthoritative: Boolean,
    val isTruncated: Boolean,
    val recursionDesired: Boolean,
    val recursionAvailable: Boolean,
    val responseCode: Int,
    val questions: List<DnsQuestion>,
    val rawBytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DnsPacket
        return transactionId == other.transactionId && rawBytes.contentEquals(other.rawBytes)
    }

    override fun hashCode(): Int {
        var result = transactionId.toInt()
        result = 31 * result + rawBytes.contentHashCode()
        return result
    }

    companion object {
        const val RCODE_NOERROR = 0
        const val RCODE_FORMERR = 1
        const val RCODE_SERVFAIL = 2
        const val RCODE_NXDOMAIN = 3
        const val RCODE_REFUSED = 5

        const val TYPE_A = 1
        const val TYPE_AAAA = 28

        /**
         * Parses a DNS query packet from raw UDP payload.
         */
        fun parse(data: ByteArray, offset: Int = 0, length: Int = data.size): DnsPacket? {
            if (length < 12) return null
            val buffer = ByteBuffer.wrap(data, offset, length)

            val id = buffer.short
            val flags = buffer.short.toInt() and 0xFFFF

            val isResponse = (flags and 0x8000) != 0
            val opCode = (flags shr 11) and 0x0F
            val isAuthoritative = (flags and 0x0400) != 0
            val isTruncated = (flags and 0x0200) != 0
            val recursionDesired = (flags and 0x0100) != 0
            val recursionAvailable = (flags and 0x0080) != 0
            val responseCode = flags and 0x000F

            val qdCount = buffer.short.toInt() and 0xFFFF
            buffer.short // anCount
            buffer.short // nsCount
            buffer.short // arCount

            val questions = mutableListOf<DnsQuestion>()
            for (i in 0 until qdCount) {
                val qName = parseDomainName(buffer) ?: return null
                if (buffer.remaining() < 4) return null
                val qType = buffer.short.toInt() and 0xFFFF
                val qClass = buffer.short.toInt() and 0xFFFF
                questions.add(DnsQuestion(qName, qType, qClass))
            }

            val rawCopy = ByteArray(length)
            System.arraycopy(data, offset, rawCopy, 0, length)

            return DnsPacket(
                transactionId = id,
                isResponse = isResponse,
                opCode = opCode,
                isAuthoritative = isAuthoritative,
                isTruncated = isTruncated,
                recursionDesired = recursionDesired,
                recursionAvailable = recursionAvailable,
                responseCode = responseCode,
                questions = questions,
                rawBytes = rawCopy
            )
        }

        private fun parseDomainName(buffer: ByteBuffer): String? {
            val sb = java.lang.StringBuilder()
            var jumped = false
            var originalPosition = -1
            var hops = 0

            while (buffer.hasRemaining()) {
                val len = buffer.get().toInt() and 0xFF
                if (len == 0) {
                    break
                }

                // Pointer compression check (0xC0)
                if ((len and 0xC0) == 0xC0) {
                    if (!buffer.hasRemaining()) return null
                    val b2 = buffer.get().toInt() and 0xFF
                    val pointer = ((len and 0x3F) shl 8) or b2
                    if (!jumped) {
                        originalPosition = buffer.position()
                        jumped = true
                    }
                    if (++hops > 10 || pointer >= buffer.limit()) return null
                    buffer.position(pointer)
                    continue
                }

                if (buffer.remaining() < len) return null
                val label = ByteArray(len)
                buffer.get(label)
                if (sb.isNotEmpty()) sb.append('.')
                sb.append(String(label, StandardCharsets.US_ASCII))
            }

            if (jumped && originalPosition != -1) {
                buffer.position(originalPosition)
            }

            return sb.toString().lowercase()
        }

        /**
         * Builds a blocked DNS response (either NXDOMAIN or 0.0.0.0 Sinkhole).
         */
        fun buildBlockedResponse(
            queryPacket: DnsPacket,
            sinkholeIp: ByteArray = byteArrayOf(0, 0, 0, 0)
        ): ByteArray {
            val buffer = ByteBuffer.allocate(512)

            // Header (12 bytes)
            buffer.putShort(queryPacket.transactionId)

            // Flags: Response (0x8000), Authoritative (0x0400), Recursion Desired (0x0100), Recursion Available (0x0080)
            // If sinkholeIp provided, return NOERROR with A record answer. Otherwise NXDOMAIN (0x0003).
            val rCode = if (sinkholeIp.size == 4) RCODE_NOERROR else RCODE_NXDOMAIN
            val flags = 0x8180 or (rCode and 0x000F)
            buffer.putShort(flags.toShort())

            val qCount = queryPacket.questions.size
            buffer.putShort(qCount.toShort()) // QDCOUNT
            buffer.putShort(if (rCode == RCODE_NOERROR) 1.toShort() else 0.toShort()) // ANCOUNT
            buffer.putShort(0.toShort()) // NSCOUNT
            buffer.putShort(0.toShort()) // ARCOUNT

            // Question section
            for (q in queryPacket.questions) {
                writeDomainName(buffer, q.qName)
                buffer.putShort(q.qType.toShort())
                buffer.putShort(q.qClass.toShort())
            }

            // Answer section (if sinkhole)
            if (rCode == RCODE_NOERROR && queryPacket.questions.isNotEmpty()) {
                val firstQ = queryPacket.questions.first()
                writeDomainName(buffer, firstQ.qName)
                buffer.putShort(TYPE_A.toShort()) // TYPE A
                buffer.putShort(1.toShort()) // CLASS IN
                buffer.putInt(300) // TTL 300 seconds
                buffer.putShort(4.toShort()) // RDLENGTH 4 bytes
                buffer.put(sinkholeIp)
            }

            val length = buffer.position()
            val result = ByteArray(length)
            System.arraycopy(buffer.array(), 0, result, 0, length)
            return result
        }

        private fun writeDomainName(buffer: ByteBuffer, domain: String) {
            val labels = domain.split('.')
            for (label in labels) {
                if (label.isEmpty()) continue
                val bytes = label.toByteArray(StandardCharsets.US_ASCII)
                buffer.put(bytes.size.toByte())
                buffer.put(bytes)
            }
            buffer.put(0.toByte())
        }
    }
}
