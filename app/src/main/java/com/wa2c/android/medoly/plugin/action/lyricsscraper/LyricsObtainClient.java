package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wa2c.android.medoly.library.MediaProperty;
import com.wa2c.android.medoly.library.PropertyData;
import com.wa2c.android.medoly.utils.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.EnumMap;
import java.util.EventListener;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.codecraft.xsoup.Xsoup;


public class LyricsObtainClient {

    /** Javascriptンターフェースオブジェクト名。 */
    private static final String JAVASCRIPT_INTERFACE = "android";
    /** 検索ページ取得スクリプト。 */
    private static final String SEARCH_PAGE_GET_SCRIPT = "javascript:window." + JAVASCRIPT_INTERFACE + ".getSearchResult(document.getElementsByTagName('html')[0].outerHTML);";
    /** 歌詞ページ取得スクリプト。 */
    private static final String LYRICS_PAGE_GET_SCRIPT = "javascript:window." + JAVASCRIPT_INTERFACE + ".getLyrics(document.getElementsByTagName('html')[0].outerHTML);";

    /** 初期状態。 */
    private static final int STATE_INIT = 0;
    /** 検索ページ取得中。 */
    private static final int STATE_SEARCH = 1;
    /** 歌詞ページ取得中。  */
    private static final int STATE_PAGE = 2;
    /** 完了済み。 */
    private static final int STATE_COMPLETE = 3;


    /** 要求プロパティ。 */
    private PropertyData requestPropertyMap;
    /** Webページ表示用WebView。 */
    private WebView webView;
    /** 現在の状態 */
    private int currentState = STATE_INIT;

    /** サイト情報。 */
    private EnumMap<SiteColumn, String> siteParam;



