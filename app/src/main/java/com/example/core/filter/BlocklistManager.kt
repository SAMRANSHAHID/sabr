package com.example.core.filter

import com.example.data.local.CustomRuleDao
import com.example.domain.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

interface BlocklistManager {
    fun isBlocklisted(domain: String): Pair<Boolean, Category?>
    fun addDomain(domain: String, category: Category = Category.CUSTOM_BLOCKED)
    fun removeDomain(domain: String)
    fun setGamblingBlockingEnabled(enabled: Boolean)
    fun setDatingBlockingEnabled(enabled: Boolean)
    val version: Long
}

class BlocklistManagerImpl(
    private val customRuleDao: CustomRuleDao?,
    externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : BlocklistManager {

    override val version: Long = BuiltInBlocklists.BLOCKLIST_VERSION

    private val adultDomains = ConcurrentHashMap.newKeySet<String>()
    private val customBlockedDomains = ConcurrentHashMap<String, Category>()
    @Volatile private var blockGambling: Boolean = true
    @Volatile private var blockDating: Boolean = true

    private val gamblingDomains = setOf(
        "bet365.com", "bovada.lv", "draftkings.com", "fanduel.com", "pokerstars.com",
        "888casino.com", "stake.com", "roobet.com", "williamhill.com", "betway.com",
        "1xbet.com", "unibet.com", "betfair.com", "paddypower.com", "ladbrokes.com"
    )

    private val datingDomains = setOf(
        "tinder.com", "badoo.com", "adultfriendfinder.com", "ashleymadison.com",
        "fling.com", "fetlife.com", "flirt.com", "alt.com", "passion.com"
    )

    init {
        // Seed default adult domains
        adultDomains.addAll(BuiltInBlocklists.ADULT_DOMAINS)

        // Observe custom database blocklist rules
        customRuleDao?.let { dao ->
            externalScope.launch {
                dao.getBlockedRules().collectLatest { rules ->
                    customBlockedDomains.clear()
                    rules.forEach { rule ->
                        val cat = try {
                            Category.valueOf(rule.category)
                        } catch (_: Exception) {
                            Category.CUSTOM_BLOCKED
                        }
                        customBlockedDomains[rule.domain.lowercase().trim()] = cat
                    }
                }
            }
        }
    }

    override fun isBlocklisted(domain: String): Pair<Boolean, Category?> {
        val clean = domain.trim().lowercase().removeSuffix(".")
        if (clean.isEmpty()) return Pair(false, null)

        // 1. Check custom user blocklist first
        customBlockedDomains[clean]?.let { return Pair(true, it) }

        // 2. Check dedicated Adult Top-Level Domains (.xxx, .porn, .adult, .sex, .cam)
        val tld = clean.substringAfterLast('.', "")
        if (BuiltInBlocklists.ADULT_TLDS.contains(tld)) {
            return Pair(true, Category.ADULT)
        }

        // 3. Check exact domain or subdomain match against Adult Database
        if (isDomainOrSubdomainInSet(clean, adultDomains)) {
            return Pair(true, Category.ADULT)
        }

        // 4. Check category rules: Gambling
        if (blockGambling && isDomainOrSubdomainInSet(clean, gamblingDomains)) {
            return Pair(true, Category.GAMBLING)
        }

        // 5. Check category rules: Dating
        if (blockDating && isDomainOrSubdomainInSet(clean, datingDomains)) {
            return Pair(true, Category.DATING_HOOKUP)
        }

        // 6. Check high-confidence adult keyword tokens
        val tokens = clean.split('.', '-')
        for (token in tokens) {
            if (BuiltInBlocklists.ADULT_KEYWORD_TOKENS.contains(token)) {
                return Pair(true, Category.ADULT)
            }
        }

        return Pair(false, null)
    }

    private fun isDomainOrSubdomainInSet(cleanDomain: String, domainSet: Set<String>): Boolean {
        if (domainSet.contains(cleanDomain)) return true

        val parts = cleanDomain.split('.')
        for (i in 1 until parts.size) {
            val root = parts.subList(i, parts.size).joinToString(".")
            if (domainSet.contains(root)) {
                return true
            }
        }
        return false
    }

    override fun addDomain(domain: String, category: Category) {
        val clean = domain.trim().lowercase().removeSuffix(".")
        if (clean.isNotEmpty()) {
            customBlockedDomains[clean] = category
        }
    }

    override fun removeDomain(domain: String) {
        val clean = domain.trim().lowercase().removeSuffix(".")
        customBlockedDomains.remove(clean)
        adultDomains.remove(clean)
    }

    override fun setGamblingBlockingEnabled(enabled: Boolean) {
        blockGambling = enabled
    }

    override fun setDatingBlockingEnabled(enabled: Boolean) {
        blockDating = enabled
    }
}
