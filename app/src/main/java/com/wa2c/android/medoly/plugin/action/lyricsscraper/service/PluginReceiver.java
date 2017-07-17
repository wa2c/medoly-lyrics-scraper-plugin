package com.wa2c.android.medoly.plugin.action.lyricsscraper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Plugin receiver.
 */
public class PluginReceiver extends BroadcastReceiver {
    /**
     * Receive message.
     * @param context A context.
     * @param intent Received intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Stop exists service.
        Intent stopIntent = new Intent(context, ScraperIntentService.class);
        context.stopService(stopIntent);

        // Launch intent service.
        Intent serviceIntent = new Intent(intent);
        serviceIntent.setClass(context, ScraperIntentService.class);
        context.startService(serviceIntent);
    }
}
