package com.wa2c.android.medoly.plugin.action.lyricsscraper.search

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.view.View
import android.webkit.*
import com.google.code.regexp.Pattern
import com.wa2c.android.medoly.library.MediaProperty
import com.wa2c.android.medoly.library.PropertyData
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.DbHelper
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.Site
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.prefs.Prefs
import org.jsoup.Jsoup
import timber.log.Timber
import us.codecraft.xsoup.Xsoup
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLEncoder
import java.util.EventListener
import kotlin.collections.LinkedHashMap
import kotlin.collections.set


@SuppressLint("SetJavaScriptEnabled")
class LyricsSearcherWebView constructor(context: Context) : WebView(context) {

    private var propertyData: PropertyData? = null
    private var site: Site? = null
    var currentState = STATE_IDLE
        private set

    /** Event handle listener。  */
    var handleListener: HandleListener? = null

    var searchUri: String? = null

    var timeoutSec: Int = context.resources.getInteger(R.integer.download_timeout_sec)

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

    private val scriptRunnable = Runnable {
        webHandler.removeCallbacks(cancelRunnable)
        when (currentState) {
            STATE_IDLE -> { }
            STATE_SEARCH -> loadUrl(LyricsSearcherWebView.SEARCH_PAGE_GET_SCRIPT)
            STATE_LYRICS -> loadUrl(LyricsSearcherWebView.LYRICS_PAGE_GET_SCRIPT)
            else -> handleListener?.onError()
        }
    }

    private val cancelRunnable = Runnable {
        this.stopLoading()
        handleListener?.onError()
    }



    fun search(propertyData: PropertyData, siteId: Long?): Boolean {
        this.propertyData = propertyData
        val site = DbHelper(context).selectSite(siteId ?: Prefs(context).getLong(R.string.prefkey_selected_site_id, -1))
        return if (site == null)
            false
        else
            search(propertyData, site)
    }

    fun search(propertyData: PropertyData, site: Site): Boolean {
        this.propertyData = propertyData
        this.site = site

        searchUri = replaceProperty(site.search_uri, true, false)
        Timber.d("Search URL: $searchUri")

        try {
            currentState = STATE_SEARCH
            stopLoading()
            loadUrl(searchUri)
            webHandler.postDelayed(cancelRunnable, timeoutSec * 1000L)
        } catch (e: Exception) {
            handleListener?.onError()
        }

        return true
    }

