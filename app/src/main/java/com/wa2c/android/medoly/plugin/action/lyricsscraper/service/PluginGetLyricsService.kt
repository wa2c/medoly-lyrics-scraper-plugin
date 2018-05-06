package com.wa2c.android.medoly.plugin.action.lyricsscraper.service

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.preference.PreferenceManager

import com.wa2c.android.medoly.library.LyricsProperty
import com.wa2c.android.medoly.library.PropertyData
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.activity.SiteActivity
import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotFoundException
import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotSelectException
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.LyricsObtainClient2
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter


/**
 * 歌詞取得サービス。
 */
/**
 * コンストラクタ。
 */
class PluginGetLyricsService : AbstractPluginService(IntentService::class.java.simpleName) {

    override fun onHandleIntent(intent: Intent?) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // onStartCommand でUIスレッドが実行可能
        try {
            downloadLyrics()
        } catch (e: Exception) {
            Logger.e(e)
        }

        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    /**
     * 歌詞取得。
     */
    private fun downloadLyrics() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        try {
            // 歌詞取得
            val obtainClient = LyricsObtainClient2(applicationContext, propertyData)

            obtainClient.obtainLyrics(object: LyricsObtainClient2.LyricsObtainListener {
                override fun onLyricsObtain(lyrics: String?, title: String?, uri: String?) {
                    // 送信
                    sharedPreferences.edit().putString(PREFKEY_PREVIOUS_MEDIA_URI, propertyData.mediaUri.toString()).apply()
                    sharedPreferences.edit().putString(PREFKEY_PREVIOUS_LYRICS_TEXT, lyrics).apply()
                    sharedPreferences.edit().putString(PREFKEY_PREVIOUS_SITE_TITLE, title).apply()
                    sharedPreferences.edit().putString(PREFKEY_PREVIOUS_SITE_URI, uri).apply()
                    sendLyricsResult(getLyricsUri(lyrics), title, uri)
                }
            })
//            obtainClient.obtainLyrics { lyrics, title, uri ->
//                // 送信
//                sharedPreferences.edit().putString(PREFKEY_PREVIOUS_MEDIA_URI, propertyData!!.mediaUri.toString()).apply()
//                sharedPreferences.edit().putString(PREFKEY_PREVIOUS_LYRICS_TEXT, lyrics).apply()
//                sharedPreferences.edit().putString(PREFKEY_PREVIOUS_SITE_TITLE, title).apply()
//                sharedPreferences.edit().putString(PREFKEY_PREVIOUS_SITE_URI, uri).apply()
//                sendLyricsResult(getLyricsUri(lyrics), title, uri)
//            }
        } catch (e: SiteNotSelectException) {
            // サイト未選択の場合はアクティビティ起動
            val siteIntent = Intent(this, SiteActivity::class.java)
            siteIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(siteIntent)
            AppUtils.showToast(this, R.string.message_no_select_site)
            sendLyricsResult(null)
        } catch (e: SiteNotFoundException) {
            // サイト情報未取得の場合はアクティビティ起動
            val siteIntent = Intent(this, SiteActivity::class.java)
            siteIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(siteIntent)
            AppUtils.showToast(this, R.string.message_no_site)
            sendLyricsResult(null)
        } catch (e: Exception) {
            Logger.e(e)
            sendLyricsResult(null)
        }

    }

    /**
     * 歌詞を保存したファイルのURIを取得する。
     * @param lyrics 歌詞テキスト。
     * @return 歌詞ファイルのURI。
     */
    private fun getLyricsUri(lyrics: String?): Uri? {
        if (lyrics.isNullOrEmpty()) {
            return null
        }

        // フォルダ作成
        val lyricsDir = File(externalCacheDir, "lyrics")
        if (!lyricsDir.exists()) {
            lyricsDir.mkdir()
        }

        // ファイル作成、URL取得
        val lyricsFile = File(lyricsDir, "lyrics.txt")
        var pw: PrintWriter? = null
        try {
            pw = PrintWriter(BufferedWriter(FileWriter(lyricsFile)))
            pw.println(lyrics)
            return Uri.fromFile(lyricsFile)
        } catch (e: Exception) {
            Logger.e(e)
            return null
        } finally {
            if (pw != null)
                pw.close()
        }
    }

    /**
     * 歌詞を送り返す。
     * @param lyricsUri 歌詞データのURI。無視の場合はUri.EMPTY、取得失敗の場合はnull (メッセージ有り)。
     * @param siteTitle サイトのタイトル。
     * @param siteUri  サイトのURI。
     */
    private fun sendLyricsResult(lyricsUri: Uri?, siteTitle: String? = null, siteUri: String? = null) {
        val resultPropertyData = PropertyData()
        resultPropertyData[LyricsProperty.DATA_URI] = lyricsUri?.toString()
        resultPropertyData[LyricsProperty.SOURCE_TITLE] = siteTitle
        resultPropertyData[LyricsProperty.SOURCE_URI] = siteUri

        val returnIntent = pluginIntent.createResultIntent(resultPropertyData)
        applicationContext.grantUriPermission(pluginIntent.srcPackage, lyricsUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        returnIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        sendBroadcast(returnIntent)

        // Message
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        if (lyricsUri != null) {
            if (lyricsUri === Uri.EMPTY)
                return  // EMPTYは無視
            if (pref.getBoolean(getString(R.string.pref_success_message_show), false)) {
                AppUtils.showToast(this, R.string.message_lyrics_success)
            }
        } else {
            if (pref.getBoolean(getString(R.string.pref_failure_message_show), false)) {
                AppUtils.showToast(this, R.string.message_lyrics_failure)
            }
        }
    }

    companion object {
        /** 前回のファイルパス設定キー。  */
        const val PREFKEY_PREVIOUS_MEDIA_URI = "previous_media_uri"
        /** 前回の歌詞テキスト設定キー。  */
        const val PREFKEY_PREVIOUS_LYRICS_TEXT = "previous_lyrics_text"
        /** 前回のサイトタイトル設定キー。  */
        const val PREFKEY_PREVIOUS_SITE_TITLE = "previous_site_text"
        /** 前回のサイトURI設定キー。  */
        const val PREFKEY_PREVIOUS_SITE_URI = "previous_site_uri"
    }
}
