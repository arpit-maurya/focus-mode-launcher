package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EInkThemePreset(val displayName: String, val description: String) {
    WARM_EPAPER("Warm E-Paper", "Muted sepia tones on warm paper for comfortable reading"),
    PURE_PAPER("Pure Matte Paper", "High contrast carbon ink on crisp paper canvas"),
    CHARCOAL_SLATE("Charcoal Slate", "Inverted dark e-ink theme for night focus"),
    ULTRA_CONTRAST("Brutalist Ink", "Absolute black and white for zero distraction")
}

enum class FocusModePreset(
    val title: String,
    val defaultMinutes: Int,
    val subtitle: String,
    val strictGrayscale: Boolean
) {
    DEEP_WORK("Deep Work Block", 25, "Executive Pomodoro sprint with strict filter", true),
    EXECUTIVE_SPRINT("Strategic Focus", 45, "Long uninterrupted focus session", true),
    MEETING_PREP("Meeting / Review", 60, "Calendar & Slack VIP only", false),
    CONTINUOUS_SHIELD("Indefinite Shield", 0, "Ongoing calm state without a timer", true)
}

@Entity(tableName = "app_shortcuts")
data class AppShortcut(
    @PrimaryKey val packageName: String,
    val appName: String,
    val customLabel: String = "",
    val isEssential: Boolean = false,
    val isPinned: Boolean = false,
    val orderIndex: Int = 0,
    val dailyLimitMinutes: Int = 0, // 0 = unlimited
    val category: String = "Productivity"
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val modeName: String,
    val blockedNotificationsCount: Int = 0,
    val completedSuccessfully: Boolean = true,
    val notes: String = ""
)

@Entity(tableName = "blocked_notifications")
data class BlockedNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "priority_tasks")
data class PriorityTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val priorityIndex: Int = 1, // 1 = Top Priority, 2 = Secondary, 3 = Third
    val createdAt: Long = System.currentTimeMillis()
)

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val customLabel: String = "",
    val isEssential: Boolean = false,
    val isPinned: Boolean = false,
    val dailyLimitMinutes: Int = 0,
    val usageTimeTodayMillis: Long = 0L,
    val launchCountToday: Int = 0,
    val category: String = "General"
) {
    val displayLabel: String
        get() = if (customLabel.isNotBlank()) customLabel else appName
}

data class ExecutiveDailyAnalytics(
    val totalScreenTimeMillis: Long = 0L,
    val totalUnlocksCount: Int = 0,
    val totalBlockedNotifications: Int = 0,
    val totalFocusMinutesToday: Int = 0,
    val focusQuotientScore: Int = 85, // 0-100 score
    val topAppsUsage: List<InstalledAppItem> = emptyList(),
    val hourlyScreenTimeMinutes: List<Int> = List(24) { 0 }
)
