package com.wa2c.android.medoly.plugin.action.lyricsscraper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils;

/**
 * Execute receiver.
 */
public class PluginReceiver {

    public static class EventAllReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            AppUtils.startService(context, intent);
        }
    }

    public static class ExecuteGetLyricsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            AppUtils.startService(context, intent);
        }
    }

}
