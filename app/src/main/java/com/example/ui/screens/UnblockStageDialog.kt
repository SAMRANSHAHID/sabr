package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
fun UnblockStageDialog(
    sessionState: UnblockSessionState,
    quote: MotivationalMessage,
    onConfirmBeginStage: () -> Unit,
    onCancelUnblock: () -> Unit,
    onDismiss: () -> Unit
) {
    val isHourComplete = sessionState.status == ProtectionStatus.HOUR_COMPLETE
    val targetStage = if (isHourComplete) sessionState.completedStages + 1 else 1

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SabrCardDark),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, SabrAmber.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = SabrAmber.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, SabrAmber)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isHourComplete) Icons.Default.HourglassTop else Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = SabrAmber,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isHourComplete) "Stage ${sessionState.completedStages} Complete" else "Deliberate 24-Hour Cooldown",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isHourComplete) "Stage $targetStage of 24 Ready to Begin" else "Stage 1 of 24 Confirmation",
                    style = MaterialTheme.typography.labelMedium,
                    color = SabrAmber
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reflection Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SabrMidnight.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, SabrCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = quote.arabicText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SabrEmerald,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "“${quote.message}”",
                            style = MaterialTheme.typography.bodySmall,
                            color = SabrSoftWhite,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sabr enforces 24 separate one-hour intervals before protection can ever be lifted. Each hour pauses when finished and requires your manual confirmation to proceed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SabrSlateGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Button(
                    onClick = onConfirmBeginStage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_begin_stage_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SabrOceanBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Begin Stage $targetStage (60 Minutes)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onCancelUnblock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("cancel_unblock_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SabrEmerald),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SabrEmerald
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cancel & Stay Protected",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