    fun download(url: String) {
        try {
            currentState = STATE_LYRICS
            stopLoading()
            loadUrl(url)
            webHandler.postDelayed(cancelRunnable, timeoutSec * 1000L)
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
                        val url = getFullUrl(urlText, html, searchUri)
                        if (url == Uri.EMPTY)
                            continue
                        val titleText = element.text()
                        if (titleText.isNullOrEmpty())
                            continue

                        val item = ResultItem()
                        item.pageUrl = url.toString()
                        item.pageTitle = titleText
                        item.musicTitle = item.pageTitle
                        searchResultItemList[item.pageUrl!!] = item
                    } catch (ignore: Exception) {
                    }
                }
                Timber.d(searchResultItemList.toString())
            } else if (site!!.result_page_parse_type == Site.PARSE_TYPE_REGEXP) {
                val parseText = replaceProperty(site!!.result_page_parse_text, false, true)
                Timber.d("Parse Text: $parseText")

                val p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE)
                val m = p.matcher(html)
                while (m.find()) {
                    try {
                        //val urlText = m.group(1)
                        val urlText = m.group("url")
                        val url = getFullUrl(urlText, html, searchUri)
                        if (url == Uri.EMPTY)
                            continue

                        val item = ResultItem()
                        item.pageUrl = url.toString()
                        //item.pageTitle = m.group(2)
                        item.pageTitle = m.group("name")
                        item.musicTitle = AppUtils.adjustHtmlText(item.pageTitle)
                        searchResultItemList[item.pageUrl!!] = item
                    } catch (ignore: Exception) {
                    }

                }
            }
        } catch (e: Exception) {
            Timber.e(e)
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
            Timber.d("Lyrics HTML: $html")

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
                Timber.d("Parse Text: $parseText")
                val p = Pattern.compile(parseText, Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)
                val m = p.matcher(html)
                if (m.find()) {
                    lyrics = m.group(1)
                }
            }

            lyrics = AppUtils.adjustHtmlText(lyrics)
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            webHandler.post {
                handleListener?.onGetLyrics(lyrics)
            }
            currentState = STATE_IDLE
        }
    }

    /**
     * Get base URL
     * @param html HTML.
     * @param pageUrl Web page URL.
     */
    private fun getFullUrl(sourceUrl: String?, html: String?, pageUrl: String?): Uri {
        try {
            val url = Uri.parse(sourceUrl)
            if (url.isAbsolute)
                return url
        } catch (ignored: Exception) {
        }

        try {
            if (!html.isNullOrEmpty()) {
                val doc = Jsoup.parse(html)
                val e = doc.getElementsByTag("base")
                if (e != null && !e.isEmpty()) {
                    val baseUrlText = e[0].attr("href")
                    if (!baseUrlText.isNullOrEmpty()) {
                        val baseUrl = URI(baseUrlText)
                        val url = resolve(baseUrl, sourceUrl)
                        return Uri.parse(url.toASCIIString())
                    }
                }
            }
        } catch (ignored: Exception) {}

        try {
            if (!pageUrl.isNullOrEmpty()) {
                val baseUrl = URI(pageUrl)
                //val url = URIUtils.resolve(baseUrl, sourceUrl)
                val url = resolve(baseUrl, sourceUrl)
                return Uri.parse(url.toASCIIString())
            }
        } catch (ignored: Exception) {}

        return Uri.EMPTY
    }

    /**
     * Resolve url. (from HtmlClient)
     * @param baseURI Base url.
     * @param referenceText pathText.
     */
    private fun resolve(baseURI: URI, referenceText: String?): URI {
        val reference = URI.create(referenceText)
        val s = reference.toASCIIString()
        if (s.startsWith("?")) {
            var baseUri = baseURI.toASCIIString()
            val i = baseUri.indexOf('?')
            baseUri = if (i > -1) baseUri.substring(0, i) else baseUri
            return URI.create(baseUri + s)
        }
        val emptyReference = s.isEmpty()
        var resolved: URI
        if (emptyReference) {
            resolved = baseURI.resolve(URI.create("#"))
            val resolvedString = resolved.toASCIIString()
            resolved = URI.create(resolvedString.substring(0, resolvedString.indexOf('#')))
        } else {
            resolved = baseURI.resolve(reference)
        }
        return resolved.normalize()
    }

    /**
     * Replace property
     * @param inputText Input text.
     * @param urlEncode True if url encoding.
     * @param escapeRegexp True if escape regexp.
     * @return Replaces text.
     */
    private fun replaceProperty(inputText: String, urlEncode: Boolean, escapeRegexp: Boolean): String {
        val regexpBuilder = StringBuilder()
        for (p in MediaProperty.values()) {
            regexpBuilder.append("|%").append(p.keyName).append("%")
        }
        val regexp = "(" + regexpBuilder.substring(1) + ")"

        // create uri
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
                    Timber.e(e)
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
        /** Init state  */
        const val STATE_IDLE = 0
        /** Getting search page. */
        const val STATE_SEARCH = 1
        /** Getting lyrics page. */
        const val STATE_LYRICS = 2

        /** Javascript interface object name. */
        const val JAVASCRIPT_INTERFACE = "android"
        /** Search page getting script. */
        const val SEARCH_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getSearchResult(document.getElementsByTagName('html')[0].outerHTML);"
        /** Lyrics page getting script. */
        const val LYRICS_PAGE_GET_SCRIPT = "javascript:window.$JAVASCRIPT_INTERFACE.getLyrics(document.getElementsByTagName('html')[0].outerHTML);"

        const val REGEXP_ESCAPE = "[\\\\\\*\\+\\.\\?\\{\\}\\(\\)\\[\\]\\^\\$\\-\\|]"
    }

}

