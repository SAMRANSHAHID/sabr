package com.example.core.filter

import com.example.data.local.CustomRuleDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

interface AllowlistManager {
    fun isAllowlisted(domain: String): Boolean
    fun addDomain(domain: String)
    fun removeDomain(domain: String)
}

class AllowlistManagerImpl(
    private val customRuleDao: CustomRuleDao?,
    externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AllowlistManager {

    private val memoryAllowlist = ConcurrentHashMap.newKeySet<String>()

    init {
        // Seed with built-in essential domains
        memoryAllowlist.addAll(BuiltInBlocklists.ESSENTIAL_ALLOWLIST)

        // Observe custom database allowlist updates
        customRuleDao?.let { dao ->
            externalScope.launch {
                dao.getAllowedRules().collectLatest { rules ->
                    val userAllowed = rules.map { it.domain.lowercase().trim() }
                    memoryAllowlist.clear()
                    memoryAllowlist.addAll(BuiltInBlocklists.ESSENTIAL_ALLOWLIST)
                    memoryAllowlist.addAll(userAllowed)
                }
            }
        }
    }

    override fun isAllowlisted(domain: String): Boolean {
        val clean = domain.trim().lowercase().removeSuffix(".")
        if (clean.isEmpty()) return true

        // Direct match
        if (memoryAllowlist.contains(clean)) return true

        // Subdomain match: e.g. api.github.com matches github.com
        val parts = clean.split('.')
        for (i in 1 until parts.size) {
            val root = parts.subList(i, parts.size).joinToString(".")
            if (memoryAllowlist.contains(root)) {
                return true
            }
        }

        return false
    }

    override fun addDomain(domain: String) {
        val clean = domain.trim().lowercase().removeSuffix(".")
        if (clean.isNotEmpty()) {
            memoryAllowlist.add(clean)
        }
    }

    override fun removeDomain(domain: String) {
        val clean = domain.trim().lowercase().removeSuffix(".")
        // Do not remove core essential domains
        if (!BuiltInBlocklists.ESSENTIAL_ALLOWLIST.contains(clean)) {
            memoryAllowlist.remove(clean)
        }
    }
}
