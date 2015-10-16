package com.wa2c.android.medoly.plugin.action.lyricsscraper;

/**
 * Created by wa2c on 2015/08/18.
 */
/** 検索URI。 */

import android.content.Context;
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

    /** 検索ページ取得中。 */
    private static final int STATE_SEARCH = 0;
    /** 歌詞ページ取得中。  */
    private static final int STATE_PAGE = 1;
    /** 完了済み。 */
    private static final int STATE_COMPLETE = 2;


    /** 要求プロパティ。 */
    private HashMap<String, String> requestPropertyMap;
    /** 取得パラメータ。 */
    private LyricsObtainParam lyricsObtainParam;
    /** Webページ表示用WebView。 */
    private WebView webView;
    /** 現在の状態 */
    private int currentState = STATE_SEARCH;



    /**
     * コンストラクタ。
     * @param context コンテキスト。
     * @param propertyMap 要求元プロパティマップ。
     */
    public LyricsObtainClient(Context context, HashMap<String, String> propertyMap, LyricsObtainParam param) {
        this.requestPropertyMap = propertyMap;
        this.lyricsObtainParam = param;

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
                handler.postDelayed(loadPage, lyricsObtainParam.DelayMilliseconds);
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



    /**
     * 歌詞を取得する。
     * @param listener 結果取得リスナ。
     */
    public void obtainLyrics(LyricsObtainListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }

        String searchUri = replaceUriTag(lyricsObtainParam.SearchURI);
        this.lyricsObtainListener = listener;
        this.webView.loadUrl(searchUri);
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
        for (ActionPluginParam.MediaProperty p : ActionPluginParam.MediaProperty.values()) {
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




//            String outputHtml = "";
//
//            // cleanerの生成
//            HtmlCleaner cleaner = new HtmlCleaner();
//            CleanerProperties props = cleaner.getProperties();
//            try {
//
//                // Cleanerの実行
//                TagNode node = cleaner.clean( html );
//
//                // XMLに変換してStringWriterに
//                XmlSerializer serializer = new PrettyXmlSerializer( props );
//                StringWriter writer = new StringWriter();
//                serializer.write(node, writer, "utf-8");
//
//                // コンソールに出力
//                outputHtml = writer.getBuffer().toString();
//                System.out.println( outputHtml );
//                writer.close();
//
//            } catch(IOException e) {
//
//            }
//
//
//            try {
//
//
//
////                HtmlDocumentBuilder builder = new HtmlDocumentBuilder();
////                StringReader reader = new StringReader(html);
////                Object obj = builder.parse(new InputSource(reader));
////                org.w3c.dom.Document w3cDoc = builder.parse(new InputSource(reader));
//
////                XPathFactory factory = XPathFactory.newInstance();
////                XPath xpath = factory.newXPath();
////                String str = xpath.evaluate(lyricsObtainParam.SearchAnchorParseText, w3cDoc);
//
////obtain Document somehow, doesn't matter how
//
//                    final XMLReader parser = new Parser();
//
//                    final HTMLSchema schema = new HTMLSchema();
//                    parser.setProperty(Parser.schemaProperty, schema);
//
//                    final StringWriter output = new StringWriter();
//
//                    final XMLWriter serializer = new XMLWriter(output);
//                parser.setContentHandler(serializer);
//
////                    // TODO 確認した時点では、これが期待通りには機能せず。
//                     parser.setDTDHandler(serializer);
////                    // 仕方が無いので、以下の記述により DOCTYPE を強制的に出力させる。(html用)
////                    serializer.setOutputProperty(XMLWriter.DOCTYPE_PUBLIC,
////                            "-//W3C//DTD HTML 4.01 Transitional//EN");
//
//                    // <html> に名前空間をあらわす属性が付かないようにする。
//                    parser.setFeature(Parser.namespacesFeature, false);
//
//
//                    final InputSource input = new InputSource();
//                    input.setCharacterStream(new StringReader(html));
//
////                    // 出力を (xhtmlではなく) html にセットします。
////                    serializer.setOutputProperty(XMLWriter.METHOD, "html");
////
//                    // XML宣言の出力を抑制します。
//                    serializer.setOutputProperty(XMLWriter.OMIT_XML_DECLARATION, "yes");
////
////                    // 属性へのデフォルト付与を抑制させます。
////                    parser.setFeature(Parser.defaultAttributesFeature, false);
//                Parser.
////
//                    // 出力先の文字エンコーディングを指定します。
//                    serializer.setOutputProperty(XMLWriter.ENCODING, "UTF-8");
//
//                    // 知らない名前の要素について寛大に処理します。(jsp対策において必要と想定)
//                    //parser.setFeature(Parser.ignoreBogonsFeature, false);
//
//
//                    // パースを実施。
//                    parser.parse(input);
                //outputHtml = output.toString();

//                DocumentBuilderFactory fac =  DocumentBuilderFactory.newInstance();
//                DocumentBuilder build = fac.newDocumentBuilder();
//                StringReader reader = new StringReader(outputHtml);
//                org.w3c.dom.Document doc2 = build.parse(new InputSource(reader));

//                HtmlDocumentBuilder builder = new HtmlDocumentBuilder();
//                StringReader reader = new StringReader(outputHtml);
//                org.w3c.dom.Document doc2 = builder.parse(new InputSource(reader));


//                Parser parser = new Parser();
//                DefaultHandler  handler = new DefaultHandler();
//                parser.setContentHandler(handler);
//                parser.parse
//                if ( contentHandler != null ) {
//                    parser.setContentHandler(contentHandler);
//                }
//                parser.setFeature(Parser.ignoreBogonsFeature, false);
//                parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
//
//                parser.parse(new InputSource(new InputStreamReader(ins)));

//                Element bodyElement = doc.body();
//                String bodyStr = Jsoup.clean(bodyElement.html(), Whitelist.none());
//                org.w3c.dom.Document doc2 = DOMBuilder.jsoup2DOM(doc);
//
//                XPathFactory factory = XPathFactory.newInstance();
//                XPath xpath = factory.newXPath();
//                String aaa = xpath.evaluate(lyricsObtainParam.SearchAnchorParseText, doc2);

//                XPathEvaluator ev = Xsoup.compile(lyricsObtainParam.SearchAnchorParseText);
//                Document doc = Jsoup.parse(outputHtml);
//                Elements e = Xsoup.compile(lyricsObtainParam.SearchAnchorParseText).evaluate(doc).getElements();
//                if (e == null || e.size() == 0) {
//                    returnLyrics(null);
//                    return;
//                }
//
//
//                // 歌詞ページのURLを取得
//                Element anchor = e.get(0);
//                final String url = anchor.attr("href");
//                if (TextUtils.isEmpty(url)) {
//                    returnLyrics(null);
//                    return;
//                }
//
//                // 歌詞取得
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        currentState = STATE_PAGE;
//                        webView.loadUrl(url);
//                    }
//                });

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

