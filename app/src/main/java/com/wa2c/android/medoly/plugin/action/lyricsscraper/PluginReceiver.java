package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * メッセージプラグイン受信レシーバ。
 */
public class PluginReceiver extends BroadcastReceiver {
    /**
     * メッセージ受信。
     * @param context コンテキスト。
     * @param intent インテント。
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 既存のサービス強制停止
        Intent stopIntent = new Intent(context, ScraperIntentService.class);
        context.stopService(stopIntent);

        // IntentService起動
        Intent serviceIntent = new Intent(intent);
        serviceIntent.setClassName(ScraperIntentService.class.getPackage().getName(), ScraperIntentService.class.getName());
        context.startService(serviceIntent);
    }
}
