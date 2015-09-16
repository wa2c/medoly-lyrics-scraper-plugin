package com.wa2c.android.medoly.plugin.action.lyricsscraper;

/**
 * Created by wa2c on 2015/08/18.
 */
/** 検索URI。 */

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wa2c.android.medoly.plugin.action.ActionPluginParam;
import com.wa2c.android.medoly.plugin.action.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.EventListener;
import java.util.HashMap;

import us.codecraft.xsoup.Xsoup;



public class LyricsObtainClient {

    /** Javascriptンターフェースオブジェクト名。 */
    private static final String JAVASCRIPT_INTERFACE = "android";
    /** 検索ページ取得スクリプト。 */
    private static final String SEARCH_PAGE_GET_SCRIPT = "javascript:window." + JAVASCRIPT_INTERFACE + ".getSearchResult(document.getElementsByTagName('html')[0].outerHTML);";
    /** 歌詞ページ取得スクリプト。 */
    private static final String LYRICS_PAGE_GET_SCRIPT = "javascript:window." + JAVASCRIPT_INTERFACE + ".getLyrics(document.getElementsByTagName('html')[0].outerHTML);";

    /** 検索URL */
    private static final String SEARCH_URL = "http://www.kasi-time.com/allsearch.php?q=%s";
    /** 検索結果アンカーのXPATH。 */
    private static final String SEARCH_ANCHOR_XPATH = "//a[@class='gs-title']";
    /** 歌詞のXPATH */
    private static final String LYRICS_XPATH = "//div[@id='lyrics']";

    /** 検索ページ取得中。 */
    private static final int STATE_SEARCH = 0;
    /** 歌詞ページ取得中。  */
    private static final int STATE_PAGE = 1;
    /** 完了済み。 */
    private static final int STATE_COMPLETE = 2;



    /** 要求プロパティ。 */
    private HashMap<String, String> requestPropertyMap;

    /** Webページ表示用WebView。 */
    private WebView webView;

    /** 現在の状態 */
    private int currentState = STATE_SEARCH;


    /**
     * コンストラクタ。
     * @param context コンテキスト。
     * @param propertyMap 要求元プロパティマップ。
     */
    public LyricsObtainClient(Context context, HashMap<String, String> propertyMap) {
        this.requestPropertyMap = propertyMap;

        this.webView = new WebView(context);
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
                new Handler().postDelayed(loadPage, 3000);
            }

            // ページ取得
            private final Runnable loadPage = new Runnable() {
                @Override
                public void run() {
                    if (currentState == STATE_SEARCH) {
                        // 歌詞検索結果の取得
                        webView.loadUrl(SEARCH_PAGE_GET_SCRIPT);
                    } else if (currentState == STATE_PAGE) {
                        // 歌詞ページの取得
                        webView.loadUrl(LYRICS_PAGE_GET_SCRIPT);
                    } else {
                        returnLyrics(null);
                    }
                }
            };

            // エラー
            @Override
            public void onReceivedError (WebView view, int errorCode, String description, String failingUrl) {
                returnLyrics(null);
            }
        });



    }


    /** ハンドラ。 */
    final Handler handler = new Handler();

    /**
     * JavaScript側からコールバックされるオブジェクト。
     */
    final private class JavaScriptInterface {

        /**
         * 歌詞の検索結果から歌詞ページを取得。
         * @param html HTMLソース。
         */
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void getSearchResult(String html) {
            try {
                Document doc = Jsoup.parse(html);
                Elements e = Xsoup.compile(SEARCH_ANCHOR_XPATH).evaluate(doc).getElements();
                if (e == null || e.size() == 0) {
                    returnLyrics(null);
                    return;
                }

                // 歌詞ページのURLを取得
                Element anchor = e.get(0);
                final String url = anchor.attr("href");
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

        /**
         * 歌詞ページから歌詞を取得。
         * @param html HTMLソース。
         */
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void getLyrics(String html) {
            try {
                Document doc = Jsoup.parse(html);
                Elements e = Xsoup.compile(LYRICS_XPATH).evaluate(doc).getElements();
                if (e == null || e.size() == 0) {
                    returnLyrics(null);
                    return;
                }

                Element elem = e.get(0);

                String lyrics = elem.html();
                lyrics = AppUtils.adjustLyrics(lyrics);
                //lyrics = elem.html();
                //lyrics = elem.outerHtml();

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



    /**
     * 歌詞を取得する。
     * @param listener 結果取得リスナ。
     */
    public void obtainLyrics(LyricsObtainListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }

        // 検索ワード
        String title = AppUtils.normalizeText(requestPropertyMap.get(ActionPluginParam.MediaProperty.TITLE.getKeyName()));
        String artist = AppUtils.normalizeText(requestPropertyMap.get(ActionPluginParam.MediaProperty.ARTIST.getKeyName()));
        String searchWord = ( title + " " + artist );

        if (TextUtils.isEmpty(searchWord)) {
            return;
        }

        String searchUrl;
        try {
            searchUrl = String.format(SEARCH_URL, URLEncoder.encode(searchWord + " item", "utf-8")); // アーティスト結果がヒットしないよう、itemを追加
        } catch (UnsupportedEncodingException e) {
            Logger.e(e);
            return;
        }

        // 歌詞検索
        this.lyricsObtainListener = listener;
        this.webView.loadUrl(searchUrl);
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

