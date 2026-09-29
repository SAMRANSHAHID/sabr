package com.example.core.filter

import com.example.domain.model.FilterDecision

sealed class DnsFilterResult {
    data class Allowed(val decision: FilterDecision) : DnsFilterResult()
    data class Blocked(val decision: FilterDecision, val sinkholeIp: ByteArray = byteArrayOf(0, 0, 0, 0)) : DnsFilterResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Blocked
            return decision == other.decision && sinkholeIp.contentEquals(other.sinkholeIp)
        }

        override fun hashCode(): Int {
            var result = decision.hashCode()
            result = 31 * result + sinkholeIp.contentHashCode()
            return result
        }
    }
    data class SafeSearchRedirect(val decision: FilterDecision, val targetIp: ByteArray) : DnsFilterResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as SafeSearchRedirect
            return decision == other.decision && targetIp.contentEquals(other.targetIp)
        }

        override fun hashCode(): Int {
            var result = decision.hashCode()
            result = 31 * result + targetIp.contentHashCode()
            return result
        }
    }
}

interface DnsFilter {
    fun evaluate(domain: String): DnsFilterResult
    fun setSafeSearchEnabled(enabled: Boolean)
}

class DnsFilterImpl(
    private val domainClassifier: DomainClassifier,
    @Volatile private var safeSearchEnabled: Boolean = true
) : DnsFilter {

    // Known static SafeSearch IPs
    private val googleSafeSearchIp = byteArrayOf(216.toByte(), 239.toByte(), 38.toByte(), 120.toByte())
    private val bingSafeSearchIp = byteArrayOf(204.toByte(), 79.toByte(), 197.toByte(), 220.toByte())

    override fun evaluate(domain: String): DnsFilterResult {
        val cleanDomain = domain.trim().lowercase().removeSuffix(".")

        // 1. SafeSearch enforcement if enabled
        if (safeSearchEnabled) {
            if (isGoogleSearch(cleanDomain)) {
                val decision = domainClassifier.classify(cleanDomain)
                return DnsFilterResult.SafeSearchRedirect(decision, googleSafeSearchIp)
            }
            if (isBingSearch(cleanDomain)) {
                val decision = domainClassifier.classify(cleanDomain)
                return DnsFilterResult.SafeSearchRedirect(decision, bingSafeSearchIp)
            }
        }

        // 2. Domain classification
        val decision = domainClassifier.classify(cleanDomain)
        return if (decision.isAllowed) {
            DnsFilterResult.Allowed(decision)
        } else {
            DnsFilterResult.Blocked(decision)
        }
    }

    override fun setSafeSearchEnabled(enabled: Boolean) {
        safeSearchEnabled = enabled
    }

    private fun isGoogleSearch(domain: String): Boolean {
        return domain == "google.com" ||
                domain == "www.google.com" ||
                domain.startsWith("google.") ||
                domain.startsWith("www.google.")
    }

    private fun isBingSearch(domain: String): Boolean {
        return domain == "bing.com" || domain == "www.bing.com"
    }
}
