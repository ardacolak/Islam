package com.example.islam

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IslamApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
