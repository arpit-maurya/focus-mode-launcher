package com.example.ui.launcher

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.HourglassFull
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppShortcut
import com.example.model.FocusModePreset
import com.example.model.PriorityTask
import com.example.ui.MainViewModel
import com.example.ui.components.BentoAppTile
import com.example.ui.components.BentoCardShape
import com.example.ui.components.BentoMetricCard
import com.example.ui.components.BentoSmallCardShape
import com.example.ui.components.BentoSquircleShape
import com.example.ui.components.EInkCard
import com.example.ui.components.EInkCheckbox
import com.example.ui.components.EInkMetricBox
import com.example.ui.components.EInkPrimaryButton
import com.example.ui.components.EInkSecondaryButton
import com.example.ui.components.EInkSectionHeader
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenAppDrawer: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFocusActive by viewModel.isFocusModeActive.collectAsStateWithLifecycle()
    val focusPreset by viewModel.currentFocusPreset.collectAsStateWithLifecycle()
    val remainingSec by viewModel.focusTimeRemainingSeconds.collectAsStateWithLifecycle()
    val pinnedShortcuts by viewModel.pinnedShortcuts.collectAsStateWithLifecycle()
    val priorityTasks by viewModel.priorityTasks.collectAsStateWithLifecycle()
    val blockedCount by viewModel.todayBlockedCount.collectAsStateWithLifecycle()
    val focusMinutesToday by viewModel.todayFocusMinutes.collectAsStateWithLifecycle()
    val analytics by viewModel.dailyAnalytics.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showPresetDialog by remember { mutableStateOf(false) }
    var showEditShortcutDialog by remember { mutableStateOf<AppShortcut?>(null) }

    // Live Clock State
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000)
        }
    }

    val screenHours = analytics.totalScreenTimeMillis / (1000 * 60 * 60)
    val screenMins = (analytics.totalScreenTimeMillis / (1000 * 60)) % 60
    val screenTimeDisplay = if (screenHours > 0) "${screenHours}h ${screenMins}m" else "${screenMins}m"
    val goalMinutes = viewModel.screenTimeGoalMinutes.value.coerceAtLeast(1)
    val totalScreenMin = (analytics.totalScreenTimeMillis / (1000 * 60)).toFloat()
    val screenProgress = (totalScreenMin / goalMinutes.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Bento Header (Clean Clock & Status Dot Action)
        item {
            BentoHeader(
                timeString = currentTimeString,
                dateString = currentDateString,
                isFocusActive = isFocusActive,
                onOpenSettings = onOpenSettings
            )
        }

        // 2. Bento Hero Card: System Status & Focus Shield
        item {
            BentoSystemStatusCard(
                isFocusActive = isFocusActive,
                currentPreset = focusPreset,
                remainingSeconds = remainingSec,
                blockedCount = blockedCount,
                onToggle = { viewModel.toggleFocusMode() },
                onSelectPreset = { showPresetDialog = true },
                onExtend = { viewModel.extendFocusSession(15) }
            )
        }

        // 3. Bento 2-Column Grid: Screen Time + Activity Pulse
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Bento: Screen Time
                BentoMetricCard(
                    title = "Screen Time",
                    value = screenTimeDisplay,
                    progress = screenProgress,
                    subtext = "Goal: ${goalMinutes / 60}h",
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenAnalytics),
                    testTag = "bento_metric_screen_time"
                )

                // Right Bento: Pulse (Daily momentum & quotient)
                val pulseSamples = listOf(0.35f, 0.65f, 1.0f, 0.55f, 0.4f)
                BentoMetricCard(
                    title = "Pulse",
                    value = "${analytics.focusQuotientScore}% Score",
                    pulseBars = pulseSamples,
                    subtext = "${focusMinutesToday}m Deep Work",
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenAnalytics),
                    testTag = "bento_metric_pulse"
                )
            }
        }

        // 4. Bento Full-Width Card: Essentials (Squircle App Grid)
        item {
            EInkCard(
                modifier = Modifier.fillMaxWidth(),
                shape = BentoCardShape,
                testTag = "bento_essentials_card"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ESSENTIALS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.6.sp
                            ),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = "APP DRAWER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(onClick = onOpenAppDrawer)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("btn_essentials_drawer")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val displayShortcuts = pinnedShortcuts.take(3)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        displayShortcuts.forEach { shortcut ->
                            val label = if (shortcut.customLabel.isNotBlank()) shortcut.customLabel else shortcut.appName
                            BentoAppTile(
                                label = label,
                                initialChar = label.take(1),
                                onClick = { viewModel.launchApp(context, shortcut.packageName) },
                                modifier = Modifier.weight(1f),
                                testTag = "bento_shortcut_${shortcut.packageName}"
                            )
                        }

                        // Add / Drawer Action Tile
                        BentoAppTile(
                            label = "Add",
                            icon = Icons.Default.Add,
                            isAddAction = true,
                            onClick = onOpenAppDrawer,
                            modifier = Modifier.weight(1f),
                            testTag = "bento_add_shortcut_tile"
                        )
                    }
                }
            }
        }

        // 5. Bento Full-Width Card: Executive Priorities Agenda
        item {
            EInkCard(
                modifier = Modifier.fillMaxWidth(),
                shape = BentoCardShape,
                testTag = "bento_priorities_card"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EXECUTIVE PRIORITIES",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.6.sp
                            ),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = "+ ADD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { showAddTaskDialog = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("btn_add_priority")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (priorityTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable { showAddTaskDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Define today's top strategic goals",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            priorityTasks.forEach { task ->
                                BentoPriorityTaskRow(
                                    task = task,
                                    onToggleComplete = { completed ->
                                        viewModel.toggleTaskCompleted(task.id, completed)
                                    },
                                    onDelete = {
                                        viewModel.deletePriorityTask(task.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAdd = { title, priority ->
                viewModel.addPriorityTask(title, priority)
                showAddTaskDialog = false
            }
        )
    }

    if (showPresetDialog) {
        FocusPresetDialog(
            currentPreset = focusPreset,
            onDismiss = { showPresetDialog = false },
            onSelect = { preset, duration ->
                viewModel.startFocusSession(preset, duration)
                showPresetDialog = false
            }
        )
    }

    if (showEditShortcutDialog != null) {
        val shortcut = showEditShortcutDialog!!
        EditShortcutDialog(
            shortcut = shortcut,
            onDismiss = { showEditShortcutDialog = null },
            onSave = { label, isEssential, limitMinutes ->
                viewModel.setAppCustomLabel(shortcut.packageName, label)
                viewModel.toggleAppEssential(shortcut.packageName, isEssential)
                viewModel.setAppDailyLimit(shortcut.packageName, limitMinutes)
                showEditShortcutDialog = null
            },
            onUnpin = {
                viewModel.toggleAppPinned(shortcut.packageName, false)
                showEditShortcutDialog = null
            }
        )
    }
}

@Composable
private fun BentoHeader(
    timeString: String,
    dateString: String,
    isFocusActive: Boolean,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 6.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 44.sp,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = dateString.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.8.sp
                ),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Circular Bento Status/Settings Button
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = MaterialTheme.colorScheme.primary),
                    onClick = onOpenSettings
                )
                .testTag("header_settings_btn"),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isFocusActive) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoSystemStatusCard(
    isFocusActive: Boolean,
    currentPreset: FocusModePreset,
    remainingSeconds: Int,
    blockedCount: Int,
    onToggle: () -> Unit,
    onSelectPreset: () -> Unit,
    onExtend: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status_pulse"
    )

    EInkCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isFocusActive) Modifier.alpha(alphaAnim) else Modifier),
        shape = BentoCardShape,
        borderWidth = if (isFocusActive) 1.5.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SYSTEM STATUS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.6.sp
                    ),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isFocusActive) "Focus Active" else "Distraction Shield",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))

                if (isFocusActive && remainingSeconds > 0) {
                    val mins = remainingSeconds / 60
                    val secs = remainingSeconds % 60
                    Text(
                        text = "%02d:%02d remaining · %d silenced".format(mins, secs, blockedCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = if (blockedCount > 0) "$blockedCount notifications silenced" else "${currentPreset.title} ready",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Bento Solid Black Toggle Button
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = MaterialTheme.colorScheme.onPrimary),
                        onClick = onToggle
                    )
                    .testTag("btn_toggle_focus"),
                shape = CircleShape,
                color = if (isFocusActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isFocusActive) "ON" else "OFF",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        ),
                        color = if (isFocusActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoPriorityTaskRow(
    task: PriorityTask,
    onToggleComplete: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            EInkCheckbox(
                checked = task.isCompleted,
                onCheckedChange = onToggleComplete,
                testTag = "task_check_${task.id}"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .size(32.dp)
                .testTag("task_delete_${task.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete task",
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "NEW EXECUTIVE OBJECTIVE",
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.4.sp)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal / Priority Item") },
                    placeholder = { Text("e.g. Finalize Q3 Strategy Deck") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_task_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        confirmButton = {
            EInkPrimaryButton(
                text = "ADD OBJECTIVE",
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, priority)
                    }
                },
                testTag = "btn_confirm_add_task"
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_add_task")
            ) {
                Text(
                    text = "CANCEL",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = BentoCardShape
    )
}

@Composable
private fun FocusPresetDialog(
    currentPreset: FocusModePreset,
    onDismiss: () -> Unit,
    onSelect: (FocusModePreset, Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "SELECT FOCUS PRESET",
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.4.sp)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (preset in FocusModePreset.values()) {
                    EInkCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = BentoSmallCardShape,
                        borderWidth = if (preset == currentPreset) 2.dp else 1.dp,
                        onClick = { onSelect(preset, preset.defaultMinutes) },
                        testTag = "preset_${preset.name.lowercase()}"
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (preset.defaultMinutes > 0) {
                                    Text(
                                        text = "${preset.defaultMinutes} MIN",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = preset.subtitle,
                                style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CLOSE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = BentoCardShape
    )
}

@Composable
private fun EditShortcutDialog(
    shortcut: AppShortcut,
    onDismiss: () -> Unit,
    onSave: (String, Boolean, Int) -> Unit,
    onUnpin: () -> Unit
) {
    var label by remember { mutableStateOf(shortcut.customLabel) }
    var isEssential by remember { mutableStateOf(shortcut.isEssential) }
    var limitMinutes by remember { mutableStateOf(shortcut.dailyLimitMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "CUSTOMIZE SHORTCUT",
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.4.sp)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Display Name") },
                    placeholder = { Text(shortcut.appName) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEssential = !isEssential }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EInkCheckbox(
                        checked = isEssential,
                        onCheckedChange = { isEssential = it }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Essential Manager App",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Never block notifications from this app",
                            style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                val primaryColor = MaterialTheme.colorScheme.primary
                Column {
                    Text(
                        text = "Daily App Limit: ${if (limitMinutes == 0) "No Limit" else "$limitMinutes mins"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (mins in listOf(0, 15, 30, 60)) {
                            OutlinedButton(
                                onClick = { limitMinutes = mins },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    if (limitMinutes == mins) 2.dp else 1.dp,
                                    primaryColor
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (mins == 0) "None" else "${mins}m",
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
                text = "SAVE",
                onClick = { onSave(label, isEssential, limitMinutes) }
            )
        },
        dismissButton = {
            Row {
                TextButton(onClick = onUnpin) {
                    Text(
                        text = "UNPIN",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "CANCEL",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = BentoCardShape
    )
}

