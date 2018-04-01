package com.wa2c.android.medoly.plugin.action.lyricsscraper.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Toast receiver.
 */
class ToastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, intent.getStringExtra(MESSAGE_TOAST), Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val MESSAGE_TOAST = "message"

        fun showToast(context: Context, stringId: Int) {
            val intent = Intent(context, ToastReceiver::class.java)
            intent.putExtra(MESSAGE_TOAST, context.getString(stringId))
            context.sendBroadcast(intent)
        }

        fun showToast(context: Context, text: String) {
            val intent = Intent(context, ToastReceiver::class.java)
            intent.putExtra(MESSAGE_TOAST, text)
            context.sendBroadcast(intent)
        }
    }

}