package com.wa2c.android.medoly.plugin.action.lyricsscraper.util;

import android.os.Debug;
import android.util.Log;

import com.wa2c.android.medoly.library.BuildConfig;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * ログ処理用クラス。
 */
public class Logger {
    /**
     * デバッグメッセージのタグ名。
     */
    public static final String TAG = "Medoly";


    /**
     * デバッグメッセージを出力する。
     *
     * @param msg メッセージ。
     */
    public static void d(Object msg) {
        if (!BuildConfig.DEBUG) return;

        Log.d(TAG, msg.toString());
    }

    /**
     * エラーメッセージを出力する。
     *
     * @param msg メッセージ。
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
     * 情報メッセージを出力する。
     *
     * @param msg メッセージ。
     */
    public static void i(Object msg) {
        if (!BuildConfig.DEBUG) return;

        Log.i(TAG, msg.toString());
    }

    /**
     * 詳細メッセージを出力する。
     *
     * @param msg メッセージ。
     */
    public static void v(Object msg) {
        if (!BuildConfig.DEBUG) return;

        Log.v(TAG, msg.toString());
    }

    /**
     * 警告メッセージを出力する。
     *
     * @param msg メッセージ。
     */
    public static void w(Object msg) {
        if (!BuildConfig.DEBUG) return;

        Log.w(TAG, msg.toString());
    }

    /**
     * デバッグメッセージを出力する。
     */
    public static void heap() {
        if (!BuildConfig.DEBUG) return;

        String msg = "heap : Free=" + Long.toString(Debug.getNativeHeapFreeSize() / 1024) + "kb"
                + ", Allocated=" + Long.toString(Debug.getNativeHeapAllocatedSize() / 1024) + "kb" + ", Size="
                + Long.toString(Debug.getNativeHeapSize() / 1024) + "kb";
        Log.v(TAG, msg);
    }

}
