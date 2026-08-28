package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.DistractionFreeApp
import com.example.MainActivity
import com.example.R
import com.example.system.DistractionShieldService

class FocusModeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_FOCUS) {
            val app = context.applicationContext as? DistractionFreeApp
            app?.repository?.toggleFocusMode()
            updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_TOGGLE_FOCUS = "com.example.distractionfree.ACTION_TOGGLE_FOCUS"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val app = context.applicationContext as? DistractionFreeApp
            val isFocus = app?.repository?.isFocusModeActive?.value ?: false
            val blocked = app?.repository?.todayBlockedCount?.value ?: 0

            val views = RemoteViews(context.packageName, R.layout.widget_focus_toggle)

            // Open app on header click
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openPendingIntent)

            // Toggle button
            val toggleIntent = Intent(context, FocusModeWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_FOCUS
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context,
                100,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_toggle_btn, togglePendingIntent)

            if (isFocus) {
                views.setTextViewText(R.id.widget_title, "FOCUS: ACTIVE")
                views.setTextViewText(R.id.widget_status, "Shield ON • $blocked blocked")
                views.setTextViewText(R.id.widget_toggle_btn, "PAUSE")
            } else {
                views.setTextViewText(R.id.widget_title, "E-INK FOCUS")
                views.setTextViewText(R.id.widget_status, "Distraction Shield: OFF")
                views.setTextViewText(R.id.widget_toggle_btn, "START")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, FocusModeWidgetProvider::class.java)
            )
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
