package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wa2c on 2015/10/15.
 */
public class LyricsObtainParam {
    /** パース種別:XPATH */
    public static int ParseTypeXPath = 0;
    /** パース種別:正規表現 */
    public static int ParseTypeRegexp = 1;

    /** ID。 */
    public int Id;

    /** サイト名。 */
    public String SiteName;
    /** サイトURI。 */
    public String SiteUri;
    /** 検索URI。 */
    public String SearchURI;

    /** アンカーのパース種別。0:XPath, 1:正規表現 */
    public int SearchAnchorParseType = ParseTypeXPath;
    /** 検索結果ページの歌詞ページへのアンカーパース文字列。(XPATH/正規表現) */
    public String SearchAnchorParseText;

    /** 歌詞のパース種別。0:XPath, 1:正規表現 */
    public int SearchLyricsParseType = ParseTypeXPath;
    /** 歌詞ページの対象歌詞パース文字列。(XPATH/正規表現) */
    public String SearchLyricsParseText;

    /** URIのエンコーディング。 */
    public String URIEncoding = "UTF-8";
    /** ページのエンコーディング。 */
    public String PageEncoding = "UTF-8";

    public long TimeoutMilliseconds = 10000;
    /** 処理遅延。 */
    public long DelayMilliseconds = 0;


    /**
     * 表示用リストを取得する。
     * @param context コンテキスト。
     * @return リスト。
     */
    public List<Pair<Integer, String>> getList(Context context) {
        List<Pair<Integer, String>> list = new ArrayList<>();
        list.add(new Pair<>(R.string.site_name, SiteName));
        list.add(new Pair<>(R.string.site_uri, SiteUri));
        list.add(new Pair<>(R.string.search_uri, SearchURI));
        if (SearchAnchorParseType == ParseTypeXPath) list.add(new Pair<>(R.string.search_anchor_parse_type, context.getString(R.string.xpath)));
        else if (SearchAnchorParseType == ParseTypeRegexp) list.add(new Pair<>(R.string.search_anchor_parse_type, context.getString(R.string.regular_expression)));
        list.add(new Pair<>(R.string.search_anchor_parse_text, SearchAnchorParseText));
        if (SearchLyricsParseType == ParseTypeXPath) list.add(new Pair<>(R.string.search_lyrics_parse_type, context.getString(R.string.xpath)));
        else if (SearchLyricsParseType == ParseTypeRegexp) list.add(new Pair<>(R.string.search_lyrics_parse_type, context.getString(R.string.regular_expression)));
        list.add(new Pair<>(R.string.search_lyrics_parse_text, SearchLyricsParseText));
        list.add(new Pair<>(R.string.uri_encoding, URIEncoding));
        list.add(new Pair<>(R.string.page_encoding, PageEncoding));
        list.add(new Pair<>(R.string.timeout_milliseconds, Long.toString(TimeoutMilliseconds)));
        list.add(new Pair<>(R.string.delay_milliseconds, Long.toString(DelayMilliseconds)));

        return list;
    }


    // TODO 仮置き
    public static Map<Integer, LyricsObtainParam> getParamMap() {
        LinkedHashMap<Integer, LyricsObtainParam> map = new LinkedHashMap<>();

        LyricsObtainParam param1 = new LyricsObtainParam();
        param1.Id = 0;
        param1.SiteName = "歌詞タイム";
        param1.SiteUri = "http://www.kasi-time.com/";
        //param1.SearchURI = "http://www.kasi-time.com/allsearch.php?q=item+%s+%s";
        param1.SearchURI = "http://www.kasi-time.com/allsearch.php?q=item+%MEDIA_TITLE%+%MEDIA_ARTIST%";
        param1.SearchAnchorParseType = LyricsObtainParam.ParseTypeXPath;
        param1.SearchAnchorParseText = "//a[@class='gs-title']";
        param1.SearchLyricsParseType = LyricsObtainParam.ParseTypeXPath;
        param1.SearchLyricsParseText = "//div[@id='lyrics']";
        param1.DelayMilliseconds = 2000;
        map.put(param1.Id, param1);

        LyricsObtainParam param2 = new LyricsObtainParam();
        param2.Id = 1;
        param2.SiteName = "J-Lyric.net";
        param2.SiteUri = "http://j-lyric.net/";
        param2.SearchURI = "http://search.j-lyric.net/index.php?kt=%MEDIA_TITLE%&ct=0&ka=%MEDIA_ARTIST%&ca=0";
        param2.SearchAnchorParseType = LyricsObtainParam.ParseTypeRegexp;
        param2.SearchAnchorParseText = "<div class=\"title\"><a href=\"(.*?)\".*?</div>";
        param2.SearchLyricsParseType = LyricsObtainParam.ParseTypeRegexp;
        param2.SearchLyricsParseText = "<p id=\"lyricBody\">(.*?)</p>";
        map.put(param2.Id, param2);

        return map;
    }


}
