package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DistractionFreeApp
import com.example.data.DistractionRepository
import com.example.model.AppShortcut
import com.example.model.BlockedNotification
import com.example.model.EInkThemePreset
import com.example.model.ExecutiveDailyAnalytics
import com.example.model.FocusModePreset
import com.example.model.InstalledAppItem
import com.example.model.PriorityTask
import com.example.system.GrayscaleHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DistractionRepository = (application as DistractionFreeApp).repository

    val isFocusModeActive: StateFlow<Boolean> = repository.isFocusModeActive
    val currentFocusPreset: StateFlow<FocusModePreset> = repository.currentFocusPreset
    val focusTimeRemainingSeconds: StateFlow<Int> = repository.focusTimeRemainingSeconds
    val isNotificationBlockerActive: StateFlow<Boolean> = repository.isNotificationBlockerActive
    val isGrayscaleEnabled: StateFlow<Boolean> = repository.isGrayscaleEnabled
    val selectedTheme: StateFlow<EInkThemePreset> = repository.selectedTheme
    val screenTimeGoalMinutes: StateFlow<Int> = repository.screenTimeGoalMinutes

    val installedApps: StateFlow<List<InstalledAppItem>> = repository.installedApps
    val dailyAnalytics: StateFlow<ExecutiveDailyAnalytics> = repository.dailyAnalytics
    val todayBlockedCount: StateFlow<Int> = repository.todayBlockedCount
    val todayFocusMinutes: StateFlow<Int> = repository.todayFocusMinutes

    val pinnedShortcuts: StateFlow<List<AppShortcut>> = repository.pinnedShortcuts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val priorityTasks: StateFlow<List<PriorityTask>> = repository.priorityTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedNotifications: StateFlow<List<BlockedNotification>> = repository.blockedNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFocusMode(preset: FocusModePreset? = null) {
        repository.toggleFocusMode(preset)
    }

    fun startFocusSession(preset: FocusModePreset, durationMinutes: Int) {
        repository.startFocusSession(preset, durationMinutes)
    }

    fun endFocusSession(completed: Boolean = true) {
        repository.endFocusSession(completed)
    }

    fun extendFocusSession(additionalMinutes: Int = 15) {
        repository.extendFocusSession(additionalMinutes)
    }

    fun toggleGrayscale() {
        repository.toggleGrayscale()
    }

    fun setTheme(theme: EInkThemePreset) {
        repository.setTheme(theme)
    }

    fun toggleNotificationBlocker() {
        repository.toggleNotificationBlocker()
    }

    fun setScreenTimeGoal(minutes: Int) {
        repository.setScreenTimeGoal(minutes)
    }

    fun addPriorityTask(title: String, priority: Int = 1) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addPriorityTask(title.trim(), priority)
        }
    }

    fun toggleTaskCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(id, completed)
        }
    }

    fun deletePriorityTask(id: Long) {
        viewModelScope.launch {
            repository.deletePriorityTask(id)
        }
    }

    fun toggleAppEssential(packageName: String, isEssential: Boolean) {
        viewModelScope.launch {
            repository.toggleAppEssential(packageName, isEssential)
        }
    }

    fun toggleAppPinned(packageName: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.toggleAppPinned(packageName, isPinned)
        }
    }

    fun setAppDailyLimit(packageName: String, limitMinutes: Int) {
        viewModelScope.launch {
            repository.setAppDailyLimit(packageName, limitMinutes)
        }
    }

    fun setAppCustomLabel(packageName: String, customLabel: String) {
        viewModelScope.launch {
            repository.setAppCustomLabel(packageName, customLabel.trim())
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearNotificationDigest() {
        viewModelScope.launch {
            repository.clearNotificationDigest()
        }
    }

    fun refreshData() {
        repository.refreshInstalledApps()
        repository.refreshAnalytics()
    }

    fun launchApp(context: Context, packageName: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                Toast.makeText(context, "Cannot launch app: $packageName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening app: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission helpers
    fun checkUsageStatsPermission(): Boolean = repository.usageHelper.hasUsageStatsPermission()
    fun openUsageAccessSettings(context: Context) = repository.usageHelper.openUsageAccessSettings()
    fun openNotificationListenerSettings(context: Context) = GrayscaleHelper.openNotificationListenerSettings(context)
    fun openDndSettings(context: Context) = GrayscaleHelper.openDndSettings(context)
    fun openBatterySettings(context: Context) = GrayscaleHelper.openBatteryOptimizationSettings(context)
    fun openSystemGrayscaleSettings(context: Context) = GrayscaleHelper.openSystemGrayscaleSettings(context)
}
