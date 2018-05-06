package com.wa2c.android.medoly.plugin.action.lyricsscraper.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import com.wa2c.android.medoly.library.MediaProperty
import com.wa2c.android.medoly.library.PropertyData
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCacheHelper
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
class LyricsSearcherWebView2 constructor(context: Context) : WebView(context) {

    private val lyricsWebClient = LyricsWebClient2(this)
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

    fun search(propertyData: PropertyData, siteId: Long?) {
        this.propertyData = propertyData
        val site = SearchCacheHelper(context).selectSite(siteId ?: Prefs(context).getLong(R.string.prefkey_selected_site_id, -1))
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
            lyricsWebClient.setState(LyricsWebClient2.STATE_SEARCH)
            stopLoading()
            loadUrl(searchUri)
        } catch (e: Exception) {
            handleListener?.onError("")
        }
    }

    fun download(url: String) {
        try {
            lyricsWebClient.setState(LyricsWebClient2.STATE_LYRICS)
            stopLoading()
            loadUrl(url)
        } catch (e: Exception) {
            handleListener?.onError("Error!!")
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
                        val item = ResultItem()
                        item.pageUrl = element.attr("href")
                        item.musicTitle = element.text()
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
                        item.musicTitle = m.group(1)
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
            webHandler.post { handleListener?.onSearchResult(searchResultItemList.values.toList()) }
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
                // 正規表現
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
            lyricsWebClient.setState(LyricsWebClient2.STATE_IDLE)
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
        fun onError(message: String?)
    }

    companion object {
        /** Javascriptンターフェースオブジェクト名。  */
        const val JAVASCRIPT_INTERFACE = "android"
        /** 検索ページ取得スクリプト。  */
        const val SEARCH_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getSearchResult(document.getElementsByTagName('html')[0].outerHTML);"
        /** 歌詞ページ取得スクリプト。  */
        const val LYRICS_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getLyrics(document.getElementsByTagName('html')[0].outerHTML);"

        const val REGEXP_ESCAPE = "[\\\\\\*\\+\\.\\?\\{\\}\\(\\)\\[\\]\\^\\$\\-\\|]"
    }

}

