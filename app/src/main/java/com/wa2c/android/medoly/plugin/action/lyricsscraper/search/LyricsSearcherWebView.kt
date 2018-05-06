package com.wa2c.android.medoly.plugin.action.lyricsscraper.search

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.view.View
import android.webkit.*
import com.wa2c.android.medoly.library.MediaProperty
import com.wa2c.android.medoly.library.PropertyData
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.DbHelper
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.Site
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Prefs
import org.jsoup.Jsoup
import us.codecraft.xsoup.Xsoup
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.EventListener
import java.util.regex.Pattern
import kotlin.collections.LinkedHashMap
import kotlin.collections.set


@SuppressLint("SetJavaScriptEnabled")
class LyricsSearcherWebView constructor(context: Context) : WebView(context) {

    private var propertyData: PropertyData? = null
    private var site: Site? = null
    private var currentState = STATE_IDLE

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
        webViewClient = (object : WebViewClient() {
//            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
//                super.onPageStarted(view, url, favicon)
//            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                webHandler.postDelayed(scriptRunnable, site?.delay ?: 0)
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                handleListener?.onError()
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                super.shouldOverrideUrlLoading(view, request)
                return false
            }
        })
        addJavascriptInterface(this, JAVASCRIPT_INTERFACE)
    }

    // スクリプトを実行してページを取得
    private val scriptRunnable = Runnable {
        when (currentState) {
            STATE_IDLE -> { }
            STATE_SEARCH -> loadUrl(LyricsSearcherWebView.SEARCH_PAGE_GET_SCRIPT)
            STATE_LYRICS -> loadUrl(LyricsSearcherWebView.LYRICS_PAGE_GET_SCRIPT)
            else -> handleListener?.onError()
        }
    }



    fun search(propertyData: PropertyData, siteId: Long?) {
        this.propertyData = propertyData
        val site = DbHelper(context).selectSite(siteId ?: Prefs(context).getLong(R.string.prefkey_selected_site_id, -1))
        if (site == null)
            return
        else
            search(propertyData, site)
    }

    fun search(propertyData: PropertyData, site: Site) {
        this.propertyData = propertyData
        this.site = site

        val searchUri = replaceProperty(site.search_uri, true, false)
        Logger.d("Search URL: $searchUri")

        try {
            currentState = STATE_SEARCH
            stopLoading()
            loadUrl(searchUri)
        } catch (e: Exception) {
            handleListener?.onError()
        }
    }

    fun download(url: String) {
        try {
            currentState = STATE_LYRICS
            stopLoading()
            loadUrl(url)
        } catch (e: Exception) {
            handleListener?.onError()
        }
    }


    @JavascriptInterface
    fun getSearchResult(html: String) {
        val searchResultItemList = LinkedHashMap<String, ResultItem>()

        try {
            val doc = Jsoup.parse(html)
            if (site!!.result_page_parse_type == Site.PARSE_TYPE_XPATH) {
                // XPath
                val e = Xsoup.compile(site!!.result_page_parse_text).evaluate(doc).elements
                if (e == null || e.size == 0) {
                    return
                }

                for (element in e) {
                    try {
                        val urlText = element.attr("href")
                        var url = Uri.parse(urlText)
                        if (!url.isAbsolute) {
                            //url = Uri.
                            val searchUrl = Uri.parse(site!!.search_uri)
                            url = Uri.Builder()
                                    .scheme(searchUrl.scheme)
                                    .authority(searchUrl.authority)
                                    .path(urlText)
                                    .build()
                        }

                        val item = ResultItem()
                        item.pageUrl = url.toString()
                        item.pageTitle = element.text()
                        item.musicTitle = item.pageTitle
                        if (item.pageUrl.isNullOrEmpty())
                            continue
                        searchResultItemList[item.pageUrl!!] = item
                    } catch (ignore: Exception) {
                    }
                }
                Logger.d(searchResultItemList)
            } else if (site!!.result_page_parse_type == Site.PARSE_TYPE_REGEXP) {
                val parseText = replaceProperty(site!!.result_page_parse_text, false, true)
                Logger.d("Parse Text: $parseText")
                val p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE)
                val m = p.matcher(html)
                while (m.find()) {
                    try {
                        val item = ResultItem()
                        item.pageUrl = m.group(1)
                        item.pageTitle = m.group(1)
                        item.musicTitle = item.pageTitle
                        if (item.pageUrl.isNullOrEmpty())
                            continue
                        searchResultItemList[item.pageUrl!!] = item
                    } catch (ignore: Exception) {
                    }

                }
            }
        } catch (e: Exception) {
            Logger.e(e)
        } finally {
            webHandler.post {
                if (!searchResultItemList.isEmpty())
                    handleListener?.onSearchResult(searchResultItemList.values.toList())
                else
                    handleListener?.onError()
            }
        }
    }

    @JavascriptInterface
    fun getLyrics(html: String) {
        var lyrics: String? = null
        try {
            Logger.d("Lyrics HTML: $html")

            if (site!!.lyrics_page_parse_type == Site.PARSE_TYPE_XPATH) {
                // XPath
                val doc = Jsoup.parse(html)
                val e = Xsoup.compile(site!!.lyrics_page_parse_text).evaluate(doc).elements
                if (e == null || e.size == 0) {
                    return
                }

                val elem = e[0]
                lyrics = elem.html()
            } else if (site!!.lyrics_page_parse_type == Site.PARSE_TYPE_REGEXP) {
                // Regular expression
                val parseText = replaceProperty(site!!.lyrics_page_parse_text, false, true)
                Logger.d("Parse Text: $parseText")
                val p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)
                val m = p.matcher(html)
                if (m.find()) {
                    lyrics = m.group(1)
                }
            }

            lyrics = AppUtils.adjustLyrics(lyrics)
        } catch (e: Exception) {
            Logger.e(e)
        } finally {
            webHandler.post {
                handleListener?.onGetLyrics(lyrics)
            }
            currentState = STATE_IDLE
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
                        text = URLEncoder.encode(text, site!!.result_page_uri_encoding)
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
        fun onError(message: String? = null)
    }

    companion object {
        /** 初期状態。  */
        const val STATE_IDLE = 0
        /** 検索ページ取得中。  */
        const val STATE_SEARCH = 1
        /** 歌詞ページ取得中。   */
        const val STATE_LYRICS = 2

        /** Javascriptンターフェースオブジェクト名。  */
        const val JAVASCRIPT_INTERFACE = "android"
        /** 検索ページ取得スクリプト。  */
        const val SEARCH_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getSearchResult(document.getElementsByTagName('html')[0].outerHTML);"
        /** 歌詞ページ取得スクリプト。  */
        const val LYRICS_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getLyrics(document.getElementsByTagName('html')[0].outerHTML);"

        const val REGEXP_ESCAPE = "[\\\\\\*\\+\\.\\?\\{\\}\\(\\)\\[\\]\\^\\$\\-\\|]"
    }

}

