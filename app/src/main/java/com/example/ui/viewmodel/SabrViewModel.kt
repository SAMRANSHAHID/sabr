package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SabrApplication
import com.example.data.local.CustomRuleEntity
import com.example.data.local.FilterLogEntity
import com.example.domain.model.Category
import com.example.domain.model.FilterDecision
import com.example.domain.model.ProtectionStatus
import com.example.domain.model.UnblockSessionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MotivationalMessage(
    val title: String,
    val arabicText: String,
    val message: String
)

val MOTIVATIONAL_MESSAGES = listOf(
    MotivationalMessage(
        title = "Patience is a Shield",
        arabicText = "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ",
        message = "True strength is not in overpowering others, but in mastering yourself during moments of impulse."
    ),
    MotivationalMessage(
        title = "Guard Your Focus",
        arabicText = "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
        message = "Every minute you choose discipline over immediate gratification, you rebuild your clarity, self-respect, and inner peace."
    ),
    MotivationalMessage(
        title = "Reflect Before Acting",
        arabicText = "وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا",
        message = "The urge you feel right now is temporary. The regret of giving in lasts far longer than the fleeting satisfaction."
    ),
    MotivationalMessage(
        title = "Step by Step",
        arabicText = "فَصَبْرٌ جَمِيلٌ",
        message = "A 24-hour deliberate cooldown is designed to allow the emotional fog to clear so wisdom can prevail."
    )
)

data class SabrUiState(
    val sessionState: UnblockSessionState = UnblockSessionState(),
    val hasSeenOnboarding: Boolean = false,
    val isVpnPermissionGranted: Boolean = false,
    val safeSearchEnabled: Boolean = true,
    val blockGambling: Boolean = true,
    val blockDating: Boolean = true,
    val blockedCount: Int = 0,
    val allowedCount: Int = 0,
    val recentLogs: List<FilterLogEntity> = emptyList(),
    val customRules: List<CustomRuleEntity> = emptyList(),
    val currentQuoteIndex: Int = 0,
    val showWarningDialog: Boolean = false,
    val showPrivacyDialog: Boolean = false,
    val showAddRuleDialog: Boolean = false,
    val showTestingSettings: Boolean = false,
    val testDomainInput: String = "",
    val testDomainResult: FilterDecision? = null,
    val wallClockTime: Long = System.currentTimeMillis()
)

class SabrViewModel(application: Application) : AndroidViewModel(application) {

    private val sabrApp = application as SabrApplication
    private val repository = sabrApp.repository
    private val vpnController = sabrApp.vpnController

    private val _uiState = MutableStateFlow(SabrUiState())
    val uiState: StateFlow<SabrUiState> = _uiState.asStateFlow()

