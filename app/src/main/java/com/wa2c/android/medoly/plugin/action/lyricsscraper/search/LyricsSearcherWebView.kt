package com.wa2c.android.medoly.plugin.action.lyricsscraper.search

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView

import com.wa2c.android.medoly.library.MediaProperty
import com.wa2c.android.medoly.library.PropertyData
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.Site
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SiteColumn
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SiteProvider
import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotFoundException
import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotSelectException
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger

import org.jsoup.Jsoup

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.ArrayList
import java.util.EnumMap
import java.util.EventListener
import java.util.regex.Pattern

import us.codecraft.xsoup.Xsoup


@SuppressLint("SetJavaScriptEnabled")
class LyricsSearcherWebView constructor(context: Context) : WebView(context) {

    //    /** 初期状態。 */
    //    private static final int STATE_IDLE = 0;
    //    /** 検索ページ取得中。 */
    //    private static final int STATE_SEARCH = 1;
    //    /** 歌詞ページ取得中。  */
    //    private static final int STATE_LYRICS = 2;
    //    /** 完了済み。 */
    //    private static final int STATE_COMPLETE = 3;
    //
    //    /** 現在の状態 */
    //    private int currentState = STATE_IDLE;
    //    /** 現在のBase URI */
    //    private URL currentBaseUrl;

    /** サイト情報。  */
    private val siteParam = EnumMap<SiteColumn, String>(SiteColumn::class.java)

    private val lyricsWebClient: LyricsWebClient = LyricsWebClient(this)
    //private val searchResultItemList = ArrayList<ResultItem>()

    //var handler: Handler? = null

    private var propertyData: PropertyData? = null
    private var site: Site? = null


    /** Event handle listener。  */
    var handleListener: HandleListener? = null

    val webHandler: Handler = Handler()

    init {

        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.loadsImagesAutomatically = false
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.userAgentString = context.getString(R.string.app_user_agent)
        visibility = View.INVISIBLE
        webViewClient = lyricsWebClient
        addJavascriptInterface(this, JAVASCRIPT_INTERFACE)
    }

