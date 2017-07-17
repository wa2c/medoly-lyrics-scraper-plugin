package com.wa2c.android.medoly.plugin.action.lyricsscraper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.service.ScraperIntentService;

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
        serviceIntent.setClassName(ScraperIntentService.class.getPackage().getName(), ScraperIntentService.class.getName());
        context.startService(serviceIntent);
    }
}
