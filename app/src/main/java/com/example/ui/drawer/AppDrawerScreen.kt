package com.example.ui.drawer

import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.InstalledAppItem
import com.example.ui.MainViewModel
import com.example.ui.components.EInkCard
import com.example.ui.components.EInkCheckbox
import com.example.ui.components.EInkPrimaryButton
import com.example.ui.components.EInkSecondaryButton

@Composable
fun AppDrawerScreen(
    viewModel: MainViewModel,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForOptions by remember { mutableStateOf<InstalledAppItem?>(null) }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                        it.customLabel.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("btn_drawer_back")
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
                    text = "ALL APPLICATIONS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "${filteredApps.size} apps • Pure Text Launcher",
                    style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter apps or categories...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("drawer_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // App List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredApps.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NO APPS FOUND",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try searching with a different name",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                items(filteredApps, key = { it.packageName }) { app ->
                    DrawerAppRow(
                        app = app,
                        onLaunch = { viewModel.launchApp(context, app.packageName) },
                        onOptionsClick = { selectedAppForOptions = app }
                    )
                }
            }
        }
    }

    // App Management Dialog
    if (selectedAppForOptions != null) {
        val app = selectedAppForOptions!!
        AppManagementDialog(
            app = app,
            onDismiss = { selectedAppForOptions = null },
            onLaunch = {
                viewModel.launchApp(context, app.packageName)
                selectedAppForOptions = null
            },
            onTogglePin = { isPinned ->
                viewModel.toggleAppPinned(app.packageName, isPinned)
                selectedAppForOptions = null
            },
            onToggleEssential = { isEssential ->
                viewModel.toggleAppEssential(app.packageName, isEssential)
                selectedAppForOptions = null
            },
            onSetDailyLimit = { limitMinutes ->
                viewModel.setAppDailyLimit(app.packageName, limitMinutes)
                selectedAppForOptions = null
            },
            onSaveCustomLabel = { newLabel ->
                viewModel.setAppCustomLabel(app.packageName, newLabel)
                selectedAppForOptions = null
            }
        )
    }
}

@Composable
private fun DrawerAppRow(
    app: InstalledAppItem,
    onLaunch: () -> Unit,
    onOptionsClick: () -> Unit
) {
    val usageMins = app.usageTimeTodayMillis / (1000 * 60)
    val usageText = if (usageMins > 0) "${usageMins}m today" else ""

    EInkCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onLaunch,
        testTag = "drawer_item_${app.packageName}"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.displayLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (app.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.category,
                        style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (usageText.isNotBlank()) {
                        Text(
                            text = " • $usageText",
                            style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (app.dailyLimitMinutes > 0) {
                        Text(
                            text = " • Limit: ${app.dailyLimitMinutes}m",
                            style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (app.isEssential) {
                    Text(
                        text = "VIP",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Text(
                    text = "MANAGE",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable(onClick = onOptionsClick)
                        .padding(6.dp)
                        .testTag("manage_app_${app.packageName}")
                )
            }
        }
    }
}

@Composable
private fun AppManagementDialog(
    app: InstalledAppItem,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onTogglePin: (Boolean) -> Unit,
    onToggleEssential: (Boolean) -> Unit,
    onSetDailyLimit: (Int) -> Unit,
    onSaveCustomLabel: (String) -> Unit
) {
    var customLabel by remember { mutableStateOf(app.customLabel) }
    var limitMins by remember { mutableStateOf(app.dailyLimitMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = app.appName.uppercase(),
                style = MaterialTheme.typography.labelLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Rename Field
                OutlinedTextField(
                    value = customLabel,
                    onValueChange = { customLabel = it },
                    label = { Text("Custom Launcher Name") },
                    placeholder = { Text(app.appName) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Pin to Home
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTogglePin(!app.isPinned) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EInkCheckbox(
                        checked = app.isPinned,
                        onCheckedChange = onTogglePin
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (app.isPinned) "Pinned to Home Shortcuts" else "Pin to Home Shortcuts",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Display on your minimalist home launcher",
                            style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Essential App Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleEssential(!app.isEssential) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EInkCheckbox(
                        checked = app.isEssential,
                        onCheckedChange = onToggleEssential
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Essential Manager App (VIP)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Bypass notification suppression during focus mode",
                            style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Daily App Timer
                val primaryColor = MaterialTheme.colorScheme.primary
                Column {
                    Text(
                        text = "Daily Screen Time Limit: ${if (limitMins == 0) "None" else "$limitMins min"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (mins in listOf(0, 10, 20, 45)) {
                            OutlinedButton(
                                onClick = {
                                    limitMins = mins
                                    onSetDailyLimit(mins)
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(
                                    if (limitMins == mins) 2.dp else 1.dp,
                                    primaryColor
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (mins == 0) "Off" else "${mins}m",
                                    fontSize = 11.sp,
                                    color = primaryColor
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            EInkPrimaryButton(
                text = "OPEN APP",
                onClick = onLaunch
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (customLabel != app.customLabel) {
                        onSaveCustomLabel(customLabel)
                    }
                    onDismiss()
                }
            ) {
                Text(
                    text = "SAVE & CLOSE",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp)
    )
}