    @Throws(SiteNotFoundException::class, SiteNotSelectException::class)
    private fun setParam() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val siteId = preferences.getString(context.getString(R.string.prefkey_selected_site_id), null)
        if (siteId.isNullOrEmpty())
            throw SiteNotSelectException()
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                    SiteProvider.SITE_URI, null,
                    SiteColumn.SITE_ID.columnKey + "=?",
                    arrayOf(siteId), null
            )
            if (cursor == null || cursor.count == 0 || !cursor.moveToFirst())
                throw SiteNotFoundException()

            for (col in SiteColumn.values()) {
                siteParam[col] = cursor.getString(cursor.getColumnIndexOrThrow(col.columnKey))
            }
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }

        try {
            lyricsWebClient.delay = siteParam[SiteColumn.DELAY]?.toLong() ?: 0
        } catch (e: NumberFormatException) {
            Logger.e(e)
        }

    }

    fun search2(propertyData: PropertyData, site: Site) {
        this.propertyData = propertyData
        this.site = site

        val searchUri = replaceProperty(siteParam[SiteColumn.SEARCH_URI]!!, true, false)
        Logger.d("Search URL: $searchUri")

        try {
            lyricsWebClient.setState(LyricsWebClient.STATE_SEARCH)
            stopLoading()
            loadUrl(searchUri)
        } catch (e: Exception) {
            handleListener?.onError("")
        }
    }

    @Throws(SiteNotSelectException::class, SiteNotFoundException::class)
    fun search(propertyData: PropertyData) {
        setParam()

        this.propertyData = propertyData
        lyricsWebClient.setState(LyricsWebClient.STATE_SEARCH)

        val searchUri = replaceProperty(siteParam[SiteColumn.SEARCH_URI]!!, true, false)

        Logger.d("Search URL: $searchUri")

        try {
            lyricsWebClient.setState(LyricsWebClient.STATE_SEARCH)
            stopLoading()
            loadUrl(searchUri)
        } catch (e: Exception) {
            handleListener?.onError("")
        }

    }

    fun download2(url: String) {
        try {
            lyricsWebClient.setState(LyricsWebClient.STATE_LYRICS)
            stopLoading()
            loadUrl(url)
        } catch (e: Exception) {
            if (handleListener != null) {
                handleListener!!.onError("■えらー")
            }
        }
    }

    @Throws(SiteNotSelectException::class, SiteNotFoundException::class)
    fun download(url: String) {
        setParam()

        try {
            lyricsWebClient.setState(LyricsWebClient.STATE_LYRICS)
            stopLoading()
            loadUrl(url)
        } catch (e: Exception) {
            if (handleListener != null) {
                handleListener!!.onError("■えらー")
            }
        }

    }


    @JavascriptInterface
    fun getSearchResult(html: String) {
        val searchResultItemList = LinkedHashMap<String, ResultItem>()

        try {
            val doc = Jsoup.parse(html)
            if (siteParam[SiteColumn.RESULT_PAGE_PARSE_TYPE] == SiteColumn.PARSE_TYPE_XPATH) {
                // XPath
                val e = Xsoup.compile(siteParam[SiteColumn.RESULT_PAGE_PARSE_TEXT]).evaluate(doc).elements
                if (e == null || e.size == 0) {
                    return
                }

                for (element in e) {
                    try {
                        val item = ResultItem()
                        item.pageUrl = element.attr("href")
                        item.musicTitle = element.text()
                        if (item.pageUrl.isNullOrEmpty())
                            continue;
                        searchResultItemList[item.pageUrl!!] = item
                    } catch (ignore: Exception) {
                    }
                }
                Logger.d(searchResultItemList)
            } else if (siteParam[SiteColumn.RESULT_PAGE_PARSE_TYPE] == SiteColumn.PARSE_TYPE_REGEXP) {
                val parseText = replaceProperty(siteParam[SiteColumn.RESULT_PAGE_PARSE_TEXT]!!, false, true)
                Logger.d("Parse Text: $parseText")
                val p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE)
                val m = p.matcher(html)
                while (m.find()) {
                    try {
                        val item = ResultItem()
                        item.pageUrl = m.group(1)
                        item.musicTitle = m.group(1)
                        if (item.pageUrl.isNullOrEmpty())
                            continue;
                        searchResultItemList[item.pageUrl!!] = item
                    } catch (ignore: Exception) {
                    }

                }
            }
        } catch (e: Exception) {
            Logger.e(e)
        } finally {
            webHandler.post { handleListener?.onSearchResult(searchResultItemList.values.toList()) }
        }
    }

    @JavascriptInterface
    fun getLyrics(html: String) {
        var lyrics: String? = null
        try {
            Logger.d("Lyrics HTML: $html")

            if (siteParam[SiteColumn.LYRICS_PAGE_PARSE_TYPE] == SiteColumn.PARSE_TYPE_XPATH) {
                // XPath
                val doc = Jsoup.parse(html)
                val e = Xsoup.compile(siteParam[SiteColumn.LYRICS_PAGE_PARSE_TEXT]).evaluate(doc).elements
                if (e == null || e.size == 0) {
                    return
                }

                val elem = e[0]
                lyrics = elem.html()
            } else if (siteParam[SiteColumn.LYRICS_PAGE_PARSE_TYPE] == SiteColumn.PARSE_TYPE_REGEXP) {
                // 正規表現
                val parseText = replaceProperty(siteParam[SiteColumn.LYRICS_PAGE_PARSE_TEXT]!!, false, true)
                Logger.d("Parse Text: $parseText")
                val p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)
                val m = p.matcher(html)
                if (m.find()) {
                    lyrics = m.group(1)
                }
            }

            if (lyrics != null) {
                lyrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(lyrics, Html.FROM_HTML_MODE_LEGACY).toString()
                } else {
                    Html.fromHtml(lyrics).toString()
                }
            }
        } catch (e: Exception) {
            Logger.e(e)
        } finally {
            val l = lyrics
            webHandler.post {
                if (handleListener != null) {
                    handleListener!!.onGetLyrics(l)
                }
            }
            lyricsWebClient.setState(LyricsWebClient.STATE_IDLE)
        }
    }

    /**
     * 文字列のプロパティ情報を入れる。
     * @param inputText 入力テキスト。
     * @return 置換え後のテキスト。
     */
    private fun replaceProperty(inputText: String, urlEncode: Boolean, escapeRegexp: Boolean): String {
        // 正規表現作成
        val regexpBuilder = StringBuilder()
        for (p in MediaProperty.values()) {
            regexpBuilder.append("|%").append(p.keyName).append("%")
        }
        val regexp = "(" + regexpBuilder.substring(1) + ")"

        // URIを作成
        val outputBuffer = StringBuilder()
        val pattern = Pattern.compile(regexp, Pattern.MULTILINE or Pattern.DOTALL)
        val matcher = pattern.matcher(inputText)
        var lastIndex = 0
        while (matcher.find()) {
            outputBuffer.append(inputText.substring(lastIndex, matcher.start()))
            val tag = matcher.group() // タグ (%KEY%)
            val key = tag.substring(1, tag.length - 1) // プロパティキー (KEY)
            val value = propertyData!!.getFirst(key) // プロパティ値
            if (!value.isNullOrEmpty()) {
                try {
                    var text = AppUtils.normalizeText(value)
                    if (escapeRegexp)
                        text = text.replace(REGEXP_ESCAPE.toRegex(), "\\\\$1")
                    if (urlEncode)
                        text = URLEncoder.encode(text, siteParam[SiteColumn.RESULT_PAGE_URI_ENCODING])
                    outputBuffer.append(text)
                } catch (e: UnsupportedEncodingException) {
                    Logger.e(e)
                }

            }
            lastIndex = matcher.start() + tag.length
        }
        outputBuffer.append(inputText.substring(lastIndex))

        return outputBuffer.toString()
    }


    /**
     * Set a event handle listener
     * @param listener A listener.
     */
    fun setOnHandleListener(listener: HandleListener) {
        this.handleListener = listener
    }

    /**
     * Event handle listener interface.
     */
    interface HandleListener : EventListener {
        fun onSearchResult(list: List<ResultItem>)
        fun onGetLyrics(lyrics: String?)
        fun onError(message: String?)
    }

    companion object {
        /** Javascriptンターフェースオブジェクト名。  */
        const val JAVASCRIPT_INTERFACE = "android"
        /** 検索ページ取得スクリプト。  */
        const val SEARCH_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getSearchResult(document.getElementsByTagName('html')[0].outerHTML);"
        /** 歌詞ページ取得スクリプト。  */
        const val LYRICS_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getLyrics(document.getElementsByTagName('html')[0].outerHTML);"
        /** BASE URI取得用XPATH。  */
        const val BASE_PATH = "/html/head/base"

        const val REGEXP_ESCAPE = "[\\\\\\*\\+\\.\\?\\{\\}\\(\\)\\[\\]\\^\\$\\-\\|]"
    }


    //    private final WebViewClient webViewClient = new WebViewClient() {
    //        // ページ読み込み完了
    //        @Override
    //        public void onPageFinished(WebView view, String url) {
    //            // Ajax処理待ちの遅延
    //            handler.postDelayed(scriptRunnable, delay);
    //        }
    //
    //        // エラー
    //        @Override
    //        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
    //            returnLyrics();
    //        }
    //
    //        // 同じビューで再読込
    //        @Override
    //        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
    //            return false;
    //        }
    //
    //        // スクリプトを実行してページを取得
    //        private final Runnable scriptRunnable = new Runnable() {
    //            @Override
    //            public void run() {
    //                if (currentState == STATE_SEARCH) {
    //                    // 歌詞検索結果の取得スクリプト実行
    //                    webView.loadUrl(SEARCH_PAGE_GET_SCRIPT);
    //                } else if (currentState == STATE_LYRICS) {
    //                    // 歌詞ページの取得スクリプト実行
    //                    webView.loadUrl(LYRICS_PAGE_GET_SCRIPT);
    //                } else {
    //                    returnLyrics();
    //                }
    //            }
    //        };
    //    };


    //    /**
    //     * 歌詞を取得する。
    //     * @param listener 結果取得リスナ。
    //     */
    //    public void obtainLyrics(LyricsObtainListener listener) {
    //        if (listener == null) {
    //            throw new IllegalArgumentException();
    //        }
    //
    //        lyricsObtainListener = listener;
    //        final String searchUri = replaceProperty(siteParam.get(SiteColumn.SEARCH_URI), true, false);
    //        Logger.d("Search URL: " + searchUri);
    //
    //        try {
    //            currentBaseUrl =  new URL(searchUri);
    //        } catch (MalformedURLException e) {
    //            Logger.e(e);
    //        }
    //
    //        // 歌詞取得開始
    //        handler.postDelayed(new Runnable() {
    //            @Override
    //            public void run() {
    //                currentState = STATE_SEARCH;
    //                webView.stopLoading();
    //                webView.loadUrl(searchUri);
    //            }
    //        }, delay);
    //    }


    //
    //
    //    /** ハンドラ。 */
    //    private final Handler handler = new Handler();
    //
    //    /**
    //     * JavaScript側からコールバックされるオブジェクト。
    //     */
    //    final private class JavaScriptInterface {
    //
    //        private String url;
    //        private EnumMap<SiteColumn, String> siteParam;
    //
    //        JavaScriptInterface(EnumMap<SiteColumn, String> siteParam) {
    //            this.siteParam = siteParam;
    //        }
    //
    //
    //
    //        /**
    //         * 歌詞の検索結果から歌詞ページを取得。
    //         * @param html HTMLソース。
    //         */
    //        @JavascriptInterface
    //        @SuppressWarnings("unused")
    //        public void getSearchResult(String html) {
    //            Logger.d("Search HTML: " + html);
    //
    //            try {
    //                url = null;
    //
    //                Document doc = Jsoup.parse(html);
    //                if (siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_XPATH)) {
    //                    // XPath
    //                    Elements e = Xsoup.compile(siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TEXT)).evaluate(doc).getElements();
    //                    if (e == null || e.size() == 0) {
    //                        returnLyrics();
    //                        return;
    //                    }
    //
    //                    // 歌詞ページのURLを取得
    //                    Element anchor = e.get(0);
    //                    url = anchor.attr("href");
    //                } else if (siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_REGEXP)) {
    //                    String parseText = replaceProperty(siteParam.get(SiteColumn.RESULT_PAGE_PARSE_TEXT), false, true);
    //                    Logger.d("Parse Text: " + parseText);
    //                    Pattern p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE);
    //                    Matcher m = p.matcher(html);
    //                    if (m.find()) {
    //                        url = m.group(1);
    //                    }
    //                }
    //
    //                if (TextUtils.isEmpty(url)) {
    //                    returnLyrics();
    //                    return;
    //                }
    //                Logger.d("Lyrics Path: " + url);
    //
    //                // BASE URL取得
    //                Elements e = Xsoup.compile(BASE_PATH).evaluate(doc).getElements();
    //                if (e != null && e.size() > 0) {
    //                    try {
    //                        currentBaseUrl = new URL(e.get(0).attr("href"));
    //                    } catch (MalformedURLException mue) {
    //                        Logger.e(mue);
    //                    }
    //                }
    //                if (currentBaseUrl != null)
    //                    currentBaseUrl = new URL(currentBaseUrl, url);
    //                Logger.d("Lyrics URL: " + currentBaseUrl);
    //
    //                // 歌詞取得
    //                handler.postDelayed(new Runnable() {
    //                    @Override
    //                    public void run() {
    //                        currentState = STATE_LYRICS;
    //                        webView.stopLoading();
    //                        webView.loadUrl(currentBaseUrl.toString());
    //                    }
    //                }, delay);
    //            } catch (Exception e) {
    //                Logger.e(e);
    //                returnLyrics();
    //            }
    //        }
    //
    //        String lyrics;
    //
    //        /**
    //         * 歌詞ページから歌詞を取得。
    //         * @param html HTMLソース。
    //         */
    //        @JavascriptInterface
    //        @SuppressWarnings("unused")
    //        public void getLyrics(String html) {
    //            try {
    //                Logger.d("Lyrics HTML: " + html);
    //
    //
    //                if (siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_XPATH)) {
    //                    // XPath
    //                    Document doc = Jsoup.parse(html);
    //                    Elements e = Xsoup.compile(siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TEXT)).evaluate(doc).getElements();
    //                    if (e == null || e.size() == 0) {
    //                        returnLyrics();
    //                        return;
    //                    }
    //
    //                    Element elem = e.get(0);
    //                    lyrics = elem.html();
    //                } else if (siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TYPE).equals(SiteColumn.PARSE_TYPE_REGEXP)) {
    //                    // 正規表現
    //                    String parseText = replaceProperty(siteParam.get(SiteColumn.LYRICS_PAGE_PARSE_TEXT), false, true);
    //                    Logger.d("Parse Text: " + parseText);
    //                    Pattern p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE|Pattern.MULTILINE|Pattern.DOTALL);
    //                    Matcher m = p.matcher(html);
    //                    if (m.find()) {
    //                        lyrics = m.group(1);
    //                    }
    //                }
    //
    //                // 調整
    //                lyrics = AppUtils.adjustLyrics(lyrics);
    //                Logger.d("Lyrics: " + lyrics);
    //
    //                // 歌詞を返す
    //                returnLyrics(lyrics, siteParam.get(SiteColumn.SITE_NAME), currentBaseUrl.toString());
    //            } catch (Exception e) {
    //                currentState = STATE_COMPLETE;
    //                Logger.e(e);
    //                returnLyrics();
    //            }
    //        }
    //    }
    //
    //
    //    /**
    //     * 歌詞取得失敗。
    //     */
    //    private void returnLyrics() {
    //        returnLyrics(null, null, null);
    //    }
    //
    //    /**
    //     * 取得した歌詞をイベントハンドラに渡して呼び出し元に返す。
    //     * @param lyrics 歌詞テキスト。
    //     */
    //    private void returnLyrics(String lyrics, String title, String uri) {
    //        if (lyricsObtainListener != null) {
    //            lyricsObtainListener.onLyricsObtain(lyrics, title, uri);
    //        }
    //
    //        handler.post(new Runnable() {
    //            @Override
    //            public void run() {
    //                if (webView != null) {
    //                    webView.stopLoading();
    //                    webView.setWebChromeClient(null);
    //                    webView.setWebViewClient(null);
    //                    webView.destroy();
    //                    webView = null;
    //                }
    //            }
    //        });
    //    }
    //
    //
    //    private static final String REGEXP_ESCAPE = "[\\\\\\*\\+\\.\\?\\{\\}\\(\\)\\[\\]\\^\\$\\-\\|]";
    //
    //    /**
    //     * 文字列のプロパティ情報を入れる。
    //     * @param inputText 入力テキスト。
    //     * @return 置換え後のテキスト。
    //     */
    //    private String replaceProperty(String inputText, boolean urlEncode, boolean escapeRegexp) {
    //        // 正規表現作成
    //        StringBuilder regexpBuilder = new StringBuilder();
    //        for (MediaProperty p : MediaProperty.values()) {
    //            regexpBuilder.append("|%").append(p.getKeyName()).append("%");
    //        }
    //        String regexp = "(" + regexpBuilder.substring(1) + ")";
    //
    //        // URIを作成
    //        StringBuilder outputBuffer = new StringBuilder();
    //        Pattern pattern = Pattern.compile(regexp, Pattern.MULTILINE|Pattern.DOTALL);
    //        Matcher matcher = pattern.matcher(inputText);
    //        int lastIndex = 0;
    //        while(matcher.find()){
    //            outputBuffer.append(inputText.substring(lastIndex, matcher.start()));
    //            String tag = matcher.group(); // タグ (%KEY%)
    //            String key = tag.substring(1, tag.length() - 1); // プロパティキー (KEY)
    //            String val = requestPropertyMap.getFirst(key); // プロパティ値
    //            if (!TextUtils.isEmpty(val)) {
    //                try {
    //                    String text = AppUtils.normalizeText(val);
    //                    if (escapeRegexp)
    //                        text = text.replaceAll(REGEXP_ESCAPE, "\\\\$1");
    //                    if (urlEncode)
    //                        text = URLEncoder.encode(text, siteParam.get(SiteColumn.RESULT_PAGE_URI_ENCODING));
    //                    outputBuffer.append(text);
    //                } catch (UnsupportedEncodingException e) {
    //                    Logger.e(e);
    //                }
    //            }
    //            lastIndex = matcher.start() + tag.length();
    //        }
    //        outputBuffer.append(inputText.substring(lastIndex));
    //
    //        return outputBuffer.toString();
    //    }
    //
    //
    //
    //    /** リスナ。 */
    //    private LyricsObtainListener lyricsObtainListener;
    //
    //    /**
    //     * リスナを設定する。
    //     * @param listener リスナ。
    //     */
    //    public void setOnLyricsObtainListener(LyricsObtainListener listener) {
    //        this.lyricsObtainListener = listener;
    //    }
    //
    //    /**
    //     * 結果を取得するインターフェース。。
    //     */
    //    public interface LyricsObtainListener extends EventListener {
    //        /**
    //         * 取得イベント。
    //         * @param lyrics 歌詞。取得に失敗した場合はnull。
    //         */
    //        void onLyricsObtain(String lyrics, String title, String uri);
    //    }

}

