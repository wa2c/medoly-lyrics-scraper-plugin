package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
 *  歌詞取得サービス。
 */
public class ScraperIntentService extends IntentService {
    /** 前回のファイルパス設定キー。 */
    private static final String PREFKEY_PREVIOUS_MEDIA_URI = "previous_media_uri";
    /** 前回の歌詞テキスト設定キー。 */
    private static final String PREFKEY_PREVIOUS_LYRICS_TEXT = "previous_lyrics_text";
    /** コンテキスト。 */
    private Context context;
    /** 設定。 */
    private SharedPreferences sharedPreferences;



    /**
     * コンストラクタ。
     */
    public ScraperIntentService() {
        super(IntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        // MEMO: onStartイベントのみ、UIスレッドが実行可能

        this.context = this;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // アクションIDを取得
        String actionId = intent.getStringExtra(ActionPluginParam.PLUGIN_ACTION_ID);
        String srcPackage = intent.getStringExtra(ActionPluginParam.PLUGIN_SRC_PACKAGE);
        if (TextUtils.isEmpty(actionId) || TextUtils.isEmpty(srcPackage)) {
            return;
        }

        // 戻り先インテント
        Intent returnIntent = new Intent(ActionPluginParam.PLUGIN_ACTION);
        returnIntent.setPackage(srcPackage);
        returnIntent.putExtra(ActionPluginParam.PLUGIN_ACTION_ID, actionId);

        // カテゴリ取得
        Set<String> categories = intent.getCategories();
        if (categories == null || categories.size() == 0) {
            sendLyricsResult(returnIntent, Uri.EMPTY);
            return;
        }

        // 自動実行がOFFの場合は終了
        if (categories.contains(ActionPluginParam.PluginOperationCategory.OPERATION_MEDIA_OPEN.getCategoryValue()) &&
                !sharedPreferences.getBoolean(context.getString(R.string.prefkey_operation_media_open_enabled), false)) {
            sendLyricsResult(returnIntent, Uri.EMPTY);
            return;
        }

        // URIを取得
        Uri mediaUri = null;
        if (intent.getExtras() != null) {
            Object extraStream = intent.getExtras().get(Intent.EXTRA_STREAM);
            if (extraStream != null && extraStream instanceof Uri)
                mediaUri = (Uri)extraStream;
        } else if (intent.getData() != null) {
            // Old version
            mediaUri = intent.getData();
        }

        // 音楽データ無し
        if (mediaUri == null) {
            AppUtils.showToast(context, R.string.message_no_media);
            sendLyricsResult(returnIntent, Uri.EMPTY);
            return;
        }

        // 前回と同じメディアかどうかチェック
        final String mediaUriText = mediaUri.toString();
        final String previousMediaUri = sharedPreferences.getString(PREFKEY_PREVIOUS_MEDIA_URI, "");
        boolean previousMediaEnabled = sharedPreferences.getBoolean(context.getString(R.string.prefkey_previous_media_enabled), false);
        if (!previousMediaEnabled && !TextUtils.isEmpty(mediaUriText) && !TextUtils.isEmpty(previousMediaUri) && mediaUriText.equals(previousMediaUri)) {
            // 前回と同じURI
            String lyrics = sharedPreferences.getString(PREFKEY_PREVIOUS_LYRICS_TEXT, null);
            sendLyricsResult(returnIntent, getLyricsUri(lyrics));
            return;
        }

        // 値を取得
        HashMap<String, String> propertyMap = null;
        try {
            if (intent.hasExtra(ActionPluginParam.PLUGIN_VALUE_KEY)) {
                Serializable serializable = intent.getSerializableExtra(ActionPluginParam.PLUGIN_VALUE_KEY);
                if (serializable != null) {
                    propertyMap = (HashMap<String, String>) serializable;
                }
            }
            if (propertyMap == null || propertyMap.isEmpty()) {
                sendLyricsResult(returnIntent, null);
                return;
            }

            // 情報無し
            if (!propertyMap.containsKey(ActionPluginParam.MediaProperty.TITLE.getKeyName()) &&
                    !propertyMap.containsKey(ActionPluginParam.MediaProperty.ARTIST.getKeyName())) {
                sendLyricsResult(returnIntent, null);
                return;
            }
        } catch (ClassCastException | NullPointerException e) {
            Logger.e(e);
            sendLyricsResult(returnIntent, null);
            return;
        }

        if (categories.contains(ActionPluginParam.PluginOperationCategory.OPERATION_EXECUTE.getCategoryValue())) {
            // Execute
            final String EXECUTE_GET_LYRICS_ID = "execute_id_get_lyrics";

            // Execute
            Bundle extras = intent.getExtras();
            if (extras != null && extras.keySet().contains(EXECUTE_GET_LYRICS_ID)) {
                // Get Lyrics
                downloadLyrics(returnIntent, mediaUri, propertyMap);


            } else {
                sendLyricsResult(returnIntent, null);
            }
        } else {
            // Event
            downloadLyrics(returnIntent, mediaUri, propertyMap);
        }
    }



    /**
     * 歌詞取得。
     * @param returnIntent  戻りインテント。
     * @param mediaUri URI。
     * @param requestPropertyMap プロパティ情報。
     */
    private void downloadLyrics(final Intent returnIntent, final Uri mediaUri, final HashMap<String, String> requestPropertyMap) {
        (new Handler()).post(
            new Runnable() {
                public void run() {
                    try {
                        // 歌詞取得
                        LyricsObtainClient obtainClient = new LyricsObtainClient(context, requestPropertyMap);
                        obtainClient.obtainLyrics(new LyricsObtainClient.LyricsObtainListener() {
                            @Override
                            public void onLyricsObtain(String lyrics) {
                                // 送信
                                sharedPreferences.edit().putString(PREFKEY_PREVIOUS_MEDIA_URI, mediaUri.toString()).apply();
                                sharedPreferences.edit().putString(PREFKEY_PREVIOUS_LYRICS_TEXT, lyrics).apply();

                                // 送信
                                sendLyricsResult(returnIntent, getLyricsUri(lyrics));
                            }
                        });
                    } catch (Exception e) {
                        sendLyricsResult(returnIntent, null);
                    }
                }
            }
        );
    }

    /**
     * 歌詞を保存したファイルのURIを取得する。
     * @param lyrics 歌詞テキスト。
     * @return 歌詞ファイルのURI。
     */
    private Uri getLyricsUri(String lyrics) {
        if (TextUtils.isEmpty(lyrics)) {
            return null;
        }

        // フォルダ作成
        File lyricsDir = new File(getFilesDir(), "lyrics");
        if (!lyricsDir.exists()) {
            lyricsDir.mkdir();
        }

        // ファイル作成、URL取得
        File lyricsFile = new File(lyricsDir, "lyrics.txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(lyricsFile)));
            pw.println(lyrics);
            return FileProvider.getUriForFile(this, getString(R.string.package_name), lyricsFile);
        } catch(Exception e) {
            Logger.e(e);
            return null;
        } finally {
            if (pw != null)
                pw.close();
        }
    }

    /**
     * 歌詞を送り返す。
     * @param returnIntent 戻りインテント。
     * @param lyricsUri 歌詞データのURI。取得できない場合はUri.EMPTY、取得失敗の場合はnull (メッセージ有り)。
     */
    public void sendLyricsResult(@NonNull Intent returnIntent, Uri lyricsUri) {
        returnIntent.addCategory(ActionPluginParam.PluginTypeCategory.TYPE_PUT_LYRICS.getCategoryValue()); // カテゴリ
        returnIntent.putExtra(Intent.EXTRA_STREAM, lyricsUri);
        returnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (lyricsUri != null) {
            context.grantUriPermission(returnIntent.getPackage(), lyricsUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        context.sendBroadcast(returnIntent);
    }
}
