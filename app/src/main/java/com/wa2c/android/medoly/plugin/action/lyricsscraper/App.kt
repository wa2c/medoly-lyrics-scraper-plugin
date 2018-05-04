package com.wa2c.android.medoly.plugin.action.lyricsscraper

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Prefs

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        versionUp(this)
    }

    private fun versionUp(context: Context) {
        val pref = Prefs(context)
        val previousVersion = pref.getInt(APP_PREVIOUS_VERSION, -1)

        // 4
        if (previousVersion < 0) {
            pref.remove("previous_media_enabled")
            pref.remove("selected_id")
            pref.remove("selected_site_id")

            if (pref.contains("event_get_lyrics"))
                pref.putValue(R.string.pref_event_get_lyrics, pref.getString("event_get_lyrics"))
            if (pref.contains("success_message_show"))
                pref.putValue(R.string.pref_success_message_show, pref.getBoolean("success_message_show"))
            if (pref.contains("failure_message_show"))
                pref.putValue(R.string.pref_failure_message_show, pref.getBoolean("failure_message_show"))
        }

        // update version
        try {
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            if (previousVersion < packageInfo.versionCode)
                pref.putValue(APP_PREVIOUS_VERSION, packageInfo.versionCode)
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.e(e)
        }
    }

    companion object {
        const val APP_PREVIOUS_VERSION = "pref_app_previous_version"
    }
}
