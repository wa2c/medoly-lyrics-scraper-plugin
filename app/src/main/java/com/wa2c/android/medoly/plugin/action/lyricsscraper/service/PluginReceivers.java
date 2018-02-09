package com.wa2c.android.medoly.plugin.action.lyricsscraper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Execute receiver.
 */
public class PluginReceivers {

    public static abstract class AbstractPluginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Intent serviceIntent = new Intent(intent);
            Class c = this.getClass();
            serviceIntent.putExtra(ProcessService.RECEIVED_CLASS_NAME, c.getName());

            if (c == EventGetLyricsReceiver.class ||
                c == ExecuteGetLyricsReceiver.class) {
                serviceIntent.setClass(context, ProcessService.class);
            }

            context.stopService(serviceIntent);
            context.startService(serviceIntent);
        }
    }

    // Event

    public static class EventGetLyricsReceiver extends AbstractPluginReceiver { }

    // Execution

    public static class ExecuteGetLyricsReceiver extends AbstractPluginReceiver { }

}
