package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import java.util.List;

/**
 * Created by wa2c on 2015/10/15.
 */
public class LyricsObtainParam {
    /** パース種別:XPATH */
    public static int ParseTypeXPath = 0;
    /** パース種別:正規表現 */
    public static int ParseTypeRegexp = 1;

    /** サイト名。 */
    public String SiteName;
    /** サイトURI。 */
    public String SiteUri;
    /** 検索URI。 */
    public String SearchURI;
    /** 検索追加キーワード。 */
    public List<String> SearchParamKeyList;

    /** 検索結果ページの歌詞ページへのアンカーパース表現。(XPATH/正規表現) */
    public String SearchAnchorExpression;
    /** アンカーのパース種別。0:XPath, 1:正規表現 */
    public int SearchAnchorParseType = ParseTypeXPath;

    /** 歌詞ページの対象歌詞パース表現。(XPATH/正規表現) */
    public String SearchLyricsExpression;
    /** 歌詞のパース種別。0:XPath, 1:正規表現 */
    public int SearchLyricsParseType = ParseTypeXPath;

    /** URIのエンコーディング。 */
    public String URIEncoding = "UTF-8";
    /** ページのエンコーディング。 */
    public String PageEncoding = "UTF-8";

    public long TimeoutMilliseconds = 10000;
    /** 処理遅延。 */
    public long DelayMilliseconds = 3000;

    /** タグの除去。 */
    public boolean RemoveTag = true;
}
