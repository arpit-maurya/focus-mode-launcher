package com.example.system

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.DistractionFreeApp
import com.example.model.BlockedNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationSuppressorService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        // Never block our own app or critical telephony/alarms
        if (packageName == applicationContext.packageName) return

        val app = application as? DistractionFreeApp ?: return
        val repo = app.repository

        // Check if shield / focus mode is active
        val isShieldActive = repo.isFocusModeActive.value || repo.isNotificationBlockerActive.value
        if (!isShieldActive) return

        val notification = sbn.notification ?: return
        val extras = notification.extras

        val isCall = notification.category == Notification.CATEGORY_CALL ||
                notification.category == Notification.CATEGORY_ALARM ||
                (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0

        // Allow ongoing calls and alarms
        if (isCall) return

        serviceScope.launch {
            val isEssential = repo.isPackageEssential(packageName)
            if (!isEssential) {
                // Intercept & cancel the distracting notification
                try {
                    cancelNotification(sbn.key)
                } catch (_: Exception) {
                    // Fallback
                }

                val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                val appLabel = repo.getAppLabel(packageName)

                val blocked = BlockedNotification(
                    packageName = packageName,
                    appName = appLabel,
                    title = title,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )

                repo.recordBlockedNotification(blocked)

                // Update notification widget
                val updateIntent = Intent(this@NotificationSuppressorService, com.example.widget.FocusModeWidgetProvider::class.java).apply {
                    action = "android.appwidget.action.APPWIDGET_UPDATE"
                }
                sendBroadcast(updateIntent)
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }
}
