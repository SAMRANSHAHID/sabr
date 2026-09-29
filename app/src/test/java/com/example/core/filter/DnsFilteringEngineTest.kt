package com.example.core.filter

import com.example.core.dns.DnsPacket
import com.example.core.dns.ParsedUdpPacket
import com.example.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DnsFilteringEngineTest {

    private lateinit var allowlistManager: AllowlistManager
    private lateinit var blocklistManager: BlocklistManager
    private lateinit var domainClassifier: DomainClassifier
    private lateinit var dnsFilter: DnsFilter

    @Before
    fun setUp() {
        allowlistManager = AllowlistManagerImpl(customRuleDao = null)
        blocklistManager = BlocklistManagerImpl(customRuleDao = null)
        domainClassifier = DomainClassifierImpl(allowlistManager, blocklistManager)
        dnsFilter = DnsFilterImpl(domainClassifier)
    }

    @Test
    fun testNormalDomainAllowed() {
        // Normal legitimate domains should be ALLOWED
        val benignDomains = listOf(
            "wikipedia.org",
            "en.wikipedia.org",
            "github.com",
            "api.github.com",
            "khanacademy.org",
            "stackoverflow.com",
            "reuters.com",
            "weather.gov"
        )

        for (domain in benignDomains) {
            val decision = domainClassifier.classify(domain)
            assertTrue("Domain $domain should be allowed", decision.isAllowed)
            assertEquals(Category.SAFE, decision.category)
        }
    }

    @Test
    fun testAdultDomainBlocked() {
        // Known adult platforms must be BLOCKED
        val adultDomains = listOf(
            "pornhub.com",
            "www.pornhub.com",
            "cdn.pornhub.com",
            "xvideos.com",
            "sub.xvideos.com",
            "xnxx.com",
            "redtube.com",
            "chaturbate.com",
            "stripchat.com",
            "brazzers.com",
            "onlyfans.com"
        )

        for (domain in adultDomains) {
            val decision = domainClassifier.classify(domain)
            assertFalse("Adult domain $domain MUST be blocked", decision.isAllowed)
            assertEquals(Category.ADULT, decision.category)
        }
    }

    @Test
    fun testAdultTldBlocked() {
        // Dedicated adult TLDs (.xxx, .porn, .adult, .sex, .cam) must be BLOCKED
        val tldDomains = listOf(
            "example.xxx",
            "portal.porn",
            "stream.adult",
            "chat.sex",
            "live.cam"
        )

        for (domain in tldDomains) {
            val decision = domainClassifier.classify(domain)
            assertFalse("Domain with adult TLD $domain must be blocked", decision.isAllowed)
            assertEquals(Category.ADULT, decision.category)
        }
    }

    @Test
    fun testExplicitKeywordTokenBlocked() {
        val keywordDomains = listOf(
            "my-pornhub-archive.net",
            "mirror.xvideos.org",
            "private-stripchat.xyz"
        )

        for (domain in keywordDomains) {
            val decision = domainClassifier.classify(domain)
            assertFalse("Keyword domain $domain should be blocked", decision.isAllowed)
        }
    }

    @Test
    fun testCustomAllowlistPriority() {
        val targetDomain = "custom-educational-service.org"
        // Add to allowlist
        allowlistManager.addDomain(targetDomain)

        val decision = domainClassifier.classify(targetDomain)
        assertTrue(decision.isAllowed)
        assertEquals(Category.SAFE, decision.category)
    }

    @Test
    fun testCustomBlocklist() {
        val customBadDomain = "distracting-site.com"
        blocklistManager.addDomain(customBadDomain, Category.CUSTOM_BLOCKED)

        val decision = domainClassifier.classify(customBadDomain)
        assertFalse(decision.isAllowed)
        assertEquals(Category.CUSTOM_BLOCKED, decision.category)
    }

    @Test
    fun testSafeSearchRedirection() {
        dnsFilter.setSafeSearchEnabled(true)
        val resultGoogle = dnsFilter.evaluate("www.google.com")
        assertTrue("Google should trigger safe search rewrite", resultGoogle is DnsFilterResult.SafeSearchRedirect)

        val resultBing = dnsFilter.evaluate("bing.com")
        assertTrue("Bing should trigger safe search rewrite", resultBing is DnsFilterResult.SafeSearchRedirect)
    }

    @Test
    fun testDnsPacketParsingAndBlockedResponseGeneration() {
        // Construct a mock DNS query for "pornhub.com"
        val queryBuffer = java.nio.ByteBuffer.allocate(64)
        queryBuffer.putShort(0x1234.toShort()) // ID
        queryBuffer.putShort(0x0100.toShort()) // Standard query, recursion desired
        queryBuffer.putShort(1.toShort()) // QDCOUNT = 1
        queryBuffer.putShort(0.toShort()) // ANCOUNT = 0
        queryBuffer.putShort(0.toShort()) // NSCOUNT = 0
        queryBuffer.putShort(0.toShort()) // ARCOUNT = 0

        // QNAME: 7 pornhub 3 com 0
        queryBuffer.put(7.toByte())
        queryBuffer.put("pornhub".toByteArray())
        queryBuffer.put(3.toByte())
        queryBuffer.put("com".toByteArray())
        queryBuffer.put(0.toByte())

        queryBuffer.putShort(1.toShort()) // QTYPE A
        queryBuffer.putShort(1.toShort()) // QCLASS IN

        val queryBytes = queryBuffer.array().copyOf(queryBuffer.position())
        val parsed = DnsPacket.parse(queryBytes)

        assertNotNull(parsed)
        assertEquals(0x1234.toShort(), parsed!!.transactionId)
        assertEquals("pornhub.com", parsed.questions.first().qName)

        // Build blocked response
        val sinkhole = byteArrayOf(0, 0, 0, 0)
        val blockedResponse = DnsPacket.buildBlockedResponse(parsed, sinkhole)
        assertNotNull(blockedResponse)
        assertTrue(blockedResponse.size > 12)

        // Verify transaction ID matches
        val responseBuffer = java.nio.ByteBuffer.wrap(blockedResponse)
        assertEquals(0x1234.toShort(), responseBuffer.short)
    }

    @Test
    fun testIpPacketAssembly() {
        // Mock a 28-byte minimal IPv4 UDP packet
        val ipPacket = ByteArray(28 + 12)
        ipPacket[0] = 0x45.toByte() // Version 4, IHL 5
        ipPacket[9] = 17.toByte() // UDP
        // Source IP: 10.254.1.1
        ipPacket[12] = 10.toByte()
        ipPacket[13] = 254.toByte()
        ipPacket[14] = 1.toByte()
        ipPacket[15] = 1.toByte()
        // Dest IP: 10.254.1.2
        ipPacket[16] = 10.toByte()
        ipPacket[17] = 254.toByte()
        ipPacket[18] = 1.toByte()
        ipPacket[19] = 2.toByte()

        // UDP Header at offset 20
        val udpOffset = 20
        val udpBuffer = java.nio.ByteBuffer.wrap(ipPacket, udpOffset, 20)
        udpBuffer.putShort(54321.toShort()) // client source port
        udpBuffer.putShort(53.toShort()) // DNS dest port
        udpBuffer.putShort(20.toShort()) // length (8 + 12 payload)
        udpBuffer.putShort(0.toShort()) // checksum

        val parsed = ParsedUdpPacket.parse(ipPacket, ipPacket.size)
        assertNotNull(parsed)
        assertEquals(54321, parsed!!.sourcePort)
        assertEquals(53, parsed.destPort)

        // Build response
        val mockResponsePayload = ByteArray(16)
        val assembled = ParsedUdpPacket.buildUdpResponsePacket(parsed, mockResponsePayload)

        assertNotNull(assembled)
        val assembledParsed = ParsedUdpPacket.parse(assembled, assembled.size)
        assertNotNull(assembledParsed)
        // Reversed ports
        assertEquals(53, assembledParsed!!.sourcePort)
        assertEquals(54321, assembledParsed.destPort)
    }
}
