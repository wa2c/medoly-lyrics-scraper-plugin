package com.wa2c.android.medoly.plugin.action.lyricsscraper.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import com.wa2c.android.medoly.library.ExtraData
import com.wa2c.android.medoly.library.MediaPluginIntent
import com.wa2c.android.medoly.library.PluginOperationCategory
import com.wa2c.android.medoly.library.PropertyData
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.prefs.Prefs
import timber.log.Timber


/**
 * Plugin service base.
 */
abstract class AbstractPluginService(name: String) : IntentService(name) {

    /** Context.  */
    protected lateinit var context: Context
    /** Preferences.  */
    protected lateinit var prefs: Prefs
    /** Plugin intent.  */
    protected lateinit var pluginIntent: MediaPluginIntent
    /** Property data.  */
    protected lateinit var propertyData: PropertyData
    /** Received class name.  */
    protected lateinit var receivedClassName: String
    /** True if result sent.  */
    protected var resultSent: Boolean = false
    /** Notification manager. */
    private var notificationManager : NotificationManager? = null
    /** True if initialized. */
    private var initialized = false



    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("onStartCommand")

        initialize(intent)

        return Service.START_NOT_STICKY
    }

    @SuppressLint("NewApi")
    override fun onHandleIntent(intent: Intent?) {
        Timber.d("onHandleIntent")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW)
                notificationManager!!.createNotificationChannel(channel)
                val builder = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("")
                        .setSmallIcon(R.drawable.ic_notification)
                startForeground(NOTIFICATION_ID, builder.build())
            }

            initialize(intent)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Initialized
     */
    private fun initialize(intent: Intent?) {
        synchronized(lock) {
            if (initialized)
                return

            resultSent = false
            context = applicationContext
            prefs = Prefs(this)

            if (intent != null) {
                pluginIntent = MediaPluginIntent(intent)
                propertyData = pluginIntent.propertyData ?: PropertyData()
                receivedClassName = pluginIntent.getStringExtra(RECEIVED_CLASS_NAME)
            }

            initialized = true
        }
    }

    @SuppressLint("NewApi")
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy" + this.javaClass.simpleName)

        if (notificationManager != null) {
            notificationManager?.cancel(NOTIFICATION_ID)
            stopForeground(true)
        }
        sendResult(null)
    }

    /**
     * Send result
     * @param resultProperty A result property data.
     * @param resultExtra A result extra data.
     */
    protected fun sendResult(resultProperty: PropertyData?, resultExtra: ExtraData? = null) {
        if (!resultSent && (this is PluginGetLyricsService)) {
            AppUtils.sendResult(this, pluginIntent, resultProperty, resultExtra)
            resultSent = true
        }
    }

    /**
     * Show message.
     */
    fun showMessage(result: CommandResult, succeededMessage: String?, failedMessage: String?) {
        if (result == CommandResult.NO_MEDIA) {
            AppUtils.showToast(context, R.string.message_no_media)
        } else if (result == CommandResult.SUCCEEDED && !succeededMessage.isNullOrEmpty()) {
            if (pluginIntent.hasCategory(PluginOperationCategory.OPERATION_EXECUTE) || prefs.getBoolean(R.string.pref_success_message_show, defRes = R.bool.pref_default_success_message_show)) {
                AppUtils.showToast(context, succeededMessage)
            }
        } else if (result == CommandResult.FAILED && !failedMessage.isNullOrEmpty()) {
            if (pluginIntent.hasCategory(PluginOperationCategory.OPERATION_EXECUTE) || prefs.getBoolean(R.string.pref_failure_message_show, defRes = R.bool.pref_default_failure_message_show)) {
                AppUtils.showToast(context, failedMessage)
            }
        }
    }



    companion object {
        /** Notification ID */
        private const val NOTIFICATION_ID = 1
        /** Notification Channel ID */
        private const val NOTIFICATION_CHANNEL_ID = "Notification"

        /** Received receiver class name.  */
        const val RECEIVED_CLASS_NAME = "RECEIVED_CLASS_NAME"

        private val lock = Object()

        /**
         * Create notification
         */
        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                return

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_MIN)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }


}
