package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppShortcut
import com.example.model.BlockedNotification
import com.example.model.EInkThemePreset
import com.example.model.ExecutiveDailyAnalytics
import com.example.model.FocusModePreset
import com.example.model.FocusSession
import com.example.model.InstalledAppItem
import com.example.model.PriorityTask
import com.example.system.DistractionShieldService
import com.example.system.UsageStatsHelper
import com.example.widget.FocusModeWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DistractionRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("distraction_free_prefs", Context.MODE_PRIVATE)
    val usageHelper = UsageStatsHelper(context)

    // State flows
    private val _isFocusModeActive = MutableStateFlow(prefs.getBoolean("focus_active", false))
    val isFocusModeActive: StateFlow<Boolean> = _isFocusModeActive.asStateFlow()

    private val _currentFocusPreset = MutableStateFlow(
        try {
            FocusModePreset.valueOf(prefs.getString("focus_preset", FocusModePreset.DEEP_WORK.name) ?: FocusModePreset.DEEP_WORK.name)
        } catch (_: Exception) {
            FocusModePreset.DEEP_WORK
        }
    )
    val currentFocusPreset: StateFlow<FocusModePreset> = _currentFocusPreset.asStateFlow()

    private val _focusTimeRemainingSeconds = MutableStateFlow(prefs.getInt("focus_remaining_sec", 0))
    val focusTimeRemainingSeconds: StateFlow<Int> = _focusTimeRemainingSeconds.asStateFlow()

    private val _activeSessionStart = MutableStateFlow(prefs.getLong("focus_start_time", 0L))
    val activeSessionStart: StateFlow<Long> = _activeSessionStart.asStateFlow()

    private val _isNotificationBlockerActive = MutableStateFlow(prefs.getBoolean("notif_blocker_active", true))
    val isNotificationBlockerActive: StateFlow<Boolean> = _isNotificationBlockerActive.asStateFlow()

    private val _isGrayscaleEnabled = MutableStateFlow(prefs.getBoolean("grayscale_enabled", true))
    val isGrayscaleEnabled: StateFlow<Boolean> = _isGrayscaleEnabled.asStateFlow()

    private val _selectedTheme = MutableStateFlow(
        try {
            EInkThemePreset.valueOf(prefs.getString("eink_theme", EInkThemePreset.WARM_EPAPER.name) ?: EInkThemePreset.WARM_EPAPER.name)
        } catch (_: Exception) {
            EInkThemePreset.WARM_EPAPER
        }
    )
    val selectedTheme: StateFlow<EInkThemePreset> = _selectedTheme.asStateFlow()

    private val _screenTimeGoalMinutes = MutableStateFlow(prefs.getInt("screen_time_goal", 120)) // 2 hours
    val screenTimeGoalMinutes: StateFlow<Int> = _screenTimeGoalMinutes.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val _dailyAnalytics = MutableStateFlow(ExecutiveDailyAnalytics())
    val dailyAnalytics: StateFlow<ExecutiveDailyAnalytics> = _dailyAnalytics.asStateFlow()

    // Room Flows
    val pinnedShortcuts: Flow<List<AppShortcut>> = database.appShortcutDao().getPinnedShortcuts()
    val allShortcuts: Flow<List<AppShortcut>> = database.appShortcutDao().getAllShortcuts()
    val priorityTasks: Flow<List<PriorityTask>> = database.priorityTaskDao().getAllTasks()
    val blockedNotifications: Flow<List<BlockedNotification>> = database.notificationDao().getAllBlockedNotifications()

    private val startOfDayMillis: Long
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

    val todayBlockedCount: StateFlow<Int> = database.notificationDao()
        .getBlockedCountSince(startOfDayMillis)
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)

    val todayFocusMinutes: StateFlow<Int> = database.focusDao()
        .getTotalFocusMinutesSince(startOfDayMillis)
        .map { it ?: 0 }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        scope.launch {
            initDefaultData()
            refreshInstalledApps()
            refreshAnalytics()
            DistractionShieldService.startService(context)
        }
    }

    private suspend fun initDefaultData() {
        // Seed default priority tasks if empty
        val currentTasks = database.priorityTaskDao().getAllTasks().first()
        if (currentTasks.isEmpty()) {
            database.priorityTaskDao().insertTask(
                PriorityTask(title = "Review quarterly team OKRs & blockers", priorityIndex = 1)
            )
            database.priorityTaskDao().insertTask(
                PriorityTask(title = "Strategic roadmap deep work block (45m)", priorityIndex = 2)
            )
            database.priorityTaskDao().insertTask(
                PriorityTask(title = "1:1 Syncs with Engineering Leads", priorityIndex = 3)
            )
        }
    }

    fun refreshInstalledApps() {
        scope.launch {
            val apps = usageHelper.getInstalledLaunchableApps()
            val existingShortcuts = database.appShortcutDao().getAllShortcuts().first()
            val shortcutMap = existingShortcuts.associateBy { it.packageName }

            val merged = apps.map { app ->
                val shortcut = shortcutMap[app.packageName]
                if (shortcut != null) {
                    app.copy(
                        customLabel = shortcut.customLabel,
                        isEssential = shortcut.isEssential,
                        isPinned = shortcut.isPinned,
                        dailyLimitMinutes = shortcut.dailyLimitMinutes
                    )
                } else {
                    app
                }
            }

            _installedApps.value = merged

            // If no pinned shortcuts exist, pin default essential manager apps
            val pinned = merged.filter { it.isPinned }
            if (pinned.isEmpty()) {
                val defaultsToPin = merged.filter {
                    it.category == "Work & Meetings" ||
                            it.category == "Email" ||
                            it.category == "Executive Planner" ||
                            it.category == "Communication"
                }.take(5)

                defaultsToPin.forEachIndexed { index, appItem ->
                    database.appShortcutDao().insertOrUpdate(
                        AppShortcut(
                            packageName = appItem.packageName,
                            appName = appItem.appName,
                            isPinned = true,
                            isEssential = true,
                            orderIndex = index,
                            category = appItem.category
                        )
                    )
                }
            }

            refreshAnalytics()
        }
    }

    fun refreshAnalytics() {
        scope.launch {
            val blocked = todayBlockedCount.value
            val focusMins = todayFocusMinutes.value
            val analytics = usageHelper.getTodayAnalytics(_installedApps.value, blocked, focusMins)
            _dailyAnalytics.value = analytics
        }
    }

    fun toggleFocusMode(preset: FocusModePreset? = null) {
        if (_isFocusModeActive.value) {
            endFocusSession(completed = false)
        } else {
            val targetPreset = preset ?: _currentFocusPreset.value
            startFocusSession(targetPreset, targetPreset.defaultMinutes)
        }
    }

    fun startFocusSession(preset: FocusModePreset, durationMinutes: Int) {
        val now = System.currentTimeMillis()
        val totalSec = durationMinutes * 60

        _currentFocusPreset.value = preset
        _isFocusModeActive.value = true
        _activeSessionStart.value = now
        _focusTimeRemainingSeconds.value = totalSec

        prefs.edit()
            .putBoolean("focus_active", true)
            .putString("focus_preset", preset.name)
            .putLong("focus_start_time", now)
            .putInt("focus_remaining_sec", totalSec)
            .apply()

        DistractionShieldService.startService(context)
        FocusModeWidgetProvider.updateAllWidgets(context)
    }

    fun endFocusSession(completed: Boolean = true) {
        val startTime = _activeSessionStart.value
        val now = System.currentTimeMillis()
        val durationMins = if (startTime > 0) {
            ((now - startTime) / (1000 * 60)).toInt().coerceAtLeast(1)
        } else 0

        if (startTime > 0 && durationMins > 0) {
            scope.launch {
                database.focusDao().insertSession(
                    FocusSession(
                        startTime = startTime,
                        endTime = now,
                        durationMinutes = durationMins,
                        modeName = _currentFocusPreset.value.title,
                        completedSuccessfully = completed
                    )
                )
                refreshAnalytics()
            }
        }

        _isFocusModeActive.value = false
        _focusTimeRemainingSeconds.value = 0
        _activeSessionStart.value = 0L

        prefs.edit()
            .putBoolean("focus_active", false)
            .putInt("focus_remaining_sec", 0)
            .putLong("focus_start_time", 0L)
            .apply()

        DistractionShieldService.startService(context)
        FocusModeWidgetProvider.updateAllWidgets(context)
    }

    fun extendFocusSession(additionalMinutes: Int) {
        if (_isFocusModeActive.value) {
            val updated = _focusTimeRemainingSeconds.value + (additionalMinutes * 60)
            _focusTimeRemainingSeconds.value = updated
            prefs.edit().putInt("focus_remaining_sec", updated).apply()
        }
    }

    fun tickTimer() {
        if (_isFocusModeActive.value) {
            val currentSec = _focusTimeRemainingSeconds.value
            if (currentSec > 1) {
                _focusTimeRemainingSeconds.value = currentSec - 1
            } else if (currentSec == 1) {
                endFocusSession(completed = true)
            }
        }
    }

    fun toggleGrayscale() {
        val updated = !_isGrayscaleEnabled.value
        _isGrayscaleEnabled.value = updated
        prefs.edit().putBoolean("grayscale_enabled", updated).apply()
    }

    fun setTheme(theme: EInkThemePreset) {
        _selectedTheme.value = theme
        prefs.edit().putString("eink_theme", theme.name).apply()
    }

    fun toggleNotificationBlocker() {
        val updated = !_isNotificationBlockerActive.value
        _isNotificationBlockerActive.value = updated
        prefs.edit().putBoolean("notif_blocker_active", updated).apply()
    }

    fun setScreenTimeGoal(minutes: Int) {
        _screenTimeGoalMinutes.value = minutes
        prefs.edit().putInt("screen_time_goal", minutes).apply()
    }

    suspend fun isPackageEssential(packageName: String): Boolean {
        val shortcut = database.appShortcutDao().getShortcut(packageName)
        if (shortcut != null) return shortcut.isEssential

        val item = _installedApps.value.find { it.packageName == packageName }
        return item?.isEssential ?: false
    }

    fun getAppLabel(packageName: String): String {
        val item = _installedApps.value.find { it.packageName == packageName }
        return item?.displayLabel ?: packageName
    }

    suspend fun recordBlockedNotification(blocked: BlockedNotification) {
        database.notificationDao().insertBlockedNotification(blocked)
        refreshAnalytics()
    }

    suspend fun markAllNotificationsAsRead() {
        database.notificationDao().markAllAsRead()
    }

    suspend fun clearNotificationDigest() {
        database.notificationDao().clearAll()
        refreshAnalytics()
    }

    suspend fun toggleAppEssential(packageName: String, isEssential: Boolean) {
        val existing = database.appShortcutDao().getShortcut(packageName)
        if (existing != null) {
            database.appShortcutDao().setEssential(packageName, isEssential)
        } else {
            val app = _installedApps.value.find { it.packageName == packageName }
            if (app != null) {
                database.appShortcutDao().insertOrUpdate(
                    AppShortcut(
                        packageName = packageName,
                        appName = app.appName,
                        isEssential = isEssential,
                        category = app.category
                    )
                )
            }
        }
        refreshInstalledApps()
    }

    suspend fun toggleAppPinned(packageName: String, isPinned: Boolean) {
        val existing = database.appShortcutDao().getShortcut(packageName)
        if (existing != null) {
            database.appShortcutDao().setPinned(packageName, isPinned)
        } else {
            val app = _installedApps.value.find { it.packageName == packageName }
            if (app != null) {
                database.appShortcutDao().insertOrUpdate(
                    AppShortcut(
                        packageName = packageName,
                        appName = app.appName,
                        isPinned = isPinned,
                        category = app.category
                    )
                )
            }
        }
        refreshInstalledApps()
    }

    suspend fun setAppDailyLimit(packageName: String, limitMinutes: Int) {
        val existing = database.appShortcutDao().getShortcut(packageName)
        if (existing != null) {
            database.appShortcutDao().setDailyLimit(packageName, limitMinutes)
        } else {
            val app = _installedApps.value.find { it.packageName == packageName }
            if (app != null) {
                database.appShortcutDao().insertOrUpdate(
                    AppShortcut(
                        packageName = packageName,
                        appName = app.appName,
                        dailyLimitMinutes = limitMinutes,
                        category = app.category
                    )
                )
            }
        }
        refreshInstalledApps()
    }

    suspend fun setAppCustomLabel(packageName: String, label: String) {
        val existing = database.appShortcutDao().getShortcut(packageName)
        if (existing != null) {
            database.appShortcutDao().setCustomLabel(packageName, label)
        } else {
            val app = _installedApps.value.find { it.packageName == packageName }
            if (app != null) {
                database.appShortcutDao().insertOrUpdate(
                    AppShortcut(
                        packageName = packageName,
                        appName = app.appName,
                        customLabel = label,
                        category = app.category
                    )
                )
            }
        }
        refreshInstalledApps()
    }

    suspend fun addPriorityTask(title: String, priorityIndex: Int = 1) {
        database.priorityTaskDao().insertTask(
            PriorityTask(title = title, priorityIndex = priorityIndex)
        )
    }

    suspend fun toggleTaskCompleted(id: Long, completed: Boolean) {
        database.priorityTaskDao().setTaskCompleted(id, completed)
    }

    suspend fun deletePriorityTask(id: Long) {
        database.priorityTaskDao().deleteById(id)
    }
}
