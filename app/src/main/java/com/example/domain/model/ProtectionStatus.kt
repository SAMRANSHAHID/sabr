package com.example.domain.model

/**
 * Exact state machine enum representing the protection and unblock lifecycle:
 *
 * 1. PROTECTION_OFF: Initial state or user-disabled. Nothing blocked.
 * 2. PROTECTION_ACTIVE: Native VpnService active, filtering adult domains.
 * 3. UNBLOCK_REQUESTED: User initiated unblock flow; reflection/warning screen presented.
 * 4. HOUR_RUNNING: Current 60-minute stage is ticking down.
 * 5. HOUR_COMPLETE: Current hour finished; timer paused. Awaits manual user resumption.
 * 6. UNBLOCK_AVAILABLE: All 24 separate stages completed. User may finally disable protection.
 */
enum class ProtectionStatus {
    PROTECTION_OFF,
    PROTECTION_ACTIVE,
    UNBLOCK_REQUESTED,
    HOUR_RUNNING,
    HOUR_COMPLETE,
    UNBLOCK_AVAILABLE;

    val isProtectionEnabled: Boolean
        get() = this != PROTECTION_OFF
}
