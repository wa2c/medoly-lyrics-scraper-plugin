package com.wa2c.android.medoly.plugin.action.lyricsscraper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import com.wa2c.android.medoly.library.MediaPluginIntent
import com.wa2c.android.medoly.library.MediaProperty
import com.wa2c.android.medoly.library.PluginOperationCategory
import com.wa2c.android.medoly.library.PluginTypeCategory
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Prefs


/**
 * Execute receiver.
 */
class PluginReceiver {

    abstract class AbstractPluginReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Logger.d("onReceive: " + this.javaClass.simpleName)

            val pluginIntent = MediaPluginIntent(intent)
            val propertyData = pluginIntent.propertyData ?: return
            val prefs = Prefs(context)

            if (this is EventGetLyricsReceiver ||
                this is ExecuteGetLyricsReceiver) {

                // checks
                if (!pluginIntent.hasCategory(PluginTypeCategory.TYPE_GET_LYRICS)) {
                    return
                }
                // media
                if (propertyData.isMediaEmpty) {
                    AppUtils.showToast(context, R.string.message_no_media)
                    AppUtils.sendResult(context, pluginIntent)
                    return
                }
                // property
                if (propertyData.getFirst(MediaProperty.TITLE).isNullOrEmpty() || propertyData.getFirst(MediaProperty.ARTIST).isNullOrEmpty()) {
                    AppUtils.sendResult(context, pluginIntent)
                    return
                }


//
//                if (pluginIntent!!.hasCategory(PluginOperationCategory.OPERATION_EXECUTE)) {
//                    val receivedClassName = pluginIntent!!.getStringExtra(PluginGetLyricsService.RECEIVED_CLASS_NAME)
//                    if (receivedClassName == PluginReceiver.ExecuteGetLyricsReceiver::class.java!!.getName()) {
//                        downloadLyrics()
//                    }
//                    return
//                }
//
//                // Event
//
//                if (pluginIntent!!.hasCategory(PluginTypeCategory.TYPE_GET_LYRICS)) {
//                    val operation = sharedPreferences!!.getString(getString(R.string.prefkey_event_get_lyrics), getString(R.string.event_get_lyrics_values_default))
//                    if (pluginIntent!!.hasCategory(PluginOperationCategory.OPERATION_MEDIA_OPEN) && PluginOperationCategory.OPERATION_MEDIA_OPEN.name == operation || pluginIntent!!.hasCategory(PluginOperationCategory.OPERATION_PLAY_START) && PluginOperationCategory.OPERATION_PLAY_START.name == operation) {
//                        downloadLyrics()
//                    } else {
//                        sendLyricsResult(Uri.EMPTY)
//                    }
//                }
//
//
//                // 音楽データ無し
//                if (propertyData.mediaUri == null) {
//                    AppUtils.showToast(applicationContext, R.string.message_no_media)
//                    sendLyricsResult(Uri.EMPTY)
//                    return
//                }
//
//                // 必須情報無し
//                if (propertyData == null ||
//                        propertyData!!.isEmpty(MediaProperty.TITLE) ||
//                        propertyData!!.isEmpty(MediaProperty.ARTIST)) {
//                    sendLyricsResult(null)
//                    return
//                }
//
//
//                // 前回メディア確認
//                val mediaUriText = propertyData!!.mediaUri.toString()
//                val previousMediaUri = sharedPreferences.getString(PluginGetLyricsService.PREFKEY_PREVIOUS_MEDIA_URI, "")
//                val previousMediaEnabled = sharedPreferences.getBoolean(applicationContext.getString(R.string.prefkey_previous_media_enabled), false)
//                if (!previousMediaEnabled && !TextUtils.isEmpty(mediaUriText) && !TextUtils.isEmpty(previousMediaUri) && mediaUriText == previousMediaUri) {
//                    // 前回と同じメディアは保存データを返す
//                    val lyrics = sharedPreferences.getString(PluginGetLyricsService.PREFKEY_PREVIOUS_LYRICS_TEXT, null)
//                    val title = sharedPreferences.getString(PluginGetLyricsService.PREFKEY_PREVIOUS_SITE_TITLE, null)
//                    val uri = sharedPreferences.getString(PluginGetLyricsService.PREFKEY_PREVIOUS_SITE_URI, null)
//                    sendLyricsResult(getLyricsUri(lyrics), title, uri)
//                    return
//                }


//                val operation = try { PluginOperationCategory.valueOf(prefs.getString(R.string.pref_event_get_lyrics)!!) } catch (ignore : Exception) { null }
//                if (!pluginIntent.hasCategory(PluginOperationCategory.OPERATION_EXECUTE) && !pluginIntent.hasCategory(operation)) {
//                    AppUtils.sendResult(context, pluginIntent)
//                    return
//                }

                // service
                pluginIntent.setClass(context, PluginReceiver::class.java)
            }

            pluginIntent.putExtra(AbstractPluginService.RECEIVED_CLASS_NAME, this.javaClass.name)
            context.stopService(pluginIntent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(pluginIntent)
            } else {
                context.startService(pluginIntent)
            }


//            val serviceIntent = Intent(intent)
//            val c = this.javaClass
//            serviceIntent.putExtra(PluginGetLyricsService.RECEIVED_CLASS_NAME, c.getName())
//
//            if (c == EventGetLyricsReceiver::class.java || c == ExecuteGetLyricsReceiver::class.java) {
//                serviceIntent.setClass(context, PluginGetLyricsService::class.java!!)
//            }
//
//            context.stopService(serviceIntent)
//            context.startService(serviceIntent)
        }
    }

    // Event

    class EventGetLyricsReceiver : AbstractPluginReceiver()

    // Execution

    class ExecuteGetLyricsReceiver : AbstractPluginReceiver()

}
