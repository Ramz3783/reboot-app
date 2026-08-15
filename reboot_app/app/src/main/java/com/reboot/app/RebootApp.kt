package com.reboot.app

import android.app.Application
import com.reboot.app.data.repository.RebootRepository

class RebootApp : Application() {
    lateinit var repository: RebootRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = RebootRepository.getInstance(this)
    }
}
