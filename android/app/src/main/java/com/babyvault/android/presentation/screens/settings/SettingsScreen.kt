package com.babyvault.android.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.BuildConfig
import com.babyvault.android.presentation.theme.CardSurface
import com.babyvault.android.presentation.theme.NeutralGray
import com.babyvault.android.presentation.theme.Teal100
import com.babyvault.android.presentation.theme.Teal400
import com.babyvault.android.presentation.theme.Teal500
import com.babyvault.android.presentation.theme.Teal600
import com.babyvault.android.presentation.theme.TextPrimary
import com.babyvault.android.presentation.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset

@Composable
fun SettingsScreen(
    onNavigateProfile: () -> Unit,
    onNavigateSync: () -> Unit,
    onNavigateModelSetup: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val profile by viewModel.profile.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralGray)
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsHeader()

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Profile card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardSurface,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateProfile),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Surface(shape = CircleShape, color = Teal100, modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👶", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            profile?.name ?: "Baby",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                        )
                        profile?.dobMillis?.let { dob ->
                            val age = Period.between(
                                Instant.ofEpochMilli(dob).atZone(ZoneOffset.UTC).toLocalDate(),
                                LocalDate.now(),
                            )
                            val label = when {
                                age.years > 0 -> "${age.years}y ${age.months}m old"
                                age.months > 0 -> "${age.months} months old"
                                else -> "${age.days} days old"
                            }
                            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Text(
                            "Edit profile",
                            style = MaterialTheme.typography.labelSmall,
                            color = Teal500,
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            // Data & Sync section
            SettingsSection(title = "Data & Sync") {
                SettingsRow(
                    icon = Icons.Default.Sync,
                    iconTint = Teal500,
                    label = "Sync Settings",
                    subtitle = when {
                        syncStatus == null -> "Loading…"
                        syncStatus?.isConfigured == true && syncStatus?.totalPending == 0 -> "All synced"
                        syncStatus?.isConfigured == true -> "${syncStatus?.totalPending} pending"
                        else -> "Not configured"
                    },
                    onClick = onNavigateSync,
                )
            }

            // AI section
            SettingsSection(title = "AI Assistant") {
                SettingsRow(
                    icon = Icons.Default.SmartToy,
                    iconTint = Teal500,
                    label = "AI Model",
                    subtitle = "Configure on-device model",
                    onClick = onNavigateModelSetup,
                )
            }

            // Security section
            SettingsSection(title = "Security") {
                SettingsRow(
                    icon = Icons.Default.Fingerprint,
                    iconTint = Teal500,
                    label = "Biometric Auth",
                    subtitle = "Enabled — protects vault on every launch",
                    onClick = null,
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = NeutralGray)
                SettingsRow(
                    icon = Icons.Default.Lock,
                    iconTint = Teal500,
                    label = "Encryption",
                    subtitle = "ChaCha20-Poly1305 · Android Keystore",
                    onClick = null,
                )
            }

            // About section
            SettingsSection(title = "About") {
                SettingsRow(
                    icon = Icons.Default.Info,
                    iconTint = TextSecondary,
                    label = "Baby Vault",
                    subtitle = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    onClick = null,
                    showArrow = false,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Brush.verticalGradient(listOf(Teal600, Teal400))),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardSurface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    showArrow: Boolean = onClick != null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Teal100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        if (showArrow) {
            Icon(
                Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
