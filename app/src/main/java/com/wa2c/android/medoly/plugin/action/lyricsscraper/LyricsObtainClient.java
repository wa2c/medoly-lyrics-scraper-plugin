package com.wa2c.android.medoly.plugin.action.lyricsscraper;

/**
 * Created by wa2c on 2015/08/18.
 */
/** 検索URI。 */

import android.content.Context;
import android.content.Intent;
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

    /** 要求プロパティ。 */
    private HashMap<String, String> requestPropertyMap;

    /** Webページ表示用WebView。 */
    private WebView webView;


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
        this.webView.addJavascriptInterface(new JavaScriptInterface(), "android");
        this.webView.setWebViewClient(searchWebViewClient);
    }



    /** 検索URL */
    private static final String SEARCH_URL = "http://www.kasi-time.com/allsearch.php?q=%s";
    /** 検索結果アンカーのXPATH。 */
    private static final String SEARCH_ANCHOR_XPATH = "//a[@class='gs-title']";
    /** 歌詞のXPATH */
    private static final String LYRICS_XPATH = "//div[@id='lyrics']";

    /**
     * 歌詞を取得する。
     * @param listener 結果取得リスナ。
     */
    public void obtainLyrics(LyricsObtainListener listener) {
        if (listener == null) {
            return;
        }

        // 検索ワード
        String title = requestPropertyMap.get(ActionPluginParam.MediaProperty.TITLE.getKeyName());
        String artist = requestPropertyMap.get(ActionPluginParam.MediaProperty.ARTIST.getKeyName());
        String searchWord = ( (title == null ? "" : title) + " " + (artist == null ? "" : artist) ).trim();

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

        try
        {
            Thread.sleep(2000);  // again,update takes less then 1 secs but still i give it 1.5 secs wait time to be sure
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }

    }


    /**
     * 歌詞検索。
     */
    private WebViewClient searchWebViewClient = new WebViewClient() {
        private static final int STATE_SEARCH = 0;   // 検索結果取得
        private static final int STATE_PAGE = 1;     // 歌詞ページ取得
        private static final int STATE_COMPLETE = 2; // 完了

        // 現在の状態
        private int currentState = STATE_SEARCH;

        @Override
        public void onPageFinished(WebView view, String url) {
            if (currentState == STATE_SEARCH) {
                // 歌詞検索結果の取得
                currentState = STATE_PAGE;
                webView.loadUrl("javascript:window.android.getSearchResult(document.getElementsByTagName('html')[0].outerHTML);");
            } else if (currentState == STATE_PAGE) {
                // 歌詞ページの取得
                currentState = STATE_COMPLETE;
                webView.loadUrl("javascript:window.android.getLyrics(document.getElementsByTagName('html')[0].outerHTML);");
            } else {
                returnLyrics(null);
            }
        }
    };

    /**
     * JavaScript側からコールバックされるオブジェクト。
     */
    final public class JavaScriptInterface {

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
                String url = anchor.attr("href");

                // 歌詞取得
                webView.loadUrl(url);
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
                String lyrics = elem.ownText();
                //lyrics = elem.html();
                lyrics = elem.outerHtml();

                // 歌詞を返す
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
         * @param lyrics 歌詞。
         */
        void onLyricsObtain(String lyrics);
    }

}

