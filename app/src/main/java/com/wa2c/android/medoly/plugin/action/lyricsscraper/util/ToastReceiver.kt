package com.wa2c.android.medoly.plugin.action.lyricsscraper.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import timber.log.Timber

/**
 * Toast receiver.
 */
class ToastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //Toast.makeText(context, intent.getStringExtra(MESSAGE_TOAST), Toast.LENGTH_SHORT).show()
        DynamicToast.make(context, intent.getStringExtra(MESSAGE_TOAST), ContextCompat.getDrawable(context, R.drawable.ic_launcher)).show()
    }

    companion object {
        private const val MESSAGE_TOAST = "message"

        fun showToast(context: Context, stringId: Int) {
            showToast(context, context.getString(stringId))
        }

        fun showToast(context: Context, text: String) {
            val intent = Intent(context, ToastReceiver::class.java)
            intent.putExtra(MESSAGE_TOAST, text)
            context.sendBroadcast(intent)
            Timber.d(text)
        }
    }

}
