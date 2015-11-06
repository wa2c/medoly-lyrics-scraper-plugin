package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wa2c.android.medoly.library.MediaProperty;
import com.wa2c.android.medoly.utils.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    private HashMap<String, String> requestPropertyMap;
    /** 取得パラメータ。 */
    private LyricsObtainParam lyricsObtainParam;
    /** Webページ表示用WebView。 */
    private WebView webView;
    /** 現在の状態 */
    private int currentState = STATE_INIT;



    /**
     * コンストラクタ。
     * @param context コンテキスト。
     * @param propertyMap 要求元プロパティマップ。
     */
    public LyricsObtainClient(Context context, HashMap<String, String> propertyMap, LyricsObtainParam param) {
        this.requestPropertyMap = propertyMap;
        this.lyricsObtainParam = param;

        this.webView = new WebView(context);
        this.webView.getSettings().setAppCacheEnabled(false);
        this.webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.webView.getSettings().setLoadsImagesAutomatically(false);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setUserAgentString(context.getString(R.string.app_user_agent));
        this.webView.setVisibility(View.INVISIBLE);
        this.webView.addJavascriptInterface(new JavaScriptInterface(), JAVASCRIPT_INTERFACE);
        this.webView.setWebViewClient(new WebViewClient() {

            // ページ読み込み完了
            @Override
            public void onPageFinished(WebView view, String url) {
                // Ajax処理待ちの遅延
                handler.postDelayed(executeScript, lyricsObtainParam.DelayMilliseconds);
            }

            // エラー
            @Override
            public void onReceivedError (WebView view, int errorCode, String description, String failingUrl) {
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
        final String searchUri = replaceUriTag(lyricsObtainParam.SearchURI);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                currentState = STATE_SEARCH;
//                webView.loadUrl(searchUri);
//            }
//        }, 1000);

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        currentState = STATE_SEARCH;
        webView.loadUrl(searchUri);

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
            String val = requestPropertyMap.get(key); // プロパティ値
            if (!TextUtils.isEmpty(val)) {
                try {
                    outputBuffer.append(URLEncoder.encode(AppUtils.normalizeText(val), lyricsObtainParam.URIEncoding));
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

                if (lyricsObtainParam.SearchAnchorParseType == LyricsObtainParam.ParseTypeXPath) {
                    // XPath
                    Document doc = Jsoup.parse(html);
                    Elements e = Xsoup.compile(lyricsObtainParam.SearchAnchorParseText).evaluate(doc).getElements();
                    if (e == null || e.size() == 0) {
                        returnLyrics(null);
                        return;
                    }

                    // 歌詞ページのURLを取得
                    Element anchor = e.get(0);
                    url = anchor.attr("href");
                } else if (lyricsObtainParam.SearchAnchorParseType == LyricsObtainParam.ParseTypeRegexp) {
                    Pattern p = Pattern.compile(lyricsObtainParam.SearchAnchorParseText, Pattern.CASE_INSENSITIVE);
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        currentState = STATE_PAGE;
                        webView.loadUrl(url);
                    }
                });
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
                if (lyricsObtainParam.SearchLyricsParseType == LyricsObtainParam.ParseTypeXPath) {
                    // XPath
                    Document doc = Jsoup.parse(html);
                    Elements e = Xsoup.compile(lyricsObtainParam.SearchLyricsParseText).evaluate(doc).getElements();
                    if (e == null || e.size() == 0) {
                        returnLyrics(null);
                        return;
                    }

                    Element elem = e.get(0);
                    lyrics = elem.html();
                } else if (lyricsObtainParam.SearchLyricsParseType == LyricsObtainParam.ParseTypeRegexp) {
                    // 正規表現
                    Pattern p = Pattern.compile(lyricsObtainParam.SearchLyricsParseText, Pattern.CASE_INSENSITIVE|Pattern.MULTILINE|Pattern.DOTALL);
                    Matcher m = p.matcher(html);
                    if (m.find()) {
                        lyrics = m.group(1);
                    }
                }

                // 調整
                lyrics = AppUtils.adjustLyrics(lyrics);

                // 歌詞を返す
                currentState = STATE_COMPLETE;
                returnLyrics(lyrics);
            } catch (Exception e) {
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