    /**
     * コンストラクタ。
     * @param context コンテキスト。
     * @param propertyData 要求元プロパティマップ。
     */
    public LyricsObtainClient(Context context, PropertyData propertyData) {
        this.requestPropertyMap = propertyData;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String siteId = preferences.getString(context.getString(R.string.prefkey_selected_site_id), "1");

        siteParam = new EnumMap<>(SiteColumn.class);
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    SiteProvider.SITE_URI,
                    null,
                    SiteColumn.SITE_ID.getColumnKey() + "=?",
                    new String[] { siteId },
                    null
            );
            if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst())
                throw new IndexOutOfBoundsException();

            for (SiteColumn col : SiteColumn.values()) {
                siteParam.put(col, cursor.getString(cursor.getColumnIndexOrThrow(col.getColumnKey())));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        this.webView = new WebView(context);
        this.webView.getSettings().setAppCacheEnabled(true);
        this.webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.webView.getSettings().setLoadsImagesAutomatically(false);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setUserAgentString(context.getString(R.string.app_user_agent));
        this.webView.setVisibility(View.INVISIBLE);
        this.webView.addJavascriptInterface(new JavaScriptInterface(), JAVASCRIPT_INTERFACE);
        this.webView.setWebChromeClient(new WebChromeClient());
        this.webView.setWebViewClient(new WebViewClient() {

            // ページ読み込み完了
            @Override
            public void onPageFinished(WebView view, String url) {
                // Ajax処理待ちの遅延
                long delay = 0;
                try {
                    delay = Long.valueOf(siteParam.get(SiteColumn.DELAY));
                } catch (NumberFormatException | NullPointerException e) {
                    Logger.e(e);
                }
                handler.postDelayed(executeScript, delay);
            }

            // エラー
            @Override
            public void onReceivedError (WebView view, int errorCode, String description, String failingUrl) {
                returnLyrics(null);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                returnLyrics(null);
            }



            // 同じビューで再読込
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            // スクリプトを実行してページを取得
            private final Runnable executeScript = new Runnable() {
                @Override
                public void run() {
                    if (currentState == STATE_SEARCH) {
                        // 歌詞検索結果の取得スクリプト実行
                        webView.loadUrl(SEARCH_PAGE_GET_SCRIPT);
                    } else if (currentState == STATE_PAGE) {
                        // 歌詞ページの取得スクリプト実行
                        webView.loadUrl(LYRICS_PAGE_GET_SCRIPT);
                    } else {
                        returnLyrics(null);
                    }
                }
            };
        });
    }





    /**
     * 歌詞を取得する。
     * @param listener 結果取得リスナ。
     */
    public void obtainLyrics(LyricsObtainListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }

        lyricsObtainListener = listener;
        final String searchUri = replaceUriTag(siteParam.get(SiteColumn.SEARCH_URI));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentState = STATE_SEARCH;
                webView.loadUrl(searchUri);
            }
        }, 1000);
    }

    /**
     * URIのタグを置換える。
     * @param inputUri 入力URI。
     * @return 置換え後のURI。
     */
    public String replaceUriTag(String inputUri) {
        // 正規表現作成
        StringBuilder regexpBuilder = new StringBuilder();
        boolean isFirst = true;
        for (MediaProperty p : MediaProperty.values()) {
            if (!isFirst) regexpBuilder.append("|");
            else isFirst = false;
            regexpBuilder.append("%").append(p.getKeyName()).append("%");
        }
        String regexp = "(" + regexpBuilder.toString() + ")";

        // URIを作成
        StringBuilder outputBuffer = new StringBuilder();
        Pattern pattern = Pattern.compile(regexp, Pattern.MULTILINE|Pattern.DOTALL);
        Matcher matcher = pattern.matcher(inputUri);
        int lastIndex = 0;
        while(matcher.find()){
            outputBuffer.append(inputUri.substring(lastIndex, matcher.start()));
            String tag = matcher.group(); // タグ (%KEY%)
            String key = tag.substring(1, tag.length() - 1); // プロパティキー (KEY)
            String val = requestPropertyMap.getFirst(key); // プロパティ値
            if (!TextUtils.isEmpty(val)) {
                try {
                    outputBuffer.append(URLEncoder.encode(AppUtils.normalizeText(val), siteParam.get(SiteColumn.RESULT_PAGE_URI_ENCODING)));
                } catch (UnsupportedEncodingException e) {
                    Logger.e(e);
                }
            }
            lastIndex = matcher.start() + tag.length();
        }
        outputBuffer.append(inputUri.substring(lastIndex));

        return outputBuffer.toString();
    }




    /** ハンドラ。 */
    final Handler handler = new Handler();

    /**
     * JavaScript側からコールバックされるオブジェクト。
     */
    final private class JavaScriptInterface {

        String url;

        /**
         * 歌詞の検索結果から歌詞ページを取得。
         * @param html HTMLソース。
         */
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void getSearchResult(String html) {

            try {
                 url = null;

                if (siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_XPATH)) {
                    // XPath
                    Document doc = Jsoup.parse(html);
                    Elements e = Xsoup.compile(siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TEXT)).evaluate(doc).getElements();
                    if (e == null || e.size() == 0) {
                        returnLyrics(null);
                        return;
                    }

                    // 歌詞ページのURLを取得
                    Element anchor = e.get(0);
                    url = anchor.attr("href");
                } else if (siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_REGEXP)) {
                    Pattern p = Pattern.compile(siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TEXT), Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(html);
                    if (m.find()) {
                        url = m.group(1);
                    }
                }

                if (TextUtils.isEmpty(url)) {
                    returnLyrics(null);
                    return;
                }

                // 歌詞取得
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentState = STATE_PAGE;
                        webView.loadUrl(url);
                    }
                }, 1000);
                // 歌詞再取得 (初回起動時に応答が返ってこない場合があるため)
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 未完了の場合は再実行
                        if (currentState != STATE_COMPLETE)
                            webView.loadUrl(url);
                    }
                }, 8000);
            } catch (Exception e) {
                Logger.e(e);
                returnLyrics(null);
            }
        }

        String lyrics;

        /**
         * 歌詞ページから歌詞を取得。
         * @param html HTMLソース。
         */
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void getLyrics(String html) {
            try {
                if (siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_XPATH)) {
                    // XPath
                    Document doc = Jsoup.parse(html);
                    Elements e = Xsoup.compile(siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TEXT)).evaluate(doc).getElements();
                    if (e == null || e.size() == 0) {
                        returnLyrics(null);
                        return;
                    }

                    Element elem = e.get(0);
                    lyrics = elem.html();
                } else if (siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_REGEXP)) {
                    // 正規表現
                    Pattern p = Pattern.compile(siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TEXT), Pattern.CASE_INSENSITIVE|Pattern.MULTILINE|Pattern.DOTALL);
                    Matcher m = p.matcher(html);
                    if (m.find()) {
                        lyrics = m.group(1);
                    }
                }

                // 調整
                lyrics = AppUtils.adjustLyrics(lyrics);

                // 歌詞を返す
                returnLyrics(lyrics);
            } catch (Exception e) {
                currentState = STATE_COMPLETE;
                Logger.e(e);
                returnLyrics(null);
            }
        }
    }

    /**
     * 取得した歌詞をイベントハンドラに渡して呼び出し元に返す。
     * @param lyrics 歌詞テキスト。
     */
    private void returnLyrics(String lyrics) {
        if (lyricsObtainListener != null) {
            lyricsObtainListener.onLyricsObtain(lyrics);
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (webView != null) {
                    webView.stopLoading();
                    webView.setWebChromeClient(null);
                    webView.setWebViewClient(null);
                    webView.destroy();
                    webView = null;
                }
            }
        });
    }





    /** リスナ。 */
    private LyricsObtainListener lyricsObtainListener;

    /**
     * リスナを設定する。
     * @param listener リスナ。
     */
    public void setOnLyricsObtainListener(LyricsObtainListener listener) {
        this.lyricsObtainListener = listener;
    }

    /**
     * 結果を取得するインターフェース。。
     */
    public interface LyricsObtainListener extends EventListener {
        /**
         * 取得イベント。
         * @param lyrics 歌詞。取得に失敗した場合はnull。
         */
        void onLyricsObtain(String lyrics);
    }

}