    init {
        checkVpnPermission()

        // Combine reactive flows from repository
        viewModelScope.launch {
            repository.sessionState.collect { state ->
                _uiState.update { it.copy(sessionState = state) }
            }
        }

        viewModelScope.launch {
            repository.hasSeenOnboarding.collect { seen ->
                _uiState.update { it.copy(hasSeenOnboarding = seen) }
            }
        }

        viewModelScope.launch {
            repository.safeSearchEnabled.collect { enabled ->
                _uiState.update { it.copy(safeSearchEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            repository.blockGambling.collect { enabled ->
                _uiState.update { it.copy(blockGambling = enabled) }
            }
        }

        viewModelScope.launch {
            repository.blockDating.collect { enabled ->
                _uiState.update { it.copy(blockDating = enabled) }
            }
        }

        viewModelScope.launch {
            repository.blockedCount.collect { count ->
                _uiState.update { it.copy(blockedCount = count) }
            }
        }

        viewModelScope.launch {
            repository.allowedCount.collect { count ->
                _uiState.update { it.copy(allowedCount = count) }
            }
        }

        viewModelScope.launch {
            repository.recentLogs.collect { logs ->
                _uiState.update { it.copy(recentLogs = logs) }
            }
        }

        viewModelScope.launch {
            repository.customRules.collect { rules ->
                _uiState.update { it.copy(customRules = rules) }
            }
        }

        // Timer ticking loop: updates UI wallClock and checks state transitions
        viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                _uiState.update { it.copy(wallClockTime = now) }
                repository.syncTimerState(now)
                delay(1000L)
            }
        }
    }

    fun checkVpnPermission(): Boolean {
        val granted = vpnController.isVpnPermissionGranted(getApplication())
        _uiState.update { it.copy(isVpnPermissionGranted = granted) }
        return granted
    }

    fun getVpnPrepareIntent(): Intent? {
        return vpnController.getVpnPrepareIntent(getApplication())
    }

    fun onVpnPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(isVpnPermissionGranted = isGranted) }
        if (isGranted) {
            activateProtection()
        }
    }

    fun activateProtection() {
        viewModelScope.launch {
            repository.setHasSeenOnboarding(true)
            repository.enableProtection(getApplication())
            checkVpnPermission()
        }
    }

    fun onRequestUnblockClicked() {
        viewModelScope.launch {
            repository.requestUnblock()
            _uiState.update {
                it.copy(
                    showWarningDialog = true,
                    currentQuoteIndex = (it.currentQuoteIndex + 1) % MOTIVATIONAL_MESSAGES.size
                )
            }
        }
    }

    fun onBeginStageConfirmed() {
        viewModelScope.launch {
            repository.startHour()
            _uiState.update { it.copy(showWarningDialog = false) }
        }
    }

    fun onCancelUnblockClicked() {
        viewModelScope.launch {
            repository.cancelUnblock()
            _uiState.update { it.copy(showWarningDialog = false) }
        }
    }

    fun onFinalizeDisableProtection() {
        viewModelScope.launch {
            repository.finalizeUnblock(getApplication())
        }
    }

    fun dismissWarningDialog() {
        _uiState.update { it.copy(showWarningDialog = false) }
    }

    fun onSetPrivacyDialog(visible: Boolean) {
        _uiState.update { it.copy(showPrivacyDialog = visible) }
    }

    fun onSetAddRuleDialog(visible: Boolean) {
        _uiState.update { it.copy(showAddRuleDialog = visible) }
    }

    fun onSetTestingSettings(visible: Boolean) {
        _uiState.update { it.copy(showTestingSettings = visible) }
    }

    fun onToggleSafeSearch(enabled: Boolean) {
        viewModelScope.launch { repository.setSafeSearchEnabled(enabled) }
    }

    fun onToggleGambling(enabled: Boolean) {
        viewModelScope.launch { repository.setBlockGambling(enabled) }
    }

    fun onToggleDating(enabled: Boolean) {
        viewModelScope.launch { repository.setBlockDating(enabled) }
    }

    fun onTestDomainInputChanged(domain: String) {
        _uiState.update { it.copy(testDomainInput = domain) }
    }

    fun onRunDomainTest() {
        val input = _uiState.value.testDomainInput
        if (input.isNotBlank()) {
            val result = repository.testDomainClassification(input)
            _uiState.update { it.copy(testDomainResult = result) }
        }
    }

    fun onAddCustomRule(domain: String, isAllowed: Boolean, category: Category, note: String) {
        viewModelScope.launch {
            repository.addCustomRule(domain, isAllowed, category, note)
            _uiState.update { it.copy(showAddRuleDialog = false) }
        }
    }

    fun onRemoveCustomRule(id: Long) {
        viewModelScope.launch { repository.removeCustomRule(id) }
    }

    fun onClearLogs() {
        viewModelScope.launch { repository.clearLogs() }
    }

    fun setTestStageDurationSeconds(seconds: Long) {
        viewModelScope.launch {
            repository.setStageDurationForTesting(seconds * 1000L)
        }
    }

    fun nextMotivationalQuote() {
        _uiState.update {
            it.copy(currentQuoteIndex = (it.currentQuoteIndex + 1) % MOTIVATIONAL_MESSAGES.size)
        }
    }
}
