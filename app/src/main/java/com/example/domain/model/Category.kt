package com.example.domain.model

enum class Category(val displayName: String, val description: String) {
    ADULT("Adult & Pornography", "Explicit adult content, pornographic websites, and tube platforms"),
    EXPLICIT_MEDIA("Explicit Media & Cams", "Live webcams, sexually explicit streaming, and erotic media"),
    DATING_HOOKUP("Hookup & Adult Dating", "Casual adult encounters and sexually oriented dating networks"),
    GAMBLING("Gambling & Betting", "Online casinos, sports betting, and addictive wagering portals"),
    MALICIOUS("Malware & Phishing", "Known malicious, tracking, and deceptive domains"),
    SAFE("Safe & Allowed", "Legitimate, safe, and family-appropriate services"),
    CUSTOM_BLOCKED("Custom Blocklist", "Manually restricted by the user"),
    CUSTOM_ALLOWED("Custom Allowlist", "Explicitly whitelisted by the user")
}

data class FilterDecision(
    val isAllowed: Boolean,
    val domain: String,
    val category: Category,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
