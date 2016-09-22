package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.wa2c.android.medoly.library.MediaProperty;
import com.wa2c.android.medoly.library.MedolyIntentParam;
import com.wa2c.android.medoly.library.PluginOperationCategory;
import com.wa2c.android.medoly.library.PluginTypeCategory;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;



/**
 *  歌詞取得サービス。
 */
public class ScraperIntentService extends IntentService {
    /** 前回のファイルパス設定キー。 */
    private static final String PREFKEY_PREVIOUS_MEDIA_URI = "previous_media_uri";
    /** 前回の歌詞テキスト設定キー。 */
    private static final String PREFKEY_PREVIOUS_LYRICS_TEXT = "previous_lyrics_text";
    /** 前回のサイトタイトル設定キー。 */
    private static final String PREFKEY_PREVIOUS_SITE_TITLE = "previous_site_text";
    /** 前回のサイトURI設定キー。 */
    private static final String PREFKEY_PREVIOUS_SITE_URI = "previous_site_uri";



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
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        startScraping(intent); // onStartCommand でUIスレッドが実行可能

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * 歌詞の取得を開始。
     * @param intent インテント。
     */
    private synchronized void startScraping(final Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        MedolyIntentParam param = null;

        try {
            param = new MedolyIntentParam(intent);
            if (param.hasCategories(PluginOperationCategory.OPERATION_MEDIA_OPEN)) {
                // Open
                if (!param.isEvent() || sharedPreferences.getBoolean(getApplicationContext().getString(R.string.prefkey_operation_media_open_enabled), false)) {
                    downloadLyrics(param);
                    return;
                }
            } else if (param.hasCategories(PluginOperationCategory.OPERATION_EXECUTE)) {
               // Execute
                if (param.hasExecuteId("execute_id_get_lyrics")) {
                    // Get Lyrics
                    downloadLyrics(param);
                    return;
               }
            }
            sendLyricsResult(param, Uri.EMPTY);
        } catch (Exception e) {
            AppUtils.showToast(this, R.string.error_app);
            sendLyricsResult(param, null);
        }
    }

    /**
     * 歌詞取得。
     */
    private void downloadLyrics(final MedolyIntentParam param) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // 音楽データ無し
        if (param.getMediaUri() == null) {
            AppUtils.showToast(getApplicationContext(), R.string.message_no_media);
            sendLyricsResult(param, Uri.EMPTY);
            return;
        }

        // 必須情報無し
        if (param.getPropertyData() == null ||
            param.getPropertyData().isEmpty(MediaProperty.TITLE) ||
            param.getPropertyData().isEmpty(MediaProperty.ARTIST)) {
            sendLyricsResult(param, null);
            return;
        }

        // 前回メディア確認
        final String mediaUriText = param.getMediaUri().toString();
        final String previousMediaUri = sharedPreferences.getString(PREFKEY_PREVIOUS_MEDIA_URI, "");
        boolean previousMediaEnabled = sharedPreferences.getBoolean(getApplicationContext().getString(R.string.prefkey_previous_media_enabled), false);
        if (!previousMediaEnabled && !TextUtils.isEmpty(mediaUriText) && !TextUtils.isEmpty(previousMediaUri) && mediaUriText.equals(previousMediaUri)) {
            // 前回と同じメディアは保存データを返す
            String lyrics = sharedPreferences.getString(PREFKEY_PREVIOUS_LYRICS_TEXT, null);
            String title = sharedPreferences.getString(PREFKEY_PREVIOUS_SITE_TITLE, null);
            String uri = sharedPreferences.getString(PREFKEY_PREVIOUS_SITE_URI, null);
            sendLyricsResult(param, getLyricsUri(lyrics), title, uri);
            return;
        }

        try {
            // 歌詞取得
            LyricsObtainClient obtainClient = new LyricsObtainClient(getApplicationContext(), param.getPropertyData());
            obtainClient.obtainLyrics(new LyricsObtainClient.LyricsObtainListener() {
                @Override
                public void onLyricsObtain(String lyrics, String title, String uri) {
                    // 送信
                    sharedPreferences.edit().putString(PREFKEY_PREVIOUS_MEDIA_URI, param.getMediaUri().toString()).apply();
                    sharedPreferences.edit().putString(PREFKEY_PREVIOUS_LYRICS_TEXT, lyrics).apply();
                    sharedPreferences.edit().putString(PREFKEY_PREVIOUS_SITE_TITLE, title).apply();
                    sharedPreferences.edit().putString(PREFKEY_PREVIOUS_SITE_URI, uri).apply();
                    sendLyricsResult(param, getLyricsUri(lyrics), title, uri);
                }
            });
        } catch (SiteNotSelectException e) {
            // サイト未選択の場合はアクティビティ起動
            Intent siteIntent = new Intent(this, SiteActivity.class);
            siteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(siteIntent);
            AppUtils.showToast(this, R.string.message_no_select_site);
        } catch (SiteNotFoundException e) {
            // サイト情報未取得の場合はアクティビティ起動
            Intent siteIntent = new Intent(this, SiteActivity.class);
            siteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(siteIntent);
            AppUtils.showToast(this, R.string.message_no_site);
        } catch (Exception e) {
            Logger.e(e);
            sendLyricsResult(param, null);
        }

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
        File lyricsDir = new File(getExternalCacheDir(), "lyrics");
        if (!lyricsDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            lyricsDir.mkdir();
        }

        // ファイル作成、URL取得
        File lyricsFile = new File(lyricsDir, "lyrics.txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(lyricsFile)));
            pw.println(lyrics);
            return Uri.fromFile(lyricsFile);
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
     * @param lyricsUri 歌詞データのURI。取得できない場合はUri.EMPTY、取得失敗の場合はnull (メッセージ有り)。
     * @param param 送信元パラメータ。
     */
    private void sendLyricsResult(MedolyIntentParam param, Uri lyricsUri) {
        sendLyricsResult(param, lyricsUri, null, null);
    }

    /**
     * 歌詞を送り返す。
     * @param param 送信元パラメータ。
     * @param lyricsUri 歌詞データのURI。無視の場合はUri.EMPTY、取得失敗の場合はnull (メッセージ有り)。
     * @param siteTitle サイトのタイトル。
     * @param siteUri  サイトのURI。
     */
    private void sendLyricsResult(MedolyIntentParam param, Uri lyricsUri, String siteTitle, String siteUri) {
        if (param == null)
            return;

        Intent returnIntent = param.createReturnIntent();
        returnIntent.addCategory(PluginTypeCategory.TYPE_PUT_LYRICS.getCategoryValue()); // カテゴリ
        returnIntent.putExtra(Intent.EXTRA_STREAM, lyricsUri);
        returnIntent.putExtra(Intent.EXTRA_TITLE, siteTitle);
        returnIntent.putExtra(Intent.EXTRA_ORIGINATING_URI, siteUri);
        returnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (lyricsUri != null) {
            getApplicationContext().grantUriPermission(returnIntent.getPackage(), lyricsUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        sendBroadcast(returnIntent);

        // Message
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (lyricsUri != null) {
            if (pref.getBoolean(getString(R.string.prefkey_success_message_show), false)) {
                AppUtils.showToast(this, R.string.message_lyrics_success);
            }
        } else if (lyricsUri != Uri.EMPTY) {
            if (pref.getBoolean(getString(R.string.prefkey_failure_message_show), false)) {
                AppUtils.showToast(this, R.string.message_lyrics_failure);
            }
        }
    }
}
