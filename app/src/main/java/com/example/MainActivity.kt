package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.ProtectionStatus
import com.example.ui.components.PrivacyDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UnblockStageDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MOTIVATIONAL_MESSAGES
import com.example.ui.viewmodel.SabrViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SabrAppRoot()
                }
            }
        }
    }
}

@Composable
fun SabrAppRoot(
    viewModel: SabrViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSettingsOpen by remember { mutableStateOf(false) }

    // Android VPN Permission Request Contract
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val isGranted = result.resultCode == Activity.RESULT_OK
        viewModel.onVpnPermissionResult(isGranted)
    }

    val handleActivateProtection: () -> Unit = {
        val prepareIntent = viewModel.getVpnPrepareIntent()
        if (prepareIntent != null) {
            vpnPermissionLauncher.launch(prepareIntent)
        } else {
            // Already granted by user
            viewModel.activateProtection()
        }
    }

    // Determine primary screen
    val showOnboarding = !uiState.hasSeenOnboarding && uiState.sessionState.status == ProtectionStatus.PROTECTION_OFF

    if (showOnboarding) {
        OnboardingScreen(
            onActivateClicked = handleActivateProtection,
            onPrivacyClicked = { viewModel.onSetPrivacyDialog(true) }
        )
    } else if (isSettingsOpen) {
        BackHandler { isSettingsOpen = false }
        SettingsScreen(
            uiState = uiState,
            onBackClicked = { isSettingsOpen = false },
            onToggleSafeSearch = { viewModel.onToggleSafeSearch(it) },
            onToggleGambling = { viewModel.onToggleGambling(it) },
            onToggleDating = { viewModel.onToggleDating(it) },
            onTestDomainInputChanged = { viewModel.onTestDomainInputChanged(it) },
            onRunDomainTest = { viewModel.onRunDomainTest() },
            onAddCustomRule = { domain, isAllowed, category, note ->
                viewModel.onAddCustomRule(domain, isAllowed, category, note)
            },
            onRemoveCustomRule = { viewModel.onRemoveCustomRule(it) },
            onSetTestingStageDuration = { viewModel.setTestStageDurationSeconds(it) },
            onShowPrivacyDialog = { viewModel.onSetPrivacyDialog(true) }
        )
    } else {
        DashboardScreen(
            uiState = uiState,
            onActivateClicked = handleActivateProtection,
            onRequestUnblockClicked = { viewModel.onRequestUnblockClicked() },
            onConfirmBeginStageClicked = { viewModel.onBeginStageConfirmed() },
            onCancelUnblockClicked = { viewModel.onCancelUnblockClicked() },
            onDisableProtectionClicked = { viewModel.onFinalizeDisableProtection() },
            onNextQuoteClicked = { viewModel.nextMotivationalQuote() },
            onPrivacyClicked = { viewModel.onSetPrivacyDialog(true) },
            onSettingsClicked = { isSettingsOpen = true }
        )
    }

    // Warning and stage dialogs
    val activeQuote = MOTIVATIONAL_MESSAGES[uiState.currentQuoteIndex % MOTIVATIONAL_MESSAGES.size]
    if (uiState.showWarningDialog || uiState.sessionState.status == ProtectionStatus.UNBLOCK_REQUESTED) {
        UnblockStageDialog(
            sessionState = uiState.sessionState,
            quote = activeQuote,
            onConfirmBeginStage = { viewModel.onBeginStageConfirmed() },
            onCancelUnblock = { viewModel.onCancelUnblockClicked() },
            onDismiss = { viewModel.dismissWarningDialog() }
        )
    }

    // Privacy Dialog
    if (uiState.showPrivacyDialog) {
        PrivacyDialog(
            onDismiss = { viewModel.onSetPrivacyDialog(false) }
        )
    }
}
