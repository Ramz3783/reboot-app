package com.reboot.app

import android.app.Application
import com.reboot.app.data.repository.RebootRepository
import com.reboot.app.notifications.ensureNotificationChannel
import com.reboot.app.notifications.scheduleEveningStreakReminder

class RebootApp : Application() {
    lateinit var repository: RebootRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = RebootRepository.getInstance(this)
        ensureNotificationChannel(this)
        scheduleEveningStreakReminder(this)
    }
}
