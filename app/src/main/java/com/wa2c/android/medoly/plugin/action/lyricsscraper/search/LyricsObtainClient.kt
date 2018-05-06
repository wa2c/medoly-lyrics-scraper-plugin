package com.wa2c.android.medoly.plugin.action.lyricsscraper.search

//package com.wa2c.android.medoly.plugin.action.lyricsscraper.service
//
//import android.content.Context
//import android.content.SharedPreferences
//import android.database.Cursor
//import android.os.Handler
//import android.preference.PreferenceManager
//import android.text.TextUtils
//import android.view.View
//import android.webkit.JavascriptInterface
//import android.webkit.WebResourceError
//import android.webkit.WebResourceRequest
//import android.webkit.WebSettings
//import android.webkit.WebView
//import android.webkit.WebViewClient
//
//import com.wa2c.android.medoly.library.MediaProperty
//import com.wa2c.android.medoly.library.PropertyData
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SiteColumn
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SiteProvider
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotFoundException
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotSelectException
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger
//
//import org.jsoup.Jsoup
//import org.jsoup.nodes.Document
//import org.jsoup.nodes.Element
//import org.jsoup.selectCache.Elements
//
//import java.io.UnsupportedEncodingException
//import java.net.MalformedURLException
//import java.net.URL
//import java.net.URLEncoder
//import java.util.EnumMap
//import java.util.EventListener
//import java.util.regex.Matcher
//import java.util.regex.Pattern
//
//import us.codecraft.xsoup.Xsoup
//
///**
// * コンストラクタ。
// * @param context コンテキスト。
// * @param propertyData 要求元プロパティマップ。
// */
//class LyricsObtainClient
//@Throws(SiteNotFoundException::class, SiteNotSelectException::class)
//constructor(context: Context, private val requestPropertyMap: PropertyData) {
//    /** Webページ表示用WebView。  */
//    private lateinit var webView: WebView
//    /** 現在の状態  */
//    private var currentState = STATE_INIT
//    /** 現在のBase URI  */
//    private var currentBaseUrl: URL? = null
//
//    /** サイト情報。  */
//    private val siteParam: EnumMap<SiteColumn, String>
//
//    /** 遅延時間  */
//    private var delay: Long = 0
//
//
//    /** ハンドラ。  */
//    private val handler = Handler()
//
//
//    /** リスナ。  */
//    private var lyricsObtainListener: LyricsObtainListener? = null
//
//
//    init {
//
//        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
//        val siteId = preferences.getString(context.getString(R.string.prefkey_selected_site_id), null)
//        if (siteId.isNullOrEmpty())
//            throw SiteNotSelectException()
//
//        siteParam = EnumMap<SiteColumn, String>(SiteColumn::class.java)
//        var cursor: Cursor? = null
//        try {
//            cursor = context.contentResolver.query(
//                    SiteProvider.SITE_URI, null,
//                    SiteColumn.SITE_ID.columnKey + "=?",
//                    arrayOf(siteId), null
//            )
//            if (cursor == null || cursor.count == 0 || !cursor.moveToFirst())
//                throw SiteNotFoundException()
//
//            for (col in SiteColumn.values()) {
//                siteParam[col] = cursor.getString(cursor.getColumnIndexOrThrow(col.columnKey))
//            }
//        } finally {
//            if (cursor != null && !cursor.isClosed) {
//                cursor.close()
//            }
//        }
//
//        try {
//            delay = java.lang.Long.valueOf(siteParam[SiteColumn.DELAY])!!
//        } catch (e: NumberFormatException) {
//            Logger.e(e)
//        } catch (e: NullPointerException) {
//            Logger.e(e)
//        }
//
//        this.webView = WebView(context)
//        this.webView.settings.setAppCacheEnabled(true)
//        this.webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
//        this.webView.settings.loadsImagesAutomatically = false
//        this.webView.settings.javaScriptEnabled = true
//        this.webView.settings.domStorageEnabled = true
//        this.webView.settings.databaseEnabled = true
//        this.webView.settings.userAgentString = context.getString(R.string.app_user_agent)
//        this.webView.visibility = View.INVISIBLE
//        this.webView.addJavascriptInterface(JavaScriptInterface(siteParam), JAVASCRIPT_INTERFACE)
//        this.webView.webViewClient = object : WebViewClient() {
//
//            // スクリプトを実行してページを取得
//            private val executeScript = Runnable {
//                if (currentState == STATE_SEARCH) {
//                    // 歌詞検索結果の取得スクリプト実行
//                    webView.loadUrl(SEARCH_PAGE_GET_SCRIPT)
//                } else if (currentState == STATE_PAGE) {
//                    // 歌詞ページの取得スクリプト実行
//                    webView.loadUrl(LYRICS_PAGE_GET_SCRIPT)
//                } else {
//                    returnLyrics()
//                }
//            }
//
//            // ページ読み込み完了
//            override fun onPageFinished(view: WebView, url: String) {
//                // Ajax処理待ちの遅延
//                handler.postDelayed(executeScript, delay)
//            }
//
//            // エラー
//            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
//                returnLyrics()
//            }
//
//            // 同じビューで再読込
//            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
//                return false
//            }
//        }
//    }
//
//
//    /**
//     * 歌詞を取得する。
//     * @param listener 結果取得リスナ。
//     */
//    fun obtainLyrics(listener: LyricsObtainListener?) {
//        if (listener == null) {
//            throw IllegalArgumentException()
//        }
//
//        lyricsObtainListener = listener
//        val searchUri = replaceProperty(siteParam[SiteColumn.SEARCH_URI]!!, true, false)
//        Logger.d("Search URL: " + searchUri)
//
//        try {
//            currentBaseUrl = URL(searchUri)
//        } catch (e: MalformedURLException) {
//            Logger.e(e)
//        }
//
//        // 歌詞取得開始
//        handler.postDelayed({
//            currentState = STATE_SEARCH
//            webView.stopLoading()
//            webView.loadUrl(searchUri)
//        }, delay)
//    }
//
//    /**
//     * JavaScript側からコールバックされるオブジェクト。
//     */
//    private inner class JavaScriptInterface internal constructor(private val siteParam: EnumMap<SiteColumn, String>) {
//
//        private var url: String? = null
//
//        internal var lyrics: String? = null
//
//
//        /**
//         * 歌詞の検索結果から歌詞ページを取得。
//         * @param html HTMLソース。
//         */
//        @JavascriptInterface
//        fun getSearchResult(html: String) {
//            Logger.d("Search HTML: " + html)
//
//            try {
//                url = null
//
//                val doc = Jsoup.parse(html)
//                if (siteParam[SiteColumn.RESULT_PAGE_PARSE_TYPE] == SiteColumn.PARSE_TYPE_XPATH) {
//                    // XPath
//                    val e = Xsoup.compile(siteParam[SiteColumn.RESULT_PAGE_PARSE_TEXT]).evaluate(doc).elements
//                    if (e == null || e.size == 0) {
//                        returnLyrics()
//                        return
//                    }
//
//                    // 歌詞ページのURLを取得
//                    val anchor = e[0]
//                    url = anchor.attr("href")
//                } else if (siteParam[SiteColumn.RESULT_PAGE_PARSE_TYPE] == SiteColumn.PARSE_TYPE_REGEXP) {
//                    val parseText = replaceProperty(siteParam[SiteColumn.RESULT_PAGE_PARSE_TEXT]!!, false, true)
//                    Logger.d("Parse Text: " + parseText)
//                    val p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE)
//                    val m = p.matcher(html)
//                    if (m.find()) {
//                        url = m.group(1)
//                    }
//                }
//
//                if (url.isNullOrEmpty()) {
//                    returnLyrics()
//                    return
//                }
//                Logger.d("Lyrics Path: " + url!!)
//
//                // BASE URL取得
//                val e = Xsoup.compile(BASE_PATH).evaluate(doc).elements
//                if (e != null && e.size > 0) {
//                    try {
//                        currentBaseUrl = URL(e[0].attr("href"))
//                    } catch (mue: MalformedURLException) {
//                        Logger.e(mue)
//                    }
//
//                }
//                if (currentBaseUrl != null)
//                    currentBaseUrl = URL(currentBaseUrl, url)
//                Logger.d("Lyrics URL: " + currentBaseUrl!!)
//
//                // 歌詞取得
//                handler.postDelayed({
//                    currentState = STATE_PAGE
//                    webView!!.stopLoading()
//                    webView!!.loadUrl(currentBaseUrl!!.toString())
//                }, delay)
//            } catch (e: Exception) {
//                Logger.e(e)
//                returnLyrics()
//            }
//
//        }
//
//        /**
//         * 歌詞ページから歌詞を取得。
//         * @param html HTMLソース。
//         */
//        @JavascriptInterface
//        fun getLyrics(html: String) {
//            try {
//                Logger.d("Lyrics HTML: " + html)
//
//
//                if (siteParam[SiteColumn.LYRICS_PAGE_PARSE_TYPE] == SiteColumn.PARSE_TYPE_XPATH) {
//                    // XPath
//                    val doc = Jsoup.parse(html)
//                    val e = Xsoup.compile(siteParam[SiteColumn.LYRICS_PAGE_PARSE_TEXT]).evaluate(doc).elements
//                    if (e == null || e.size == 0) {
//                        returnLyrics()
//                        return
//                    }
//
//                    val elem = e[0]
//                    lyrics = elem.html()
//                } else if (siteParam[SiteColumn.LYRICS_PAGE_PARSE_TYPE] == SiteColumn.PARSE_TYPE_REGEXP) {
//                    // 正規表現
//                    val parseText = replaceProperty(siteParam[SiteColumn.LYRICS_PAGE_PARSE_TEXT]!!, false, true)
//                    Logger.d("Parse Text: " + parseText)
//                    val p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)
//                    val m = p.matcher(html)
//                    if (m.find()) {
//                        lyrics = m.group(1)
//                    }
//                }
//
//                // 調整
//                lyrics = AppUtils.adjustLyrics(lyrics)
//                Logger.d("Lyrics: " + lyrics!!)
//
//                // 歌詞を返す
//                returnLyrics(lyrics, siteParam[SiteColumn.SITE_NAME], currentBaseUrl!!.toString())
//            } catch (e: Exception) {
//                currentState = STATE_COMPLETE
//                Logger.e(e)
//                returnLyrics()
//            }
//
//        }
//    }
//
//    /**
//     * 取得した歌詞をイベントハンドラに渡して呼び出し元に返す。
//     * @param lyrics 歌詞テキスト。
//     */
//    private fun returnLyrics(lyrics: String? = null, title: String? = null, uri: String? = null) {
//        if (lyricsObtainListener != null) {
//            lyricsObtainListener!!.onLyricsObtain(lyrics, title, uri)
//        }
//
//        handler.post {
//            webView.stopLoading()
//            webView.webChromeClient = null
//            webView.webViewClient = null
//            webView.destroy()
//        }
//    }
//
//    /**
//     * 文字列のプロパティ情報を入れる。
//     * @param inputText 入力テキスト。
//     * @return 置換え後のテキスト。
//     */
//    private fun replaceProperty(inputText: String, urlEncode: Boolean, escapeRegexp: Boolean): String {
//        // 正規表現作成
//        val regexpBuilder = StringBuilder()
//        for (p in MediaProperty.values()) {
//            regexpBuilder.append("|%").append(p.keyName).append("%")
//        }
//        val regexp = "(" + regexpBuilder.substring(1) + ")"
//
//        // URIを作成
//        val outputBuffer = StringBuilder()
//        val pattern = Pattern.compile(regexp, Pattern.MULTILINE or Pattern.DOTALL)
//        val matcher = pattern.matcher(inputText)
//        var lastIndex = 0
//        while (matcher.find()) {
//            outputBuffer.append(inputText.substring(lastIndex, matcher.start()))
//            val tag = matcher.group() // タグ (%KEY%)
//            val key = tag.substring(1, tag.length - 1) // プロパティキー (KEY)
//            val value = requestPropertyMap.getFirst(key) // プロパティ値
//            if (!value.isNullOrEmpty()) {
//                try {
//                    var text = AppUtils.normalizeText(value)
//                    if (escapeRegexp)
//                        text = text.replace(REGEXP_ESCAPE.toRegex(), "\\\\$1")
//                    if (urlEncode)
//                        text = URLEncoder.encode(text, siteParam[SiteColumn.RESULT_PAGE_URI_ENCODING])
//                    outputBuffer.append(text)
//                } catch (e: UnsupportedEncodingException) {
//                    Logger.e(e)
//                }
//
//            }
//            lastIndex = matcher.start() + tag.length
//        }
//        outputBuffer.append(inputText.substring(lastIndex))
//
//        return outputBuffer.toString()
//    }
//
//    /**
//     * リスナを設定する。
//     * @param listener リスナ。
//     */
//    fun setOnLyricsObtainListener(listener: LyricsObtainListener) {
//        this.lyricsObtainListener = listener
//    }
//
//    /**
//     * 結果を取得するインターフェース。。
//     */
//    interface LyricsObtainListener : EventListener {
//        /**
//         * 取得イベント。
//         * @param lyrics 歌詞。取得に失敗した場合はnull。
//         */
//        fun onLyricsObtain(lyrics: String?, title: String?, uri: String?)
//    }
//
//    companion object {
//
//        /** Javascriptンターフェースオブジェクト名。  */
//        private val JAVASCRIPT_INTERFACE = "android"
//        /** 検索ページ取得スクリプト。  */
//        private val SEARCH_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getSearchResult(document.getElementsByTagName('html')[0].outerHTML);"
//        /** 歌詞ページ取得スクリプト。  */
//        private val LYRICS_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getLyrics(document.getElementsByTagName('html')[0].outerHTML);"
//        /** BASE URI取得用XPATH。  */
//        private val BASE_PATH = "/html/head/base"
//
//        /** 初期状態。  */
//        private val STATE_INIT = 0
//        /** 検索ページ取得中。  */
//        private val STATE_SEARCH = 1
//        /** 歌詞ページ取得中。   */
//        private val STATE_PAGE = 2
//        /** 完了済み。  */
//        private val STATE_COMPLETE = 3
//
//
//        private val REGEXP_ESCAPE = "[\\\\\\*\\+\\.\\?\\{\\}\\(\\)\\[\\]\\^\\$\\-\\|]"
//    }
//
//}
///**
// * 歌詞取得失敗。
// */
//
