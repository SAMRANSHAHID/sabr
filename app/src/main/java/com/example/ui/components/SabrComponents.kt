package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ProtectionStatus
import com.example.domain.model.UnblockSessionState
import com.example.ui.theme.SabrAmber
import com.example.ui.theme.SabrCardBorder
import com.example.ui.theme.SabrCardDark
import com.example.ui.theme.SabrDeepNavy
import com.example.ui.theme.SabrEmerald
import com.example.ui.theme.SabrEmeraldDark
import com.example.ui.theme.SabrMidnight
import com.example.ui.theme.SabrOceanBlue
import com.example.ui.theme.SabrRed
import com.example.ui.theme.SabrSage
import com.example.ui.theme.SabrSlateGray
import com.example.ui.theme.SabrSoftWhite
import com.example.ui.viewmodel.MotivationalMessage

@Composable
fun SabrTopBar(
    onPrivacyClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = SabrOceanBlue.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, SabrEmerald.copy(alpha = 0.4f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Sabr Logo",
                        tint = SabrEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SABR",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "صَبْر",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SabrEmerald
                    )
                }
                Text(
                    text = "Steadfast Digital Shield",
                    style = MaterialTheme.typography.bodySmall,
                    color = SabrSlateGray
                )
            }
        }

        Row {
            IconButton(
                onClick = onPrivacyClick,
                modifier = Modifier.testTag("privacy_info_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Privacy Policy",
                    tint = SabrSlateGray
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = SabrSlateGray
                )
            }
        }
    }
}

@Composable
fun StatusShield(
    status: ProtectionStatus,
    modifier: Modifier = Modifier
) {
    val isActive = status.isProtectionEnabled
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shield_scale"
    )

    val glowColor by animateColorAsState(
        targetValue = when (status) {
            ProtectionStatus.PROTECTION_OFF -> Color.Transparent
            ProtectionStatus.PROTECTION_ACTIVE -> SabrEmerald
            ProtectionStatus.UNBLOCK_REQUESTED -> SabrAmber
            ProtectionStatus.HOUR_RUNNING -> SabrOceanBlue
            ProtectionStatus.HOUR_COMPLETE -> SabrSage
            ProtectionStatus.UNBLOCK_AVAILABLE -> SabrRed
        },
        label = "glow_color"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(190.dp)
                .scale(pulseScale)
        ) {
            // Background ambient glow circle
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    glowColor.copy(alpha = 0.28f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Outer ring
            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                color = SabrDeepNavy,
                border = BorderStroke(
                    width = 3.dp,
                    color = if (isActive) glowColor else SabrCardBorder
                ),
                shadowElevation = if (isActive) 12.dp else 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Shield else Icons.Outlined.Lock,
                        contentDescription = "Status Shield",
                        tint = if (isActive) glowColor else SabrSlateGray,
                        modifier = Modifier.size(68.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Badge pill
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isActive) glowColor.copy(alpha = 0.15f) else SabrCardBorder.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, if (isActive) glowColor.copy(alpha = 0.6f) else SabrCardBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) glowColor else SabrSlateGray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (status) {
                        ProtectionStatus.PROTECTION_OFF -> "PROTECTION OFF"
                        ProtectionStatus.PROTECTION_ACTIVE -> "PROTECTION ACTIVE"
                        ProtectionStatus.UNBLOCK_REQUESTED -> "UNBLOCK REQUESTED"
                        ProtectionStatus.HOUR_RUNNING -> "COOLDOWN RUNNING"
                        ProtectionStatus.HOUR_COMPLETE -> "STAGE PAUSED"
                        ProtectionStatus.UNBLOCK_AVAILABLE -> "UNBLOCK AVAILABLE"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (isActive) Color.White else SabrSlateGray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (status) {
                ProtectionStatus.PROTECTION_OFF -> "Adult content filtering is currently inactive"
                ProtectionStatus.PROTECTION_ACTIVE -> "Native VpnService blocking adult websites on Wi-Fi and mobile data"
                ProtectionStatus.UNBLOCK_REQUESTED -> "Warning: Unblock session requested"
                ProtectionStatus.HOUR_RUNNING -> "Stage timer is actively ticking"
                ProtectionStatus.HOUR_COMPLETE -> "Hour ended. Protection is still 100% active."
                ProtectionStatus.UNBLOCK_AVAILABLE -> "All 24 stages cleared. Unblock is now accessible."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = SabrSlateGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StageProgressGrid(
    sessionState: UnblockSessionState,
    now: Long,
    onBeginNextStageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = SabrCardDark),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SabrCardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "24-Hour Deliberate Cooldown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Completed: ${sessionState.completedStages} of 24 Hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = SabrSlateGray
                    )
                }

                if (sessionState.status == ProtectionStatus.HOUR_RUNNING) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SabrOceanBlue.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, SabrEmerald.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = sessionState.formatRemainingTime(now),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = SabrEmerald,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 24 Stage Pills Grid (6 columns x 4 rows)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 6
            ) {
                for (hour in 1..24) {
                    val isCompleted = hour <= sessionState.completedStages
                    val isCurrent = hour == sessionState.currentStage &&
                            (sessionState.status == ProtectionStatus.HOUR_RUNNING || sessionState.status == ProtectionStatus.HOUR_COMPLETE)

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    isCompleted -> SabrEmerald.copy(alpha = 0.25f)
                                    isCurrent && sessionState.status == ProtectionStatus.HOUR_RUNNING -> SabrOceanBlue
                                    isCurrent && sessionState.status == ProtectionStatus.HOUR_COMPLETE -> SabrAmber.copy(alpha = 0.3f)
                                    else -> SabrMidnight.copy(alpha = 0.6f)
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = when {
                                    isCompleted -> SabrEmerald
                                    isCurrent -> SabrSage
                                    else -> SabrCardBorder
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Stage $hour complete",
                                tint = SabrEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        } else if (isCurrent && sessionState.status == ProtectionStatus.HOUR_RUNNING) {
                            Text(
                                text = "$hour",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "$hour",
                                style = MaterialTheme.typography.labelSmall,
                                color = SabrSlateGray
                            )
                        }
                    }
                }
            }

            // Prompt when current hour is finished
            AnimatedVisibility(visible = sessionState.status == ProtectionStatus.HOUR_COMPLETE) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SabrAmber.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SabrAmber.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Hour finished",
                                tint = SabrAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Hour ${sessionState.completedStages} Complete!",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Protection remains strictly ON. Take a moment to reflect before starting Stage ${sessionState.completedStages + 1}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SabrSoftWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MotivationalCard(
    quote: MotivationalMessage,
    onNextQuote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onNextQuote() },
        colors = CardDefaults.cardColors(containerColor = SabrCardDark.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SabrCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quote.title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = SabrEmerald
                )
                Text(
                    text = quote.arabicText,
                    fontSize = 16.sp,
                    color = SabrSage,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "“${quote.message}”",
                style = MaterialTheme.typography.bodyMedium,
                color = SabrSoftWhite,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap for another reflection",
                style = MaterialTheme.typography.labelSmall,
                color = SabrSlateGray.copy(alpha = 0.7f)
            )
        }
    }
}
