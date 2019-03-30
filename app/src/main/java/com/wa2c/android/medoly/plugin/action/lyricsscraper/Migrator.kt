package com.wa2c.android.medoly.plugin.action.lyricsscraper

import android.content.Context
import com.wa2c.android.medoly.library.PluginOperationCategory
import com.wa2c.android.prefs.Prefs
import timber.log.Timber

/**
 * Migrator
 */
class Migrator(private val context: Context) {
    private val prefs = Prefs(context)

    /**
     * Get current app version code.
     * @return Current version.
     */
    private val currentVersionCode: Int
        get() {
            return try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionCode
            } catch (e: Exception) {
                Timber.d(e)
                0
            }
        }

    /**
     * Get saved app version code. (previous version)
     * @return Saved version.
     */
    private val savedVersionCode: Int
        get() = prefs.getInt(R.string.pref_app_previous_version, 0)

    /**
     * Save current version.
     */
    private fun saveCurrentVersionCode() {
        val version = currentVersionCode
        prefs[R.string.pref_app_previous_version] = version
    }



    /**
     * Version up.
     */
    fun versionUp(): Boolean {
        val prevVersionCode = savedVersionCode
        val currentVersionCode = currentVersionCode

        if (currentVersionCode <= prevVersionCode)
            return false

        // migration
        versionUpFrom0(prevVersionCode)
        versionUpFrom4(prevVersionCode)

        // save version
        saveCurrentVersionCode()
        return true
    }

    /**
     * Ver > Ver. 0 (0)
     */
    private fun versionUpFrom0(prevVersionCode: Int) {
        if (prevVersionCode > 0)
            return

        // delete db
        try {
            context.deleteDatabase("medoly_lyricsscraper")
        } catch (e: Exception) {
            Timber.d(e)
        }

        // deleteCache prefs
        prefs.remove("previous_media_enabled")
        prefs.remove("selected_id")
        prefs.remove("selected_site_id")

        // replace prefs
        if (prefs.contains("event_get_lyrics"))
            prefs.putString(R.string.pref_event_get_lyrics, prefs.getString("event_get_lyrics"))
        if (prefs.contains("success_message_show"))
            prefs.putBoolean(R.string.pref_success_message_show, prefs.getBoolean("success_message_show"))
        if (prefs.contains("failure_message_show"))
            prefs.putBoolean(R.string.pref_failure_message_show, prefs.getBoolean("failure_message_show"))
    }

    /**
     * Ver > Ver. 2.5.0 (4)
     */
    private fun versionUpFrom4(prevVersionCode: Int) {
        if (prevVersionCode > 4)
            return

        // Get Property
        val property: String? = prefs[R.string.pref_event_get_lyrics]
        if (property == "OPERATION_MEDIA_OPEN")
            prefs[R.string.pref_event_get_lyrics] = PluginOperationCategory.OPERATION_MEDIA_OPEN.categoryValue
        else if (property == "OPERATION_PLAY_START")
            prefs[R.string.pref_event_get_lyrics] = PluginOperationCategory.OPERATION_PLAY_START.categoryValue
    }

}
