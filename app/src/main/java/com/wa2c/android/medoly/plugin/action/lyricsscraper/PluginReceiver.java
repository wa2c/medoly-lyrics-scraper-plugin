package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.wa2c.android.medoly.plugin.action.ActionPluginParam;
import com.wa2c.android.medoly.plugin.action.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;


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
        // MEMO: BroadcastReceiverは10秒以上でスレッドが中断される場合があるので、処理をサービスに委譲する
        Intent serviceIntent = new Intent(intent);
        serviceIntent.setClassName(ScraperIntentService.class.getPackage().getName(), ScraperIntentService.class.getName());
        context.startService(serviceIntent);
    }
}
