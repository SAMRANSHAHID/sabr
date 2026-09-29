package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SabrCardBorder
import com.example.ui.theme.SabrCardDark
import com.example.ui.theme.SabrDeepNavy
import com.example.ui.theme.SabrEmerald
import com.example.ui.theme.SabrEmeraldDark
import com.example.ui.theme.SabrSlateGray
import com.example.ui.theme.SabrSoftWhite

@Composable
fun PrivacyDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SabrCardDark),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, SabrCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = SabrDeepNavy,
                    border = BorderStroke(1.dp, SabrEmerald)
                ) {
                    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SabrEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Privacy & Security Guarantee",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sabr (صبر) is engineered with zero-compromise privacy principles:\n\n" +
                            "• On-Device DNS Filtering: All domain lookups are categorized strictly inside your device. No traffic is routed through third-party proxy servers.\n\n" +
                            "• Zero Browsing Logs: We do not log, retain, or transmit your search history or visited domains.\n\n" +
                            "• No Data Sale or Telemetry: Your device identity and activity are never collected, sold, or shared with advertisers.\n\n" +
                            "• Native VpnService Compliance: Uses standard Android VpnService exclusively for local loopback DNS sinkholing, complying with Google Play Security Policies.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SabrSoftWhite,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_privacy_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SabrEmeraldDark)
                ) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
