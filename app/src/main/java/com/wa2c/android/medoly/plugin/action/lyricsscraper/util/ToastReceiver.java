package com.wa2c.android.medoly.plugin.action.lyricsscraper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Toast出力用Receiver。
 */
public class ToastReceiver extends BroadcastReceiver {
    public static final String MESSAGE_TOAST = "message";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, intent.getStringExtra(MESSAGE_TOAST), Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int stringId) {
        Intent intent = new Intent(context, ToastReceiver.class);
        intent.putExtra(MESSAGE_TOAST, context.getString(stringId));
        context.sendBroadcast(intent);
    }

    public static void showToast(Context context, String text) {
        Intent intent = new Intent(context, ToastReceiver.class);
        intent.putExtra(MESSAGE_TOAST, text);
        context.sendBroadcast(intent);
    }

}