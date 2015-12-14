package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by wa2c on 2015/11/18.
 */
public class ToastReceiver extends BroadcastReceiver {
   // public static final String ACTION_TOAST = "com.wa2c.android.medoly.plugin.action.lyricsscraper.TOAST";
    public static final String MESSAGE_TOAST = "message";

    @Override
    public void onReceive(Context context, Intent intent) {
        //if (intent.getAction().equals(ACTION_TOAST)) {
            Toast.makeText(context, intent.getStringExtra(MESSAGE_TOAST), Toast.LENGTH_SHORT).show();
        //}
    }

    public static void showToast(Context context, int stringId) {
        //Intent intent = new Intent(ACTION_TOAST);
        Intent intent = new Intent(context, ToastReceiver.class);
        //intent.setClass(context, ToastReceiver.class);
        intent.putExtra(MESSAGE_TOAST, context.getString(stringId));
        context.sendBroadcast(intent);
    }

    public static void showToast(Context context, String text) {
        //Intent intent = new Intent(ACTION_TOAST);
        Intent intent = new Intent(context, ToastReceiver.class);
        //intent.setClass(context, ToastReceiver.class);
        intent.putExtra(MESSAGE_TOAST, text);
        context.sendBroadcast(intent);
    }



//    public static Intent createIntent(Context context, int message) {
//        Intent intent = new Intent(ACTION_TOAST);
//        intent.putExtra(MESSAGE_TOAST, context.getString(message));
//        return intent;
//    }
}