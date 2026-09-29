# Sabr (صبر) — Native Android Adult Content Protection & Mindful Focus Shield

[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-3DDC84.svg?style=flat&logo=android)](https://www.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-34A853.svg?style=flat)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20MVVM-FF6F00.svg?style=flat)](https://developer.android.com/topic/architecture)
[![Network Security](https://img.shields.io/badge/Engine-Android%20VpnService-0B2545.svg?style=flat)](https://developer.android.com/reference/android/net/VpnService)
[![Tests](https://img.shields.io/badge/Tests-Robolectric%20%26%20JUnit4%20Passing-10B981.svg?style=flat)]()
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20On--Device-10B981.svg?style=flat)]()

---

## Table of Contents
- [About the App](#about-the-app)
- [Core Features](#core-features)
- [Architecture](#architecture)
  - [MVVM & Clean Architecture Approach](#mvvm--clean-architecture-approach)
  - [The 24-Hour Deliberate Cooldown State Machine](#the-24-hour-deliberate-cooldown-state-machine)
  - [Selective Packet Interception Pipeline](#selective-packet-interception-pipeline)
  - [Package Structure](#package-structure)
- [Security Protocols](#security-protocols)
- [How to Build](#how-to-build)
  - [Prerequisites](#prerequisites)
  - [Build Commands](#build-commands)
  - [Running Automated Tests](#running-automated-tests)
- [License](#license)

---

## About the App

**Sabr** (*Arabic: صَبْر — Patience, Steadfastness, Self-Restraint*) is a production-grade Android application built to provide device-level protection against adult and sexually explicit web content.

Most content blockers rely on superficial browser overlays, custom webview simulations, or restrictive third-party proxy tunnels that compromise user privacy and break on mobile data. **Sabr is different**: it operates natively at the Android OS network layer using a high-efficiency `VpnService` loopback interface.

Sabr pairs zero-tolerance, low-latency adult domain sinkholing with a **24-stage deliberate cooldown state machine**. By demanding intentional, multi-stage confirmation periods rather than instant toggles, Sabr introduces healthy behavioral friction that halts impulsive behavior and reinforces long-term self-mastery.

---

## Core Features

### 1. Real Device-Level `VpnService` Filtering
- **Local TUN Interface**: Uses Android's native `VpnService` to bind a local virtual network interface (`10.254.1.1/32`) and establish a local virtual DNS server (`10.254.1.2/32`).
- **Selective Port 53 Interception**: Only UDP port 53 packets are captured and analyzed. Regular HTTP/HTTPS payloads, video streams, calls, and app traffic bypass the TUN interface completely, ensuring **zero speed loss, zero battery drain, and zero TLS certificate complications**.
- **Instant In-Memory Sinkhole (`0.0.0.0` / `NXDOMAIN`)**: When an adult domain is queried, Sabr synthesizes an immediate DNS response on-device. The browser receives an instant `ERR_NAME_NOT_RESOLVED` without any request ever reaching an external adult server.
- **Upstream Defense-in-Depth**: Clean queries are forwarded to Cloudflare Family DNS (`1.1.1.3` / `1.0.0.3`) using protected datagram sockets (`vpnService.protect()`), ensuring an extra layer of upstream malware and threat prevention.

### 2. Comprehensive Category Threat Engine
- **Adult & Explicit Content**: Hardened root-domain database covering top adult tubes, webcam platforms, explicit communities, and dedicated adult TLDs (`.xxx`, `.porn`, `.adult`, `.sex`, `.cam`).
- **Keyword & Subdomain Matching**: Catches mirror sites, subdomains, and CDN delivery networks (`*.pornhub.com`, `cdn.xvideos.com`).
- **Strict SafeSearch Enforcement**: Automatically reroutes Google and Bing DNS queries to strict SafeSearch VIPs directly at the network layer.
- **Configurable Risk Categories**: Optional toggleable blocking for Gambling/Sports Betting and Casual Hookup/Adult Dating portals.
- **Custom Rule Engine**: Persisted via Room database, allowing users to whitelist critical educational domains or blacklist custom distracting URLs.

### 3. Interruption & Reboot Resilience
- **Boot Recovery**: Registers a high-priority `BootReceiver` that automatically re-engages the protection service on device reboot (`BOOT_COMPLETED` and `MY_PACKAGE_REPLACED`).
- **Network Mobility**: Utilizes `ConnectivityManager.NetworkCallback` to seamlessly maintain filtering when switching between Wi-Fi and Cellular (4G/5G).

### 4. Interactive Live Classifier Diagnostic Tool
- Located in the Settings screen, this tool allows users and QA engineers to type any domain (e.g., `pornhub.com`, `wikipedia.org`, `google.com`) and receive an instant breakdown of the filtering decision, category match, and evaluation rationale.

---

## Architecture

Sabr is built using **Clean Architecture** principles and the modern **Model-View-ViewModel (MVVM)** pattern, ensuring strict separation of concerns, testability, and modularity.

```
                           ┌────────────────────────┐
                           │      UI Layer          │
                           │  Jetpack Compose M3    │
                           │  (Dashboard, Onboard,  │
                           │  Settings, Dialogs)    │
                           └───────────┬────────────┘
                                       │ (StateFlow / Events)
                           ┌───────────▼────────────┐
                           │     SabrViewModel      │
                           └───────────┬────────────┘
                                       │
            ┌──────────────────────────┴──────────────────────────┐
            │                     Domain Layer                    │
            │  ProtectionStateManager   │  UnblockSessionManager  │
            │  VpnController            │  DomainClassifier       │
            └───────────┬───────────────────────────┬─────────────┘
                        │                           │
            ┌───────────▼────────────┐  ┌───────────▼─────────────┐
            │  ProtectionRepository  │  │   DnsFilteringEngine    │
            │  (DataStore + Room)    │  │   (Allowlist/Blocklist) │
            └───────────┬────────────┘  └───────────┬─────────────┘
                        │                           │
  ┌─────────────────────▼───────────────────────────▼─────────────────────┐
  │                           Platform Layer                              │
  │  ProtectionService (VpnService TUN interface + async UDP loop)        │
  │  BootReceiver (reboot recovery)                                       │
  └───────────────────────────────────────────────────────────────────────┘
```

### MVVM & Clean Architecture Approach

- **UI Layer (Jetpack Compose M3)**: Pure declarative UI reacting exclusively to immutable `SabrUiState`. Follows Google's Material 3 guidelines with a dignified Deep Navy (`#0B2545`) and Emerald Green (`#10B981`) color palette.
- **ViewModel Layer (`SabrViewModel`)**: Retains state across configuration changes, orchestrates asynchronous coroutine pipelines, and exposes UI state via `StateFlow`.
- **Domain Layer (`domain/`)**: Pure Kotlin business logic containing use cases, state machine controllers, and domain interfaces (`ProtectionStateManager`, `UnblockSessionManager`, `DomainClassifier`).
- **Data Layer (`data/`)**: Implements repositories with a local Room database (`SabrDatabase`) for custom rules and filtering metrics, alongside AndroidX DataStore for persistent state machine timestamps.
- **Service Layer (`service/`)**: Contains `ProtectionService`, extending Android's `VpnService` to process raw network buffers asynchronously via Kotlin Coroutines.

---

### The 24-Hour Deliberate Cooldown State Machine

To eliminate instant bypasses fueled by momentary urges, Sabr enforces a deterministic 6-state progression model:

```
[ PROTECTION_OFF ] ──(User taps "Block Adult Websites" + Grants VPN)──> [ PROTECTION_ACTIVE ]
                                                                                │
                                                                       (Request Unblock)
                                                                                │
                                                                                ▼
                                                                     [ UNBLOCK_REQUESTED ]
                                                                                │
                                                                        (User starts Hour 1)
                                                                                │
                                                                                ▼
   ┌─────────────────────────────────────────────────────────────────> [  HOUR_RUNNING  ]
   │                                                                            │
   │                                                                     (60 minutes elapse)
   │                                                                            │
   │                                                                            ▼
   │                                                                     [  HOUR_COMPLETE  ]
   │                                                                            │
   └──(User manually resumes next hour: 2 through 24)───────────────────────────┤
                                                                                │ (Hour 24 completes)
                                                                                ▼
                                                                     [ UNBLOCK_AVAILABLE ]
                                                                                │
                                                                        (Disable Protection)
                                                                                │
                                                                                ▼
                                                                       [  PROTECTION_OFF  ]
```

#### State Machine Rules:
1. **`PROTECTION_OFF`**: Default state upon first launch. Protection is inactive and nothing is filtered until the user explicitly accepts onboarding.
2. **`PROTECTION_ACTIVE`**: The `ProtectionService` is actively running and filtering network traffic.
3. **`UNBLOCK_REQUESTED`**: Triggered when the user taps "Request Unblock". A motivational warning and reflection screen appears. Protection remains **100% active**.
4. **`HOUR_RUNNING`**: The user confirms Stage 1. A 60-minute countdown begins. The elapsed time is verified against system wall-clock timestamps (`System.currentTimeMillis()`), rendering device time tampering ineffective.
5. **`HOUR_COMPLETE`**: Once 60 minutes have elapsed, **the timer stops**. The system does **NOT** auto-advance to the next hour. The user must manually reopen the app, reflect, and click "Begin Stage X".
6. **`UNBLOCK_AVAILABLE`**: Unlocked **only** after Stage 24 has been completed. The user may now permanently disable protection or re-engage the shield.
7. **Immediate Cancellation**: At any point during `UNBLOCK_REQUESTED`, `HOUR_RUNNING`, or `HOUR_COMPLETE`, the user can tap "Cancel & Stay Protected" to immediately reset the cooldown back to `PROTECTION_ACTIVE`.

---

### Selective Packet Interception Pipeline

```
[ Device App / Browser DNS Request ]
                │
                ▼ (UDP Port 53)
[ Sabr Virtual DNS Gateway: 10.254.1.2:53 ]
                │
                ▼ (TUN Interface Read)
[ ParsedUdpPacket.parse() -> DnsPacket.parse() ]
                │
                ├─────────────────────────────────────────────┐
                │                                             │
       [ Allowlist Check ]                           [ SafeSearch Check ]
                │ (Matched)                                   │ (Search Engine)
                ▼                                             ▼
     Forward to Upstream                            Rewrite to Strict VIP
                │                                             │
                ├─────────────────────────────────────────────┘
                │
       [ Blocklist Engine ]
       • Adult TLDs (.xxx, .porn, .adult, .cam)
       • Known Adult Domain Registry
       • Adult Keyword Tokenizer
       • Gambling & Dating Toggles
                │
        ┌───────┴────────┐
        │                │
    [ MATCHED ]     [ NO MATCH ]
        │                │
        ▼                ▼
[ Craft 0.0.0.0 /   [ Forward to 1.1.1.3
  NXDOMAIN ]          via protect(socket) ]
        │                │
        ▼                ▼
[ Write IP Response Packet to TUN fd ]
                │
                ▼
[ Browser Receives Immediate Resolution ]
```

---

### Package Structure

```
com.example/
├── SabrApplication.kt                   # Application DI container & lifecycle
├── MainActivity.kt                      # Activity host & ActivityResult VPN permission contract
├── core/
│   ├── dns/
│   │   ├── DnsPacket.kt                 # Raw DNS query parser & NXDOMAIN/0.0.0.0 serializer
│   │   └── IpPacket.kt                  # IPv4 UDP packet parser, checksum calculator & response builder
│   └── filter/
│       ├── AllowlistManager.kt          # System-critical allowlist & user whitelist cache
│       ├── BlocklistManager.kt          # Curated adult domains, TLDs, keyword tokens, category filters
│       ├── BuiltInBlocklists.kt         # Hardened adult threat feed with versioning
│       ├── DomainClassifier.kt          # Evaluation coordinator producing FilterDecision
│       └── DnsFilter.kt                 # SafeSearch rewrite & filtering verdict provider
├── data/
│   ├── local/
│   │   ├── SabrDatabase.kt              # Room DB instance
│   │   ├── CustomRuleEntity.kt & Dao    # Room persistence for custom rules
│   │   ├── FilterLogEntity.kt & Dao     # Local aggregate counts and recent blocked events
│   │   └── DataStoreManager.kt          # Persistent storage for timestamps, stage index & settings
│   └── repository/
│       ├── ProtectionRepository.kt      # Repository abstraction
│       └── ProtectionRepositoryImpl.kt  # Unified repository implementation
├── domain/
│   ├── model/
│   │   ├── ProtectionStatus.kt          # 6-state machine enum
│   │   ├── Category.kt                  # Threat categories (ADULT, GAMBLING, DATING, SAFE)
│   │   ├── FilterDecision.kt            # Verdict data structure
│   │   └── UnblockSessionState.kt       # Persistent unblock cooldown state model
│   └── manager/
│       ├── ProtectionStateManager.kt    # Service state coordinator
│       ├── UnblockSessionManager.kt     # Monotonic timer & stage progression engine
│       └── VpnController.kt             # Android VpnService launcher & binder
├── receiver/
│   └── BootReceiver.kt                  # Restores protection on device boot / update
├── service/
│   └── ProtectionService.kt             # Native Android VpnService loopback DNS filter
└── ui/
    ├── theme/                           # Color.kt (Navy/Emerald), Theme.kt, Type.kt
    ├── components/                      # SabrTopBar, StatusShield, StageProgressGrid, MotivationalCard
    ├── screens/
    │   ├── OnboardingScreen.kt          # First-launch onboarding (Protection initially OFF)
    │   ├── DashboardScreen.kt           # Main dashboard with 24-hour stage grid & status shield
    │   ├── UnblockStageDialog.kt        # Reflection & warning modal for unblock confirmation
    │   └── SettingsScreen.kt            # Live domain diagnostic tester, rules, SafeSearch & categories
    └── viewmodel/                       # SabrViewModel.kt, SabrUiState.kt
```

---

## Security Protocols

1. **Zero-Logging & On-Device Processing (100% Local)**:
   - All packet inspection and domain matching happen strictly in memory on the user's device.
   - **No browsing history, URLs, or DNS queries are ever sent to an external server or stored persistently.**
2. **Google Play Policy Compliance**:
   - **No Accessibility Abuse**: Sabr strictly avoids using `AccessibilityService` or deceptive overlays to block content. It uses the officially sanctioned `android.net.VpnService` API designed specifically for network traffic management.
   - **Transparent System Consent**: Prior to activating protection, Sabr clearly explains the necessity of VPN permissions and delegates authorization to the standard Android system dialog via `VpnService.prepare()`.
3. **Defense-in-Depth Upstream Encryption**:
   - Outbound allowed requests are proxied directly to Cloudflare Family DNS (`1.1.1.3` / `1.0.0.3`) through protected native sockets, shielding the user from upstream malware and phishing vectors.
4. **Anti-Bypass Monotonic Timer Security**:
   - Cooldown progression is tied to wall-clock epoch timestamps stored in DataStore.
   - Force-quitting the app, clearing memory, or restarting the phone will not reset or advance the timer.

---

## How to Build

### Prerequisites
- **Android Studio**: Ladybug (2024.2+) or Meerkat+
- **JDK**: Version 17 or 21
- **Android SDK**:
  - `compileSdk`: 36
  - `minSdk`: 24 (Android 7.0 Nougat+)
  - `targetSdk`: 36 (Android 15+)

### Build Commands

1. **Clone the repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/sabr-android.git
   cd sabr-android
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   *The generated APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.*

3. **Build Production Release App Bundle (AAB for Google Play)**:
   ```bash
   ./gradlew bundleRelease
   ```
   *The generated bundle will be located at `app/build/outputs/bundle/release/app-release.aab`.*

### Running Automated Tests

Sabr includes a comprehensive test suite covering the DNS packet parser, the domain categorization engine, the 24-stage state machine, and Robolectric CUJs.

Execute all unit and Robolectric tests with:
```bash
./gradlew :app:testDebugUnitTest
```

#### Test Coverage Includes:
- `DnsFilteringEngineTest`:
  - Normal domains allowed (`google.com`, `wikipedia.org`, `github.com`).
  - Explicit adult domains sinkholed (`pornhub.com`, `xvideos.com`, `chaturbate.com`).
  - Dedicated adult TLDs blocked (`.xxx`, `.porn`, `.adult`, `.cam`).
  - Keyword tokenization and subdomain evasion defense.
  - SafeSearch DNS rewriting for Google and Bing.
  - Custom user allowlist/blocklist precedence.
  - Raw DNS query parsing and `0.0.0.0` response serialization.
  - Raw IPv4/UDP packet assembly and checksum calculation.
- `UnblockSessionStateMachineTest`:
  - First-launch default OFF verification.
  - Activation and permission delegation.
  - 60-minute stage persistence across app kills.
  - Pausing behavior upon stage completion (no auto-advancement).
  - Manual user progression requirement for stages 1 through 24.
  - Unblock availability trigger at stage 24.
  - Cooldown cancellation and immediate shield re-engagement.
- `ExampleRobolectricTest`:
  - Application context DI initialization and string resource verification on simulated Android SDK 36.

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
