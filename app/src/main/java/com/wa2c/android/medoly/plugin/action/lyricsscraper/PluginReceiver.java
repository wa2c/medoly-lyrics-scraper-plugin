package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.webkit.WebView;

import com.wa2c.android.medoly.plugin.action.ActionPluginParam;
import com.wa2c.android.medoly.plugin.action.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;


/**
 * メッセージプラグイン受信レシーバ。
 */
public class PluginReceiver extends BroadcastReceiver {

    /** 前回のファイルパス設定キー。 */
    private static final String PREFKEY_PREVIOUS_MEDIA_URI = "previous_media_uri";

    /** コンテキスト。 */
    private Context context;
    /** 設定。 */
    private SharedPreferences sharedPreferences;

    /** Webページ表示用WebView。 */
    private WebView webView;


    /**
     * メッセージ受信。
     * @param context コンテキスト。
     * @param intent インテント。
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        Set<String> categories = intent.getCategories();
        if (categories == null || categories.size() == 0) {
            return;
        }

        if (!categories.contains(ActionPluginParam.PluginTypeCategory.TYPE_GET_LYRICS.getCategoryValue())) {
            return;
        }

        // 値を取得
        HashMap<String, String> propertyMap = null;
        boolean isEvent = false;
        try {
            if (intent.hasExtra(ActionPluginParam.PLUGIN_VALUE_KEY)) {
                Serializable serializable = intent.getSerializableExtra(ActionPluginParam.PLUGIN_VALUE_KEY);
                if (serializable != null) {
                    propertyMap = (HashMap<String, String>) serializable;
                }
            }
            if (propertyMap == null || propertyMap.isEmpty()) { return; }

            if (intent.hasExtra(ActionPluginParam.PLUGIN_EVENT_KEY))
                isEvent = intent.getBooleanExtra(ActionPluginParam.PLUGIN_EVENT_KEY, false);
        } catch (ClassCastException | NullPointerException e) {
            Logger.e(e);
            return;
        }

        if (categories.contains(ActionPluginParam.PluginOperationCategory.OPERATION_MEDIA_OPEN.getCategoryValue())) {
            // メディア開始
            getLyrics(intent, propertyMap);
        } else if (categories.contains(ActionPluginParam.PluginOperationCategory.OPERATION_EXECUTE.getCategoryValue())) {
            // 手動実行

        }
    }



    /**
     * 歌詞取得。
     * @param requestIntent 要求インテント。
     * @param requestPropertyMap 要求プロパティ。
     */
    private void getLyrics(final Intent requestIntent, final HashMap<String, String> requestPropertyMap) {
        // URIを取得
        Uri uri = requestIntent.getData();

        // 音楽データ無し
        if (uri == null) {
            AppUtils.showToast(context, R.string.message_no_media);
            return;
        }

        // 情報無し
        if (!requestPropertyMap.containsKey(ActionPluginParam.MediaProperty.TITLE.getKeyName()) &&
            !requestPropertyMap.containsKey(ActionPluginParam.MediaProperty.ARTIST.getKeyName())) {
            return;
        }

        // 前回と同じメディアかどうかチェック
        String mediaUri = uri.toString();
        String previousMediaUri = sharedPreferences.getString(PREFKEY_PREVIOUS_MEDIA_URI, "");
        boolean previousMediaEnabled = sharedPreferences.getBoolean(context.getString(R.string.prefkey_previous_media_enabled), false);
        if (!previousMediaEnabled && !TextUtils.isEmpty(mediaUri) && !TextUtils.isEmpty(previousMediaUri) && mediaUri.equals(previousMediaUri)) {
            return;
        }
        sharedPreferences.edit().putString(PREFKEY_PREVIOUS_MEDIA_URI, mediaUri).apply();


        LyricsObtainClient obtainClient = new LyricsObtainClient(context, requestPropertyMap);
        obtainClient.obtainLyrics(new LyricsObtainClient.LyricsObtainListener() {
            @Override
            public void onLyricsObtain(String lyrics) {
                if (!TextUtils.isEmpty(lyrics)) {
                    Intent intent = new Intent(ActionPluginParam.PLUGIN_ACTION); // プラグインアクション
                    intent.setPackage(ActionPluginParam.MEDOLY_PACKAGE); // パッケージ名
                    intent.addCategory(ActionPluginParam.PluginTypeCategory.TYPE_PUT_LYRICS.getCategoryValue()); // カテゴリ
                    intent.putExtra(ActionPluginParam.PLUGIN_ACTION_ID, requestIntent.getStringExtra(ActionPluginParam.PLUGIN_ACTION_ID)); // アクションID
                    intent.putExtra(ActionPluginParam.LyricsProperty.LYRICS.getKeyName(), lyrics); // 歌詞
                    context.sendBroadcast(intent);
                }
            }
        });
    }

}
