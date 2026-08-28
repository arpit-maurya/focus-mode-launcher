package com.example.system

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.DistractionFreeApp
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DistractionShieldService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_FOCUS -> {
                val app = application as? DistractionFreeApp
                app?.repository?.toggleFocusMode()
            }
        }

        startForegroundNotification()
        monitorFocusTimer()

        return START_STICKY
    }

    private fun startForegroundNotification() {
        val app = application as? DistractionFreeApp
        val repo = app?.repository
        val isFocus = repo?.isFocusModeActive?.value ?: false
        val blockedCount = repo?.todayBlockedCount?.value ?: 0
        val remainingSec = repo?.focusTimeRemainingSeconds?.value ?: 0

        val timeString = if (isFocus && remainingSec > 0) {
            val mins = remainingSec / 60
            val secs = remainingSec % 60
            " | %02d:%02d".format(mins, secs)
        } else ""

        val statusText = if (isFocus) {
            "Executive Focus Active$timeString • $blockedCount distractions blocked"
        } else {
            "Distraction Shield Standing By • $blockedCount distractions blocked today"
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, DistractionShieldService::class.java).apply {
            action = ACTION_TOGGLE_FOCUS
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, DistractionFreeApp.CHANNEL_FOCUS_SHIELD)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Distraction Free Shield")
            .setContentText(statusText)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                0,
                if (isFocus) "Pause Focus" else "Start Deep Work",
                togglePendingIntent
            )

        startForeground(NOTIFICATION_ID, builder.build())
    }

    private fun monitorFocusTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                val app = application as? DistractionFreeApp
                val repo = app?.repository
                if (repo != null) {
                    repo.tickTimer()
                    startForegroundNotification()
                }
            }
        }
    }

    override fun onDestroy() {
        timerJob?.cancel()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 9001
        const val ACTION_START_SERVICE = "com.example.distractionfree.START_SHIELD"
        const val ACTION_STOP_SERVICE = "com.example.distractionfree.STOP_SHIELD"
        const val ACTION_TOGGLE_FOCUS = "com.example.distractionfree.ACTION_TOGGLE_FOCUS"

        fun startService(context: Context) {
            val intent = Intent(context, DistractionShieldService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, DistractionShieldService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
