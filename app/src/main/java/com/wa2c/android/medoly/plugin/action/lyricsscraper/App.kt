package com.wa2c.android.medoly.plugin.action.lyricsscraper

import android.app.Application
import android.content.Context

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        versionUp(this)
    }

    private fun versionUp(context: Context) {

    }
}
