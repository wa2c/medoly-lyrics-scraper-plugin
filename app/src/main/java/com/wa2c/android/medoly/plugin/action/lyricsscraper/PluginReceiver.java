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
        // MEMO: BroadcastReceiverは10秒以上でスレッドが中断される場合があるので、処理をIntentServiceに委譲する
        Intent serviceIntent = new Intent(intent);
        serviceIntent.setClassName(com.wa2c.android.medoly.plugin.action.lyricsscraper.ScraperIntentService.class.getPackage().getName(), com.wa2c.android.medoly.plugin.action.lyricsscraper.ScraperIntentService.class.getName());
        context.startService(serviceIntent);
    }
}
