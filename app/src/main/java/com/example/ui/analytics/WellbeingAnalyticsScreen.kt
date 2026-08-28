package com.example.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BlockedNotification
import com.example.ui.MainViewModel
import com.example.ui.components.EInkCard
import com.example.ui.components.EInkMetricBox
import com.example.ui.components.EInkPrimaryButton
import com.example.ui.components.EInkSecondaryButton
import com.example.ui.components.EInkSectionHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WellbeingAnalyticsScreen(
    viewModel: MainViewModel,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.dailyAnalytics.collectAsStateWithLifecycle()
    val blockedNotifications by viewModel.blockedNotifications.collectAsStateWithLifecycle()
    val todayBlockedCount by viewModel.todayBlockedCount.collectAsStateWithLifecycle()
    val todayFocusMinutes by viewModel.todayFocusMinutes.collectAsStateWithLifecycle()
    val screenGoalMins by viewModel.screenTimeGoalMinutes.collectAsStateWithLifecycle()

    val screenHours = analytics.totalScreenTimeMillis / (1000 * 60 * 60)
    val screenMins = (analytics.totalScreenTimeMillis / (1000 * 60)) % 60
    val totalScreenMin = (analytics.totalScreenTimeMillis / (1000 * 60)).toInt()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
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
                        .testTag("btn_analytics_back")
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
                        text = "DIGITAL WELLBEING",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Executive Focus & Distraction Shield Metrics",
                        style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.labelMedium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // 1. Executive Productivity Card (Focus Quotient)
        item {
            EInkCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FOCUS QUOTIENT",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "${analytics.focusQuotientScore}%",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "EXECUTIVE STATUS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = if (analytics.focusQuotientScore >= 80) "OPTIMAL CLARITY" else "MODERATE DISTRACTION",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress vs Daily Limit
                    val screenProgress = (totalScreenMin.toFloat() / screenGoalMins.toFloat()).coerceIn(0f, 1f)
                    Text(
                        text = "Screen Time: ${screenHours}h ${screenMins}m / ${screenGoalMins / 60}h limit (${(screenProgress * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(screenProgress)
                                .height(8.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // 2. Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EInkMetricBox(
                    value = "${analytics.totalUnlocksCount}",
                    label = "Unlocks",
                    subtext = "Device Pickups",
                    modifier = Modifier.weight(1f)
                )
                EInkMetricBox(
                    value = "$todayBlockedCount",
                    label = "Shielded",
                    subtext = "Alerts Suppressed",
                    modifier = Modifier.weight(1f)
                )
                EInkMetricBox(
                    value = "${todayFocusMinutes}m",
                    label = "Deep Work",
                    subtext = "Intentional Time",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. 24-Hour Screen Time Activity Bar Chart
        item {
            EInkCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TODAY'S HOURLY ACTIVITY (24H)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val primaryColor = MaterialTheme.colorScheme.primary
                    val outlineColor = MaterialTheme.colorScheme.outline

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val barCount = 24
                        val maxMinutes = (analytics.hourlyScreenTimeMinutes.maxOrNull() ?: 15).coerceAtLeast(15)
                        val totalWidth = size.width
                        val chartHeight = size.height - 20.dp.toPx()
                        val barWidth = (totalWidth / barCount) * 0.7f
                        val stepX = totalWidth / barCount

                        // Draw baseline
                        drawLine(
                            color = outlineColor,
                            start = Offset(0f, chartHeight),
                            end = Offset(totalWidth, chartHeight),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Draw bars
                        analytics.hourlyScreenTimeMinutes.forEachIndexed { hour, minutes ->
                            val barHeight = (minutes.toFloat() / maxMinutes.toFloat()) * chartHeight
                            val x = (hour * stepX) + (stepX - barWidth) / 2f
                            val y = chartHeight - barHeight

                            if (minutes > 0) {
                                drawRect(
                                    color = primaryColor,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "00:00", style = MaterialTheme.typography.labelMedium, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "06:00", style = MaterialTheme.typography.labelMedium, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "12:00", style = MaterialTheme.typography.labelMedium, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "18:00", style = MaterialTheme.typography.labelMedium, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "23:00", style = MaterialTheme.typography.labelMedium, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        // 4. Top App Screen Time Breakdown
        if (analytics.topAppsUsage.isNotEmpty()) {
            item {
                EInkSectionHeader(title = "App Screen Time Ranking")
            }

            items(analytics.topAppsUsage, key = { it.packageName }) { app ->
                val appMins = app.usageTimeTodayMillis / (1000 * 60)
                val totalUsed = (analytics.totalScreenTimeMillis / (1000 * 60)).coerceAtLeast(1)
                val pct = ((appMins.toFloat() / totalUsed.toFloat()) * 100).toInt()

                EInkCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = app.displayLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${appMins}m ($pct%)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pct / 100f)
                                    .height(4.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }

        // 5. Blocked Notifications Digest (Quiet Batch Review)
        item {
            EInkSectionHeader(
                title = "Quiet Notification Digest",
                actionText = if (blockedNotifications.isNotEmpty()) "Clear All" else null,
                onActionClick = { viewModel.clearNotificationDigest() }
            )
        }

        if (blockedNotifications.isEmpty()) {
            item {
                EInkCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NO INTERCEPTED NOTIFICATIONS",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Distracting notifications from non-essential apps will appear here quietly for scheduled batch review",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(blockedNotifications.take(20), key = { it.id }) { notif ->
                BlockedNotificationItem(notification = notif)
            }
        }
    }
}

@Composable
private fun BlockedNotificationItem(notification: BlockedNotification) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormat.format(Date(notification.timestamp))

    EInkCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notification.appName.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (notification.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (notification.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
