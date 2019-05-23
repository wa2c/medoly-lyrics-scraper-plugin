package com.wa2c.android.medoly.plugin.action.lyricsscraper

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.wa2c.android.medoly.plugin.action.lyricsscraper.service.AbstractPluginService
import timber.log.Timber


/**
 * App.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            //Timber.plant(CrashlyticsTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        // Create channel
        AbstractPluginService.createChannel(this)
        // Migrator
        Migrator(this).versionUp()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this) // for Over 64K Methods
    }

}
