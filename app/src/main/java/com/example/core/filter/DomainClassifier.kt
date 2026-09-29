package com.example.core.filter

import com.example.domain.model.Category
import com.example.domain.model.FilterDecision

interface DomainClassifier {
    fun classify(domain: String): FilterDecision
}

class DomainClassifierImpl(
    private val allowlistManager: AllowlistManager,
    private val blocklistManager: BlocklistManager
) : DomainClassifier {

    override fun classify(domain: String): FilterDecision {
        val cleanDomain = domain.trim().lowercase().removeSuffix(".")

        // 1. Allowlist has absolute priority
        if (allowlistManager.isAllowlisted(cleanDomain)) {
            return FilterDecision(
                isAllowed = true,
                domain = cleanDomain,
                category = Category.SAFE,
                reason = "Domain is present in the allowlist"
            )
        }

        // 2. Blocklist evaluation
        val (isBlocked, category) = blocklistManager.isBlocklisted(cleanDomain)
        if (isBlocked && category != null) {
            return FilterDecision(
                isAllowed = false,
                domain = cleanDomain,
                category = category,
                reason = "Matched category '${category.displayName}'"
            )
        }

        // 3. Default allow
        return FilterDecision(
            isAllowed = true,
            domain = cleanDomain,
            category = Category.SAFE,
            reason = "Standard web traffic"
        )
    }
}
