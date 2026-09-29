package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ProtectionStatus
import com.example.ui.components.MotivationalCard
import com.example.ui.components.SabrTopBar
import com.example.ui.components.StageProgressGrid
import com.example.ui.components.StatusShield
import com.example.ui.theme.SabrAmber
import com.example.ui.theme.SabrCardBorder
import com.example.ui.theme.SabrCardDark
import com.example.ui.theme.SabrDeepNavy
import com.example.ui.theme.SabrEmerald
import com.example.ui.theme.SabrEmeraldDark
import com.example.ui.theme.SabrMidnight
import com.example.ui.theme.SabrOceanBlue
import com.example.ui.theme.SabrRed
import com.example.ui.theme.SabrSlateGray
import com.example.ui.theme.SabrSoftWhite
import com.example.ui.viewmodel.MOTIVATIONAL_MESSAGES
import com.example.ui.viewmodel.SabrUiState

@Composable
fun DashboardScreen(
    uiState: SabrUiState,
    onActivateClicked: () -> Unit,
    onRequestUnblockClicked: () -> Unit,
    onConfirmBeginStageClicked: () -> Unit,
    onCancelUnblockClicked: () -> Unit,
    onDisableProtectionClicked: () -> Unit,
    onNextQuoteClicked: () -> Unit,
    onPrivacyClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val session = uiState.sessionState
    val status = session.status
    val quote = MOTIVATIONAL_MESSAGES[uiState.currentQuoteIndex % MOTIVATIONAL_MESSAGES.size]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SabrMidnight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top Bar
            SabrTopBar(
                onPrivacyClick = onPrivacyClicked,
                onSettingsClick = onSettingsClicked
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Protection Shield Indicator
            StatusShield(status = status)

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Action Buttons depending on exact state machine
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (status) {
                    ProtectionStatus.PROTECTION_OFF -> {
                        Button(
                            onClick = onActivateClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("block_adult_websites_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SabrEmeraldDark,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "BLOCK ADULT WEBSITES",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    ProtectionStatus.PROTECTION_ACTIVE -> {
                        OutlinedButton(
                            onClick = onRequestUnblockClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("request_unblock_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, SabrAmber),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SabrAmber
                            )
                        ) {
                            Icon(imageVector = Icons.Default.VpnKey, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "REQUEST UNBLOCK",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    ProtectionStatus.UNBLOCK_REQUESTED -> {
                        Button(
                            onClick = onConfirmBeginStageClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("begin_stage_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SabrOceanBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "BEGIN STAGE 1 (60 MIN)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onCancelUnblockClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("cancel_unblock_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, SabrEmerald),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SabrEmerald)
                        ) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Keep Protection Active",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    ProtectionStatus.HOUR_RUNNING -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = SabrOceanBlue.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, SabrOceanBlue)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Stage ${session.currentStage} of 24 Running",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Time Remaining: ${session.formatRemainingTime(uiState.wallClockTime)}",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = SabrEmerald
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onCancelUnblockClicked,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("cancel_unblock_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SabrEmeraldDark,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.Shield, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cancel Unblock & Stay Protected")
                                }
                            }
                        }
                    }

                    ProtectionStatus.HOUR_COMPLETE -> {
                        Button(
                            onClick = onConfirmBeginStageClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("begin_stage_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SabrAmber,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "BEGIN STAGE ${session.completedStages + 1} OF 24",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onCancelUnblockClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("cancel_unblock_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, SabrEmerald),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SabrEmerald)
                        ) {
                            Text("Cancel Unblock & Stay Protected")
                        }
                    }

                    ProtectionStatus.UNBLOCK_AVAILABLE -> {
                        Button(
                            onClick = onDisableProtectionClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("disable_protection_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SabrRed,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "DISABLE PROTECTION",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onCancelUnblockClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("cancel_unblock_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, SabrEmerald),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SabrEmerald)
                        ) {
                            Text("Re-Engage Shield (Recommended)")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 24-Hour Cooldown Grid (Visible whenever protection is active or in progress)
            if (status.isProtectionEnabled) {
                StageProgressGrid(
                    sessionState = session,
                    now = uiState.wallClockTime,
                    onBeginNextStageClick = onConfirmBeginStageClicked
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Stats row (Threats Blocked / Queries Allowed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SabrCardDark),
                    border = BorderStroke(1.dp, SabrCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = SabrRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Threats Blocked",
                                style = MaterialTheme.typography.labelSmall,
                                color = SabrSlateGray
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${uiState.blockedCount}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SabrCardDark),
                    border = BorderStroke(1.dp, SabrCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SabrEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Clean Traffic",
                                style = MaterialTheme.typography.labelSmall,
                                color = SabrSlateGray
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (status.isProtectionEnabled) "Filtered (Active)" else "Direct (Inactive)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (status.isProtectionEnabled) SabrEmerald else SabrSlateGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Motivational Card
            MotivationalCard(
                quote = quote,
                onNextQuote = onNextQuoteClicked
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
