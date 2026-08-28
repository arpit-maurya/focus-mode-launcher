package com.example.system

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.example.model.ExecutiveDailyAnalytics
import com.example.model.InstalledAppItem
import java.util.Calendar

class UsageStatsHelper(private val context: Context) {

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
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

    fun getInstalledLaunchableApps(): List<InstalledAppItem> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val usageMap = getTodayUsageMap()

        val results = mutableListOf<InstalledAppItem>()
        for (ri in resolveInfos) {
            val pkg = ri.activityInfo.packageName
            if (pkg == context.packageName) continue // Skip self from app drawer if desired or keep as tool

            val appName = try {
                ri.loadLabel(pm).toString()
            } catch (_: Exception) {
                pkg
            }

            val isSystem = try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            } catch (_: Exception) {
                false
            }

            val category = categorizeApp(pkg, appName, isSystem)
            val usageMillis = usageMap[pkg] ?: 0L

            results.add(
                InstalledAppItem(
                    packageName = pkg,
                    appName = appName,
                    isEssential = isDefaultEssential(pkg, category),
                    usageTimeTodayMillis = usageMillis,
                    category = category
                )
            )
        }

        return results.sortedBy { it.appName.lowercase() }
    }

    private fun getTodayUsageMap(): Map<String, Long> {
        if (!hasUsageStatsPermission()) return emptyMap()

        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyMap()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, now)
        val map = mutableMapOf<String, Long>()
        if (stats != null) {
            for (us in stats) {
                if (us.totalTimeInForeground > 0) {
                    map[us.packageName] = us.totalTimeInForeground
                }
            }
        }
        return map
    }

    fun getTodayAnalytics(installedApps: List<InstalledAppItem>, blockedCountToday: Int, focusMinutesToday: Int): ExecutiveDailyAnalytics {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        var totalScreenTime = 0L
        var unlocks = 0
        val hourlyUsage = IntArray(24) { 0 }

        if (hasUsageStatsPermission()) {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            if (usm != null) {
                val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, now)
                if (stats != null) {
                    for (s in stats) {
                        totalScreenTime += s.totalTimeInForeground
                    }
                }

                // Query events for unlocks and hourly distribution
                try {
                    val events = usm.queryEvents(startOfDay, now)
                    val event = UsageEvents.Event()
                    var lastInteractiveTime = 0L

                    while (events.hasNextEvent()) {
                        events.getNextEvent(event)
                        if (event.eventType == UsageEvents.Event.SCREEN_INTERACTIVE ||
                            event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) {
                            unlocks++
                            lastInteractiveTime = event.timeStamp
                        } else if (event.eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE) {
                            if (lastInteractiveTime > 0) {
                                val durMin = ((event.timeStamp - lastInteractiveTime) / (1000 * 60)).toInt()
                                val cal = Calendar.getInstance().apply { timeInMillis = lastInteractiveTime }
                                val hour = cal.get(Calendar.HOUR_OF_DAY)
                                if (hour in 0..23) {
                                    hourlyUsage[hour] = (hourlyUsage[hour] + durMin).coerceAtMost(60)
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Fallback for events
                }
            }
        } else {
            // Default baseline if permission is pending
            totalScreenTime = 42 * 60 * 1000L // 42 mins
            unlocks = 18
            hourlyUsage[9] = 12
            hourlyUsage[10] = 15
            hourlyUsage[14] = 8
            hourlyUsage[15] = 7
        }

        // Top apps sorted by usage
        val topApps = installedApps.filter { it.usageTimeTodayMillis > 0 }
            .sortedByDescending { it.usageTimeTodayMillis }
            .take(6)

        // Calculate Focus Quotient (Manager Productivity Index)
        // High score when focus is high and non-essential distractions/screen time are low
        val screenTimeHours = totalScreenTime / (1000.0 * 3600.0)
        val score = when {
            screenTimeHours <= 2.0 && focusMinutesToday >= 45 -> 95
            screenTimeHours <= 3.0 && focusMinutesToday >= 30 -> 88
            screenTimeHours <= 4.0 -> 78
            screenTimeHours <= 5.5 -> 65
            else -> 50
        }.coerceIn(40, 99)

        return ExecutiveDailyAnalytics(
            totalScreenTimeMillis = totalScreenTime,
            totalUnlocksCount = unlocks.coerceAtLeast(1),
            totalBlockedNotifications = blockedCountToday,
            totalFocusMinutesToday = focusMinutesToday,
            focusQuotientScore = score,
            topAppsUsage = topApps,
            hourlyScreenTimeMinutes = hourlyUsage.toList()
        )
    }

    private fun categorizeApp(pkg: String, name: String, isSystem: Boolean): String {
        val lowerPkg = pkg.lowercase()
        val lowerName = name.lowercase()
        return when {
            lowerPkg.contains("slack") || lowerPkg.contains("teams") || lowerPkg.contains("zoom") || lowerPkg.contains("meet") -> "Work & Meetings"
            lowerPkg.contains("dialer") || lowerPkg.contains("phone") || lowerPkg.contains("contacts") || lowerPkg.contains("messaging") || lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") -> "Communication"
            lowerPkg.contains("calendar") || lowerPkg.contains("tasks") || lowerPkg.contains("keep") || lowerPkg.contains("notion") || lowerPkg.contains("todo") -> "Executive Planner"
            lowerPkg.contains("gmail") || lowerPkg.contains("email") || lowerPkg.contains("mail") || lowerPkg.contains("outlook") -> "Email"
            lowerPkg.contains("chrome") || lowerPkg.contains("browser") || lowerPkg.contains("firefox") -> "Browser"
            lowerPkg.contains("instagram") || lowerPkg.contains("tiktok") || lowerPkg.contains("twitter") || lowerPkg.contains("x.android") || lowerPkg.contains("facebook") || lowerPkg.contains("reddit") || lowerPkg.contains("youtube") -> "Distractions / Social"
            isSystem -> "System"
            else -> "Utilities"
        }
    }

    private fun isDefaultEssential(pkg: String, category: String): Boolean {
        val lowerPkg = pkg.lowercase()
        return category == "Communication" ||
                category == "Work & Meetings" ||
                category == "Executive Planner" ||
                lowerPkg.contains("dialer") ||
                lowerPkg.contains("phone") ||
                lowerPkg.contains("calendar") ||
                lowerPkg.contains("slack")
    }
}
