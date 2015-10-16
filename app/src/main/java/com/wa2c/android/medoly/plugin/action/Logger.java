package com.wa2c.android.medoly.plugin.action;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.BuildConfig;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;


/**
 * ログ処理用クラス。
 */
public class Logger implements UncaughtExceptionHandler {
	/** エラーメッセージの保存キー。 */
	public static final String PREFKEY_ERROR_LOG = "ERROR_LOG";
	/** デバッグメッセージのタグ名。 */
	public static final String TAG = "MedolyLyricsScraper";

	/** コンテキスト。 */
	private static Context context = null;
	/** 標準の例外処理。 */
	private static final UncaughtExceptionHandler defaultHandler  = Thread.getDefaultUncaughtExceptionHandler();



	/**
	 * コンストラクタ
	 * @param context コンテキスト。
	 */
	public Logger(Context context){
		Logger.context = context;
	}

	/**
	 * 未処理例外の処理。
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		try {
			saveExceptionMessage(ex);
		} finally {
			// デフォルトの例外処理を実行
			defaultHandler.uncaughtException(thread, ex);
		}
	}

	/**
	 * 例外メッセージを書込む。
	 * @param ex 例外。
	 */
	public static void saveExceptionMessage(Throwable ex) {
		// スタックトレースを保存
		StringBuilder builder = new StringBuilder();
		builder.append(DateFormat.format("yyyy/MM/dd HH:mm:ss", new Date())).append("\n");
		builder.append(ex.getMessage()).append("\n");
		builder.append(Build.MANUFACTURER).append(" / ").append(Build.MODEL).append("\n");
		builder.append("-----------------------------\n");
		StackTraceElement[] stacks = ex.getStackTrace();
		for (StackTraceElement stack : stacks) {
			builder.append(stack.getClassName()).append("/");  // ファイル名
			builder.append(stack.getClassName()).append("#");  // クラス名
			builder.append(stack.getMethodName()).append(":"); // メソッド名
			builder.append(stack.getLineNumber());             // 行番行
			builder.append("\n");
		}
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		preference.edit().putString(PREFKEY_ERROR_LOG, builder.toString()).apply();
		ex.printStackTrace();
	}


	/**
	 * デバッグメッセージを出力する。
	 * @param msg メッセージ。
	 */
	public static void d(Object msg) {
		if (!BuildConfig.DEBUG) return;

		Log.d(TAG, msg.toString());
	}

	/**
	 * エラーメッセージを出力する。
	 * @param msg メッセージ。
	 */
	public static void e(Object msg) {
		if (!BuildConfig.DEBUG) return;

		if (msg instanceof Exception) {
			StringWriter w = new StringWriter();
			PrintWriter pw = new PrintWriter(w);
			((Exception)msg).printStackTrace(pw);
			pw.flush();
			msg = w.toString();
		}

		Log.e(TAG, msg.toString());
	}

	/**
	 * 情報メッセージを出力する。
	 * @param msg メッセージ。
	 */
	public static void i(Object msg) {
		if (!BuildConfig.DEBUG) return;

		Log.i(TAG, msg.toString());
	}

	/**
	 * 詳細メッセージを出力する。
	 * @param msg メッセージ。
	 */
	public static void v(Object msg) {
		if (!BuildConfig.DEBUG) return;

		Log.v(TAG, msg.toString());
	}

	/**
	 * 警告メッセージを出力する。
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
