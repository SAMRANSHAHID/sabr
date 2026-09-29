package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.Category
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
import com.example.ui.viewmodel.SabrUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SabrUiState,
    onBackClicked: () -> Unit,
    onToggleSafeSearch: (Boolean) -> Unit,
    onToggleGambling: (Boolean) -> Unit,
    onToggleDating: (Boolean) -> Unit,
    onTestDomainInputChanged: (String) -> Unit,
    onRunDomainTest: () -> Unit,
    onAddCustomRule: (domain: String, isAllowed: Boolean, category: Category, note: String) -> Unit,
    onRemoveCustomRule: (Long) -> Unit,
    onSetTestingStageDuration: (Long) -> Unit,
    onShowPrivacyDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var newDomainInput by remember { mutableStateOf("") }
    var isNewDomainAllow by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SabrMidnight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBackClicked,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Protection & Rules",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Content Filtering Categories
            Text(
                text = "PROTECTION CATEGORIES",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = SabrEmerald
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SabrCardDark),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SabrCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Adult Content (Mandatory Core)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Adult & Explicit Web Content", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Pornography, explicit tube sites, and live cams", style = MaterialTheme.typography.bodySmall, color = SabrSlateGray)
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = null, // Always locked ON when protection is active
                            enabled = false,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SabrEmerald,
                                checkedTrackColor = SabrEmeraldDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // SafeSearch Enforcement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enforce Strict SafeSearch", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Locks Google & Bing into Strict Mode at network layer", style = MaterialTheme.typography.bodySmall, color = SabrSlateGray)
                        }
                        Switch(
                            checked = uiState.safeSearchEnabled,
                            onCheckedChange = onToggleSafeSearch,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SabrEmerald,
                                checkedTrackColor = SabrEmeraldDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gambling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Block Gambling & Betting", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Casinos, sports betting, and wagering networks", style = MaterialTheme.typography.bodySmall, color = SabrSlateGray)
                        }
                        Switch(
                            checked = uiState.blockGambling,
                            onCheckedChange = onToggleGambling,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SabrEmerald,
                                checkedTrackColor = SabrEmeraldDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dating / Hookup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Block Casual Hookup & Adult Dating", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Casual adult encounter and hookup platforms", style = MaterialTheme.typography.bodySmall, color = SabrSlateGray)
                        }
                        Switch(
                            checked = uiState.blockDating,
                            onCheckedChange = onToggleDating,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SabrEmerald,
                                checkedTrackColor = SabrEmeraldDark
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Real-time Live Domain Classification Tester (QA & Verification)
            Text(
                text = "FILTERING ENGINE DIAGNOSTIC TOOL",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = SabrEmerald
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SabrCardDark),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SabrCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Test Any Domain Real-Time",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Type any domain to verify the native classifier's immediate decision.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SabrSlateGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.testDomainInput,
                            onValueChange = onTestDomainInputChanged,
                            placeholder = { Text("e.g. pornhub.com or wikipedia.org", color = SabrSlateGray) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_domain_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SabrEmerald,
                                unfocusedBorderColor = SabrCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onRunDomainTest,
                            modifier = Modifier.testTag("test_domain_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SabrOceanBlue)
                        ) {
                            Text("Test")
                        }
                    }

                    // Result card
                    uiState.testDomainResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (result.isAllowed) SabrEmerald.copy(alpha = 0.15f) else SabrRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (result.isAllowed) SabrEmerald else SabrRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (result.isAllowed) Icons.Default.Check else Icons.Default.Block,
                                    contentDescription = null,
                                    tint = if (result.isAllowed) SabrEmerald else SabrRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (result.isAllowed) "ALLOWED" else "BLOCKED (Sinkhole 0.0.0.0)",
                                        fontWeight = FontWeight.Bold,
                                        color = if (result.isAllowed) SabrEmerald else SabrRed
                                    )
                                    Text(
                                        text = "${result.category.displayName} • ${result.reason}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SabrSoftWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Custom Allowlist / Blocklist
            Text(
                text = "CUSTOM DOMAIN RULES",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = SabrEmerald
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SabrCardDark),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SabrCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Add Custom Rule",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newDomainInput,
                            onValueChange = { newDomainInput = it },
                            placeholder = { Text("customdomain.com", color = SabrSlateGray) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SabrEmerald,
                                unfocusedBorderColor = SabrCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (newDomainInput.isNotBlank()) {
                                    onAddCustomRule(newDomainInput, false, Category.CUSTOM_BLOCKED, "User Custom Block")
                                    newDomainInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SabrRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Block Domain")
                        }

                        Button(
                            onClick = {
                                if (newDomainInput.isNotBlank()) {
                                    onAddCustomRule(newDomainInput, true, Category.CUSTOM_ALLOWED, "User Custom Whitelist")
                                    newDomainInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SabrEmeraldDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Allow Domain")
                        }
                    }

                    // Existing rules list
                    if (uiState.customRules.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Active Custom Rules:", style = MaterialTheme.typography.labelMedium, color = SabrSlateGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.customRules.forEach { rule ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SabrMidnight,
                                    border = BorderStroke(1.dp, SabrCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(rule.domain, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                if (rule.isAllowed) "Custom Allowed" else "Custom Blocked",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (rule.isAllowed) SabrEmerald else SabrRed
                                            )
                                        }
                                        IconButton(onClick = { onRemoveCustomRule(rule.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SabrSlateGray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: Testing & QA Controls (Allows testing the 24 stages in seconds!)
            Text(
                text = "QA / EVALUATION MODE",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = SabrAmber
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SabrCardDark),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SabrAmber.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = SabrAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stage Duration Control", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "For automated and manual verification of the 24 unblock stages without waiting 24 physical hours.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SabrSlateGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSetTestingStageDuration(3600L) },
                            colors = ButtonDefaults.buttonColors(containerColor = SabrOceanBlue),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set_duration_60m_button")
                        ) {
                            Text("60 Min (Prod)")
                        }

                        Button(
                            onClick = { onSetTestingStageDuration(5L) },
                            colors = ButtonDefaults.buttonColors(containerColor = SabrAmber),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set_duration_5s_button")
                        ) {
                            Text("5 Sec (Test)")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
