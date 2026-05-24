package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BatteryApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Koin dependency injection
        startKoin {
            androidLogger()
            androidContext(this@BatteryApp)
            modules(appModule)
        }

        // Setup notification channel
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Battery Full Notification"
            val descriptionText = "Triggers an alert when the battery is fully charged"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("battery_full_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
