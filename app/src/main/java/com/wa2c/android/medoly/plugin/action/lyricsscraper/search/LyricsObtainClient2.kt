package com.wa2c.android.medoly.plugin.action.lyricsscraper.search

import android.content.Context
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import android.webkit.*
import com.wa2c.android.medoly.library.MediaProperty
import com.wa2c.android.medoly.library.PropertyData
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCacheHelper
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.Site
import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotFoundException
import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotSelectException
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger
import org.jsoup.Jsoup
import us.codecraft.xsoup.Xsoup
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern


/**
 * Lyrics obtain client
 */
class LyricsObtainClient2
@Throws(SiteNotFoundException::class, SiteNotSelectException::class)
constructor(context: Context, private val requestPropertyMap: PropertyData) {
    /** Webページ表示用WebView。  */
    private val webView: WebView
    /** 現在の状態  */
    private var currentState = STATE_INIT
    /** 現在のBase URI  */
    private var currentBaseUrl: URL? = null

    /** サイト情報。  */
    private val siteParam: Site


    /** ハンドラ。  */
    private val handler = Handler()


    /** リスナ。  */
    private var lyricsObtainListener: LyricsObtainListener? = null


    init {

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val siteId = preferences.getLong(context.getString(R.string.prefkey_selected_site_id), -1)
        if (siteId < 0)
            throw SiteNotSelectException()

        siteParam = SearchCacheHelper(context).selectSite(siteId)

        this.webView = WebView(context)
        this.webView.settings.setAppCacheEnabled(true)
        this.webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        this.webView.settings.loadsImagesAutomatically = false
        this.webView.settings.javaScriptEnabled = true
        this.webView.settings.domStorageEnabled = true
        this.webView.settings.databaseEnabled = true
        this.webView.settings.userAgentString = context.getString(R.string.app_user_agent)
        this.webView.visibility = View.INVISIBLE
        this.webView.addJavascriptInterface(JavaScriptInterface(siteParam), JAVASCRIPT_INTERFACE)
        this.webView.webViewClient = object : WebViewClient() {

            // スクリプトを実行してページを取得
            private val executeScript = Runnable {
                when (currentState) {
                    STATE_SEARCH ->
                        // 歌詞検索結果の取得スクリプト実行
                        webView.loadUrl(SEARCH_PAGE_GET_SCRIPT)
                    STATE_PAGE ->
                        // 歌詞ページの取得スクリプト実行
                        webView.loadUrl(LYRICS_PAGE_GET_SCRIPT)
                    else ->
                        //
                        returnLyrics()
                }
            }

            // ページ読み込み完了
            override fun onPageFinished(view: WebView, url: String) {
                // Ajax処理待ちの遅延
                handler.postDelayed(executeScript, siteParam.delay)
            }

            // エラー
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                returnLyrics()
            }

            // 同じビューで再読込
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return false
            }
        }
    }


    /**
     * 歌詞を取得する。
     * @param listener 結果取得リスナ。
     */
    fun obtainLyrics(listener: LyricsObtainListener?) {
        if (listener == null) {
            throw IllegalArgumentException()
        }

        lyricsObtainListener = listener
        val searchUri = replaceProperty(siteParam.search_uri, true, false)
        Logger.d("Search URL: $searchUri")

        try {
            currentBaseUrl = URL(searchUri)
        } catch (e: MalformedURLException) {
            Logger.e(e)
        }

        // 歌詞取得開始
        handler.postDelayed({
            currentState = STATE_SEARCH
            webView.stopLoading()
            webView.loadUrl(searchUri)
        }, siteParam.delay)
    }

    /**
     * JavaScript側からコールバックされるオブジェクト。
     */
    private inner class JavaScriptInterface internal constructor(private val siteParam: Site) {

        private var url: String? = null

        internal var lyrics: String? = null


        /**
         * 歌詞の検索結果から歌詞ページを取得。
         * @param html HTMLソース。
         */
        @JavascriptInterface
        fun getSearchResult(html: String) {
            Logger.d("Search HTML: $html")

            try {
                url = null

                val doc = Jsoup.parse(html)
                if (siteParam.result_page_parse_type == Site.PARSE_TYPE_XPATH) {
                    // XPath
                    val e = Xsoup.compile(siteParam.result_page_parse_text).evaluate(doc).elements
                    if (e == null || e.size == 0) {
                        returnLyrics()
                        return
                    }

                    // 歌詞ページのURLを取得
                    val anchor = e[0]
                    url = anchor.attr("href")
                } else if (siteParam.result_page_parse_type == Site.PARSE_TYPE_REGEXP) {
                    val parseText = replaceProperty(siteParam.result_page_parse_text, false, true)
                    Logger.d("Parse Text: $parseText")
                    val p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE)
                    val m = p.matcher(html)
                    if (m.find()) {
                        url = m.group(1)
                    }
                }

                if (url.isNullOrEmpty()) {
                    returnLyrics()
                    return
                }
                Logger.d("Lyrics Path: " + url!!)

                // BASE URL取得
                val e = Xsoup.compile(BASE_PATH).evaluate(doc).elements
                if (e != null && e.size > 0) {
                    try {
                        currentBaseUrl = URL(e[0].attr("href"))
                    } catch (mue: MalformedURLException) {
                        Logger.e(mue)
                    }

                }
                if (currentBaseUrl != null)
                    currentBaseUrl = URL(currentBaseUrl, url)
                Logger.d("Lyrics URL: " + currentBaseUrl!!)

                // 歌詞取得
                handler.postDelayed({
                    currentState = STATE_PAGE
                    webView.stopLoading()
                    webView.loadUrl(currentBaseUrl!!.toString())
                }, siteParam.delay)
            } catch (e: Exception) {
                Logger.e(e)
                returnLyrics()
            }

        }

        /**
         * 歌詞ページから歌詞を取得。
         * @param html HTMLソース。
         */
        @JavascriptInterface
        fun getLyrics(html: String) {
            try {
                Logger.d("Lyrics HTML: $html")


                if (siteParam.lyrics_page_parse_type == Site.PARSE_TYPE_XPATH) {
                    // XPath
                    val doc = Jsoup.parse(html)
                    val e = Xsoup.compile(siteParam.lyrics_page_parse_text).evaluate(doc).elements
                    if (e == null || e.size == 0) {
                        returnLyrics()
                        return
                    }

                    val elem = e[0]
                    lyrics = elem.html()
                } else if (siteParam.lyrics_page_parse_type == Site.PARSE_TYPE_REGEXP) {
                    // 正規表現
                    val parseText = replaceProperty(siteParam.lyrics_page_parse_text, false, true)
                    Logger.d("Parse Text: $parseText")
                    val p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)
                    val m = p.matcher(html)
                    if (m.find()) {
                        lyrics = m.group(1)
                    }
                }

                // 調整
                lyrics = AppUtils.adjustLyrics(lyrics)
                Logger.d("Lyrics: " + lyrics!!)

                // 歌詞を返す
                returnLyrics(lyrics, siteParam.site_name, currentBaseUrl!!.toString())
            } catch (e: Exception) {
                currentState = STATE_COMPLETE
                Logger.e(e)
                returnLyrics()
            }

        }
    }

    /**
     * 取得した歌詞をイベントハンドラに渡して呼び出し元に返す。
     * @param lyrics 歌詞テキスト。
     */
    private fun returnLyrics(lyrics: String? = null, title: String? = null, uri: String? = null) {
        if (lyricsObtainListener != null) {
            lyricsObtainListener!!.onLyricsObtain(lyrics, title, uri)
        }

        handler.post {
            webView.stopLoading()
            webView.webChromeClient = null
            webView.webViewClient = null
            webView.destroy()
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
            val value = requestPropertyMap.getFirst(key) // プロパティ値
            if (!value.isNullOrEmpty()) {
                try {
                    var text = AppUtils.normalizeText(value)
                    if (escapeRegexp)
                        text = text.replace(REGEXP_ESCAPE.toRegex(), "\\\\$1")
                    if (urlEncode)
                        text = URLEncoder.encode(text, siteParam.result_page_uri_encoding)
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
     * リスナを設定する。
     * @param listener リスナ。
     */
    fun setOnLyricsObtainListener(listener: LyricsObtainListener) {
        this.lyricsObtainListener = listener
    }

    /**
     * 結果を取得するインターフェース。。
     */
    interface LyricsObtainListener : EventListener {
        /**
         * 取得イベント。
         * @param lyrics 歌詞。取得に失敗した場合はnull。
         */
        fun onLyricsObtain(lyrics: String?, title: String?, uri: String?)
    }

    companion object {

        /** Javascriptンターフェースオブジェクト名。  */
        private const val JAVASCRIPT_INTERFACE = "android"
        /** 検索ページ取得スクリプト。  */
        private const val SEARCH_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getSearchResult(document.getElementsByTagName('html')[0].outerHTML);"
        /** 歌詞ページ取得スクリプト。  */
        private const val LYRICS_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getLyrics(document.getElementsByTagName('html')[0].outerHTML);"
        /** BASE URI取得用XPATH。  */
        private const val BASE_PATH = "/html/head/base"

        /** 初期状態。  */
        private const val STATE_INIT = 0
        /** 検索ページ取得中。  */
        private const val STATE_SEARCH = 1
        /** 歌詞ページ取得中。   */
        private const val STATE_PAGE = 2
        /** 完了済み。  */
        private const val STATE_COMPLETE = 3


        private const val REGEXP_ESCAPE = "[\\\\\\*\\+\\.\\?\\{\\}\\(\\)\\[\\]\\^\\$\\-\\|]"
    }

}
/**
 * 歌詞取得失敗。
 */

