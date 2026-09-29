package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnLock
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.SabrCardBorder
import com.example.ui.theme.SabrCardDark
import com.example.ui.theme.SabrDeepNavy
import com.example.ui.theme.SabrEmerald
import com.example.ui.theme.SabrEmeraldDark
import com.example.ui.theme.SabrMidnight
import com.example.ui.theme.SabrOceanBlue
import com.example.ui.theme.SabrSage
import com.example.ui.theme.SabrSlateGray
import com.example.ui.theme.SabrSoftWhite

@Composable
fun OnboardingScreen(
    onActivateClicked: () -> Unit,
    onPrivacyClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SabrMidnight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Hero Banner Image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SabrOceanBlue.copy(alpha = 0.5f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sabr_hero_banner_1790660372765),
                        contentDescription = "Sabr Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "صَبْر",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = SabrEmerald
                )

                Text(
                    text = "SABR",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Patience, Self-Mastery & Digital Protection",
                    style = MaterialTheme.typography.titleSmall,
                    color = SabrSlateGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // What Sabr Does - Features
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OnboardingFeatureRow(
                        icon = Icons.Default.VpnLock,
                        title = "Native Android Network Filtering",
                        description = "Intercepts DNS requests device-wide using Android's native VpnService. Works on Wi-Fi, 4G, and 5G."
                    )
                    OnboardingFeatureRow(
                        icon = Icons.Default.Security,
                        title = "Zero-Tolerance Adult Blocking",
                        description = "Instantly sinkholes adult websites, explicit tube portals, and webcams before any connection is made."
                    )
                    OnboardingFeatureRow(
                        icon = Icons.Default.HourglassBottom,
                        title = "24-Hour Deliberate Cooldown",
                        description = "Requires 24 separate conscious one-hour confirmation stages to disable. No impulsive bypass."
                    )
                    OnboardingFeatureRow(
                        icon = Icons.Default.Lock,
                        title = "100% Private & Local",
                        description = "Your browsing activity stays strictly on your device. Never logged, never transmitted, never sold."
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SabrOceanBlue.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, SabrOceanBlue.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SabrEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Protection is currently OFF. Tapping below will prompt for Android VPN permission to start filtering.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SabrSoftWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onActivateClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("block_adult_websites_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SabrEmeraldDark,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Block Adult Websites",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "Privacy Guarantees & Technical Architecture",
                        style = MaterialTheme.typography.labelMedium,
                        color = SabrSlateGray,
                        modifier = Modifier
                            .testTag("onboarding_privacy_link")
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingFeatureRow(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SabrCardDark,
        border = BorderStroke(1.dp, SabrCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = SabrDeepNavy,
                border = BorderStroke(1.dp, SabrEmerald.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SabrEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SabrSlateGray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
