package com.example.system

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.graphics.ColorMatrix

object GrayscaleHelper {

    val EINK_GRAYSCALE_MATRIX: ColorMatrix by lazy {
        ColorMatrix().apply {
            setToSaturation(0f)
        }
    }

    val EINK_HIGH_CONTRAST_MATRIX: ColorMatrix by lazy {
        // High contrast monochrome matrix
        val matrix = floatArrayOf(
            1.5f, 1.5f, 1.5f, 0f, -128f,
            1.5f, 1.5f, 1.5f, 0f, -128f,
            1.5f, 1.5f, 1.5f, 0f, -128f,
            0f,   0f,   0f,   1f, 0f
        )
        ColorMatrix(matrix)
    }

    val WARM_EPAPER_MATRIX: ColorMatrix by lazy {
        // Soft sepia warmth with desaturation
        val matrix = floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f,     0f,     0f,     1f, 0f
        )
        ColorMatrix(matrix)
    }

    fun openSystemGrayscaleSettings(context: Context) {
        val intents = listOf(
            Intent("android.settings.COLOR_CORRECTION_SETTINGS"),
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
            Intent(Settings.ACTION_DISPLAY_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Try next
            }
        }
    }

    fun openNotificationListenerSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
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

    fun openDndSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
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

    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
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
}
