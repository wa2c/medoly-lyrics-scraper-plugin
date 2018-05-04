package com.wa2c.android.medoly.plugin.action.lyricsscraper.search

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class LyricsWebClient2(private val lyricsSearcherWebView: LyricsSearcherWebView2) : WebViewClient() {

    private var state = STATE_IDLE

    var delay = 1000L


    // スクリプトを実行してページを取得
    private val scriptRunnable = Runnable {
        when (state) {
            STATE_IDLE -> { }
            STATE_SEARCH -> lyricsSearcherWebView.loadUrl(LyricsSearcherWebView2.SEARCH_PAGE_GET_SCRIPT)
            STATE_LYRICS -> lyricsSearcherWebView.loadUrl(LyricsSearcherWebView2.LYRICS_PAGE_GET_SCRIPT)
            else -> lyricsSearcherWebView.handleListener?.onError(null)
        }
    }

    fun setState(state: Int) {
        this.state = state
    }

    override fun onPageFinished(view: WebView, url: String) {
        // Ajax処理待ちの遅延
        lyricsSearcherWebView.webHandler.postDelayed(scriptRunnable, delay)
    }

    // エラー
    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        //returnLyrics();
    }

    // 同じビューで再読込
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return false
    }

    companion object {
        /** 初期状態。  */
        const val STATE_IDLE = 0
        /** 検索ページ取得中。  */
        const val STATE_SEARCH = 1
        /** 歌詞ページ取得中。   */
        const val STATE_LYRICS = 2
    }
}