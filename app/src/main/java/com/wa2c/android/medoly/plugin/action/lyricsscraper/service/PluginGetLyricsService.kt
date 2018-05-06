package com.wa2c.android.medoly.plugin.action.lyricsscraper.service

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.support.v4.content.FileProvider
import com.wa2c.android.medoly.library.LyricsProperty
import com.wa2c.android.medoly.library.PropertyData
import com.wa2c.android.medoly.plugin.action.lyricsscraper.BuildConfig
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.LyricsSearcherWebView
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.ResultItem
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger
import java.io.File


/**
 * Get lyrics plugin service.
 */
class PluginGetLyricsService : AbstractPluginService(IntentService::class.java.simpleName) {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Logger.d("onStartCommand")

        try {
            getLyrics()
        } catch (e: Exception) {
            Logger.e(e)
            //AppUtils.showToast(this, R.string.error_app);
        }

        return Service.START_NOT_STICKY
    }

    override fun onHandleIntent(intent: Intent?) {
        super.onHandleIntent(intent)
        Logger.d("onHandleIntent")

        // prevent service destroying
        val startTime = System.currentTimeMillis()
        val stopTime = startTime + resources.getInteger(R.integer.download_timeout_sec) * 1000
        while (!resultSent || (System.currentTimeMillis() >= stopTime)) {
            Thread.sleep(100)
        }
    }



    /**
     * Get lyrics.
     */
    private fun getLyrics() {
        val webView = LyricsSearcherWebView(this)
        webView.setOnHandleListener(object : LyricsSearcherWebView.HandleListener {
            var item: ResultItem? = null
            override fun onSearchResult(list: List<ResultItem>) {
                item = list.firstOrNull()
                if (item == null || item?.pageUrl == null) {
                    sendLyricsResult(null)

                }
                webView.download(item?.pageUrl!!)
            }
            override fun onGetLyrics(lyrics: String?) {
                item?.lyrics = lyrics
                sendLyricsResult(item)
            }
            override fun onError(message: String?) {
                if (message.isNullOrEmpty())
                    Logger.e(getString(R.string.message_lyrics_failure))
                else
                    Logger.d(message)
                sendLyricsResult(null)
            }
        })
        val selectedSiteId = prefs.getLong(R.string.prefkey_selected_site_id, -1)
        webView.search(propertyData, selectedSiteId)
    }

    /**
     * Send lyrics info.
     * @param resultItem search result.
     */
    private fun sendLyricsResult(resultItem: ResultItem?) {
        val resultPropertyData = PropertyData()
        if (!resultItem?.lyrics.isNullOrEmpty()) {
            val fileUri = saveLyricsFile(resultItem?.lyrics) // save lyrics and get uri
            resultPropertyData[LyricsProperty.DATA_URI] = fileUri?.toString()
            resultPropertyData[LyricsProperty.SOURCE_TITLE] = resultItem?.pageTitle
            resultPropertyData[LyricsProperty.SOURCE_URI] = resultItem?.pageUrl
            applicationContext.grantUriPermission(pluginIntent.srcPackage, fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (prefs.getBoolean(R.string.pref_success_message_show)) {
                AppUtils.showToast(this, R.string.message_lyrics_success)
            }
        } else {
            if (prefs.getBoolean(R.string.pref_failure_message_show)) {
                AppUtils.showToast(this, R.string.message_lyrics_failure)
            }
        }
        sendResult(resultPropertyData)
    }

    /**
     * Save lyrics file.
     * @param lyricsText Lyrics text.
     * @return Saved lyrics URI.
     */
    private fun saveLyricsFile(lyricsText: String?): Uri? {
        if (lyricsText.isNullOrEmpty()) {
            return null
        }
        // Create folder
        val sharedLyricsDir = File(this.filesDir, SHARED_DIR_NAME)
        if (!sharedLyricsDir.exists()) {
            sharedLyricsDir.mkdir()
        }
        val sharedLyricsFile = File(sharedLyricsDir, SHARED_FILE_NAME)
        sharedLyricsFile.writeText(lyricsText!!)

        return FileProvider.getUriForFile(this, PROVIDER_AUTHORITIES, sharedLyricsFile)
    }



    companion object {
        private const val SHARED_DIR_NAME = "lyrics"
        private const val SHARED_FILE_NAME = "lyrics.txt"
        private const val PROVIDER_AUTHORITIES = BuildConfig.APPLICATION_ID + ".fileprovider"
    }
}
