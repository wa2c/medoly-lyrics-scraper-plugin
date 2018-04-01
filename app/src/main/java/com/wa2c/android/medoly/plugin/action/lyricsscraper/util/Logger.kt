package com.wa2c.android.medoly.plugin.action.lyricsscraper.util

import android.os.Debug
import android.util.Log

import com.wa2c.android.medoly.plugin.action.lyricsscraper.BuildConfig

import java.io.PrintWriter
import java.io.StringWriter


/**
 * Log utilities.
 */
object Logger {
    /** Tag name.  */
    private const val TAG = "Medoly"

    /**
     * Output debug message.
     *
     * @param msg message.
     */
    fun d(msg: Any?) {
        if (!BuildConfig.DEBUG) return

        Log.d(TAG, msg.toString())
    }

    /**
     * Output error message.
     *
     * @param msg message.
     */
    fun e(msg: Any?) {
        var message = msg
        if (!BuildConfig.DEBUG) return

        if (message is Exception) {
            val w = StringWriter()
            val pw = PrintWriter(w)
            message.printStackTrace(pw)
            pw.flush()
            message = w.toString()
        }

        Log.e(TAG, message.toString())
    }

    /**
     * Output information message.
     *
     * @param msg message.
     */
    fun i(msg: Any?) {
        if (!BuildConfig.DEBUG) return

        Log.i(TAG, msg.toString())
    }

    /**
     * Output verbose message.
     *
     * @param msg message.
     */
    fun v(msg: Any?) {
        if (!BuildConfig.DEBUG) return

        Log.v(TAG, msg.toString())
    }

    /**
     * Output warning message.
     *
     * @param msg message.
     */
    fun w(msg: Any?) {
        if (!BuildConfig.DEBUG) return

        Log.w(TAG, msg.toString())
    }

    /**
     * Output heap message.
     */
    fun heap() {
        if (!BuildConfig.DEBUG) return

        val msg = ("heap : Free=" + java.lang.Long.toString(Debug.getNativeHeapFreeSize() / 1024) + "kb"
                + ", Allocated=" + java.lang.Long.toString(Debug.getNativeHeapAllocatedSize() / 1024) + "kb" + ", Size="
                + java.lang.Long.toString(Debug.getNativeHeapSize() / 1024) + "kb")
        Log.v(TAG, msg)
    }

}
