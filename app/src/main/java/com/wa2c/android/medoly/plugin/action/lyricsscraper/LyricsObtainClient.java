package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wa2c.android.medoly.library.MediaProperty;
import com.wa2c.android.medoly.library.PropertyData;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.EnumMap;
import java.util.EventListener;
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
    /** BASE URI取得用XPATH。 */
    private static final String BASE_PATH = "/html/head/base";

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
    /** 現在のBase URI */
    private URL currentBaseUrl;

    /** サイト情報。 */
    private EnumMap<SiteColumn, String> siteParam;

    /** 遅延時間 */
    private long delay = 0;


    /**
     * コンストラクタ。
     * @param context コンテキスト。
     * @param propertyData 要求元プロパティマップ。
     */
    public LyricsObtainClient(Context context, PropertyData propertyData) throws SiteNotFoundException, SiteNotSelectException {
        this.requestPropertyMap = propertyData;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String siteId = preferences.getString(context.getString(R.string.prefkey_selected_site_id), null);
        if (TextUtils.isEmpty(siteId))
            throw new SiteNotSelectException();

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
                throw new SiteNotFoundException();

            for (SiteColumn col : SiteColumn.values()) {
                siteParam.put(col, cursor.getString(cursor.getColumnIndexOrThrow(col.getColumnKey())));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        try {
            delay = Long.valueOf(siteParam.get(SiteColumn.DELAY));
        } catch (NumberFormatException | NullPointerException e) {
            Logger.e(e);
        }

        this.webView = new WebView(context);
        this.webView.getSettings().setAppCacheEnabled(true);
        this.webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.webView.getSettings().setLoadsImagesAutomatically(false);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.getSettings().setDatabaseEnabled(true);
        this.webView.getSettings().setUserAgentString(context.getString(R.string.app_user_agent));
        this.webView.setVisibility(View.INVISIBLE);
        this.webView.addJavascriptInterface(new JavaScriptInterface(siteParam), JAVASCRIPT_INTERFACE);
        this.webView.setWebViewClient(new WebViewClient() {

            // ページ読み込み完了
            @Override
            public void onPageFinished(WebView view, String url) {
                // Ajax処理待ちの遅延
                handler.postDelayed(executeScript, delay);
            }

            // エラー
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                returnLyrics();
            }

            // 同じビューで再読込
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
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
                        returnLyrics();
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
        final String searchUri = replaceProperty(siteParam.get(SiteColumn.SEARCH_URI), true, false);
        Logger.d("Search URL: " + searchUri);

        try {
            currentBaseUrl =  new URL(searchUri);
        } catch (MalformedURLException e) {
            Logger.e(e);
        }

        // 歌詞取得開始
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentState = STATE_SEARCH;
                webView.stopLoading();
                webView.loadUrl(searchUri);
            }
        }, delay);
    }




    /** ハンドラ。 */
    private final Handler handler = new Handler();

    /**
     * JavaScript側からコールバックされるオブジェクト。
     */
    final private class JavaScriptInterface {

        private String url;
        private EnumMap<SiteColumn, String> siteParam;

        JavaScriptInterface(EnumMap<SiteColumn, String> siteParam) {
            this.siteParam = siteParam;
        }



        /**
         * 歌詞の検索結果から歌詞ページを取得。
         * @param html HTMLソース。
         */
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void getSearchResult(String html) {
            Logger.d("Search HTML: " + html);

            try {
                url = null;

                Document doc = Jsoup.parse(html);
                if (siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_XPATH)) {
                    // XPath
                    Elements e = Xsoup.compile(siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TEXT)).evaluate(doc).getElements();
                    if (e == null || e.size() == 0) {
                        returnLyrics();
                        return;
                    }

                    // 歌詞ページのURLを取得
                    Element anchor = e.get(0);
                    url = anchor.attr("href");
                } else if (siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_REGEXP)) {
                    String parseText = replaceProperty(siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TEXT), false, true);
                    Logger.d("Parse Text: " + parseText);
                    Pattern p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(html);
                    if (m.find()) {
                        url = m.group(1);
                    }
                }

                if (TextUtils.isEmpty(url)) {
                    returnLyrics();
                    return;
                }
                Logger.d("Lyrics Path: " + url);

                // BASE URL取得
                Elements e = Xsoup.compile(BASE_PATH).evaluate(doc).getElements();
                if (e != null && e.size() > 0) {
                    try {
                        currentBaseUrl = new URL(e.get(0).attr("href"));
                    } catch (MalformedURLException mue) {
                        Logger.e(mue);
                    }
                }
                if (currentBaseUrl != null)
                    currentBaseUrl = new URL(currentBaseUrl, url);
                Logger.d("Lyrics URL: " + currentBaseUrl);

                // 歌詞取得
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentState = STATE_PAGE;
                        webView.stopLoading();
                        webView.loadUrl(currentBaseUrl.toString());
                    }
                }, delay);
            } catch (Exception e) {
                Logger.e(e);
                returnLyrics();
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
                Logger.d("Lyrics HTML: " + html);


                if (siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_XPATH)) {
                    // XPath
                    Document doc = Jsoup.parse(html);
                    Elements e = Xsoup.compile(siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TEXT)).evaluate(doc).getElements();
                    if (e == null || e.size() == 0) {
                        returnLyrics();
                        return;
                    }

                    Element elem = e.get(0);
                    lyrics = elem.html();
                } else if (siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_REGEXP)) {
                    // 正規表現
                    String parseText = replaceProperty(siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TEXT), false, true);
                    Logger.d("Parse Text: " + parseText);
                    Pattern p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE|Pattern.MULTILINE|Pattern.DOTALL);
                    Matcher m = p.matcher(html);
                    if (m.find()) {
                        lyrics = m.group(1);
                    }
                }

                // 調整
                lyrics = AppUtils.adjustLyrics(lyrics);
                Logger.d("Lyrics: " + lyrics);

                // 歌詞を返す
                returnLyrics(lyrics, siteParam.get(SiteColumn.SITE_NAME), currentBaseUrl.toString());
            } catch (Exception e) {
                currentState = STATE_COMPLETE;
                Logger.e(e);
                returnLyrics();
            }
        }
    }


    /**
     * 歌詞取得失敗。
     */
    private void returnLyrics() {
        returnLyrics(null, null, null);
    }

    /**
     * 取得した歌詞をイベントハンドラに渡して呼び出し元に返す。
     * @param lyrics 歌詞テキスト。
     */
    private void returnLyrics(String lyrics, String title, String uri) {
        if (lyricsObtainListener != null) {
            lyricsObtainListener.onLyricsObtain(lyrics, title, uri);
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


    private static final String REGEXP_ESCAPE = "[\\\\\\*\\+\\.\\?\\{\\}\\(\\)\\[\\]\\^\\$\\-\\|]";

    /**
     * 文字列のプロパティ情報を入れる。
     * @param inputText 入力テキスト。
     * @return 置換え後のテキスト。
     */
    private String replaceProperty(String inputText, boolean urlEncode, boolean escapeRegexp) {
        // 正規表現作成
        StringBuilder regexpBuilder = new StringBuilder();
        for (MediaProperty p : MediaProperty.values()) {
            regexpBuilder.append("|%").append(p.getKeyName()).append("%");
        }
        String regexp = "(" + regexpBuilder.substring(1) + ")";

        // URIを作成
        StringBuilder outputBuffer = new StringBuilder();
        Pattern pattern = Pattern.compile(regexp, Pattern.MULTILINE|Pattern.DOTALL);
        Matcher matcher = pattern.matcher(inputText);
        int lastIndex = 0;
        while(matcher.find()){
            outputBuffer.append(inputText.substring(lastIndex, matcher.start()));
            String tag = matcher.group(); // タグ (%KEY%)
            String key = tag.substring(1, tag.length() - 1); // プロパティキー (KEY)
            String val = requestPropertyMap.getFirst(key); // プロパティ値
            if (!TextUtils.isEmpty(val)) {
                try {
                    String text = AppUtils.normalizeText(val);
                    if (escapeRegexp)
                        text = text.replaceAll(REGEXP_ESCAPE, "\\\\$1");
                    if (urlEncode)
                        text = URLEncoder.encode(text, siteParam.get(SiteColumn.RESULT_PAGE_URI_ENCODING));
                    outputBuffer.append(text);
                } catch (UnsupportedEncodingException e) {
                    Logger.e(e);
                }
            }
            lastIndex = matcher.start() + tag.length();
        }
        outputBuffer.append(inputText.substring(lastIndex));

        return outputBuffer.toString();
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
        void onLyricsObtain(String lyrics, String title, String uri);
    }

}

