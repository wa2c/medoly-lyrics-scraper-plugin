package com.wa2c.android.medoly.plugin.action.lyricsscraper

import android.app.Application
import android.content.Context
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Migrator
import timber.log.Timber
import android.support.multidex.MultiDex



/**
 * App.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Migrator(this).versionUp()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this) // for Over 64K Methods
    }

}
