package com.wa2c.android.medoly.plugin.action.lyricsscraper.util;

import android.os.Debug;
import android.util.Log;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.BuildConfig;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Log utilities.
 */
public class Logger {
    /** Tag name. */
    private static final String TAG = "Medoly";

    /**
     * Output debug message.
     *
     * @param msg message.
     */
    public static void d(Object msg) {
        if (!BuildConfig.DEBUG) return;

        Log.d(TAG, msg.toString());
    }

    /**
     * Output error message.
     *
     * @param msg message.
     */
    public static void e(Object msg) {
        if (!BuildConfig.DEBUG) return;

        if (msg instanceof Exception) {
            StringWriter w = new StringWriter();
            PrintWriter pw = new PrintWriter(w);
            ((Exception) msg).printStackTrace(pw);
            pw.flush();
            msg = w.toString();
        }

        Log.e(TAG, msg.toString());
    }

    /**
     * Output information message.
     *
     * @param msg message.
     */
    public static void i(Object msg) {
        if (!BuildConfig.DEBUG) return;

        Log.i(TAG, msg.toString());
    }

    /**
     * Output verbose message.
     *
     * @param msg message.
     */
    public static void v(Object msg) {
        if (!BuildConfig.DEBUG) return;

        Log.v(TAG, msg.toString());
    }

    /**
     * Output warning message.
     *
     * @param msg message.
     */
    public static void w(Object msg) {
        if (!BuildConfig.DEBUG) return;

        Log.w(TAG, msg.toString());
    }

    /**
     * Output heap message.
     */
    public static void heap() {
        if (!BuildConfig.DEBUG) return;

        String msg = "heap : Free=" + Long.toString(Debug.getNativeHeapFreeSize() / 1024) + "kb"
                + ", Allocated=" + Long.toString(Debug.getNativeHeapAllocatedSize() / 1024) + "kb" + ", Size="
                + Long.toString(Debug.getNativeHeapSize() / 1024) + "kb";
        Log.v(TAG, msg);
    }

}
