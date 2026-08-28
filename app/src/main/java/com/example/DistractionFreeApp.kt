package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.DistractionRepository

class DistractionFreeApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: DistractionRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        repository = DistractionRepository(this, database)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = CHANNEL_FOCUS_SHIELD
            val name = getString(R.string.channel_focus_name)
            val descriptionText = getString(R.string.channel_focus_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_FOCUS_SHIELD = "focus_shield_channel"
        lateinit var instance: DistractionFreeApp
            private set
    }
}
