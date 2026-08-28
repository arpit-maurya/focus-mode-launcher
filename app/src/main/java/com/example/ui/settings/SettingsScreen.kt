package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.EInkThemePreset
import com.example.ui.MainViewModel
import com.example.ui.components.EInkCard
import com.example.ui.components.EInkCheckbox
import com.example.ui.components.EInkPrimaryButton
import com.example.ui.components.EInkSecondaryButton
import com.example.ui.components.EInkSectionHeader

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val isGrayscale by viewModel.isGrayscaleEnabled.collectAsStateWithLifecycle()
    val isBlockerActive by viewModel.isNotificationBlockerActive.collectAsStateWithLifecycle()
    val screenGoalMins by viewModel.screenTimeGoalMinutes.collectAsStateWithLifecycle()
    val hasUsagePermission = viewModel.checkUsageStatsPermission()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("btn_settings_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "EXECUTIVE PREFERENCES",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "E-Ink display themes, shields & permissions",
                        style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // 1. E-Ink Paper Themes
        item {
            EInkSectionHeader(title = "E-Ink Display Theme")
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (theme in EInkThemePreset.values()) {
                    EInkCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderWidth = if (theme == currentTheme) 2.dp else 1.dp,
                        onClick = { viewModel.setTheme(theme) },
                        testTag = "theme_${theme.name.lowercase()}"
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = theme.displayName.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = theme.description,
                                    style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if (theme == currentTheme) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Grayscale & Visual Distraction Filters
        item {
            EInkSectionHeader(title = "Visual Distraction Reduction")
        }

        item {
            EInkCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleGrayscale() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Grayscale & E-Ink Filter",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Convert all visuals and UI to zero-saturation paper tones",
                                style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        EInkCheckbox(
                            checked = isGrayscale,
                            onCheckedChange = { viewModel.toggleGrayscale() },
                            testTag = "checkbox_grayscale"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    EInkSecondaryButton(
                        text = "Open System Grayscale / Bedtime Settings",
                        onClick = { viewModel.openSystemGrayscaleSettings(context) },
                        icon = Icons.Default.ColorLens,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. Notification Suppression Shield
        item {
            EInkSectionHeader(title = "Distraction Blocker Shield")
        }

        item {
            EInkCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleNotificationBlocker() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Suppress Non-Essential Notifications",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Intercept non-essential apps and store in Quiet Digest",
                                style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        EInkCheckbox(
                            checked = isBlockerActive,
                            onCheckedChange = { viewModel.toggleNotificationBlocker() },
                            testTag = "checkbox_notif_blocker"
                        )
                    }
                }
            }
        }

        // 4. Daily Screen Time Goal
        item {
            EInkSectionHeader(title = "Daily Screen Time Target")
        }

        item {
            EInkCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Screen Goal",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${screenGoalMins / 60}h ${screenGoalMins % 60}m",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = screenGoalMins.toFloat(),
                        onValueChange = { viewModel.setScreenTimeGoal(it.toInt()) },
                        valueRange = 60f..360f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }

        // 5. Android System Permissions & Always-Active Shield
        item {
            EInkSectionHeader(title = "System Integration & Battery Exemption")
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Notification Listener Access
                EInkCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.openNotificationListenerSettings(context) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notification Interception Access",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Allows filtering non-essential alerts into the quiet digest",
                                style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Usage Stats Access
                EInkCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.openUsageAccessSettings(context) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Usage Stats & Screen Time Access",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (hasUsagePermission) "Granted • Tracking digital wellbeing" else "Tap to grant permission in Settings",
                                style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Battery Optimization Exemption
                EInkCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.openBatterySettings(context) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exempt from Battery Optimization",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Ensures distraction shield stays active regardless of power settings",
                                style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Set as Default Launcher
                EInkCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Set as Default Home Launcher",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Replace cluttered phone homescreen with this distraction-free view",
                                style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
