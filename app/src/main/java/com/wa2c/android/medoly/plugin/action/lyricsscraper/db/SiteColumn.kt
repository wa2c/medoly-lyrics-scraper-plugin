package com.wa2c.android.medoly.plugin.action.lyricsscraper.db

/**
 * Site Column.
 */
enum class SiteColumn constructor(val columnName: String, val columnKey: String, private val constraint: String) {
    /** ID.  */
    _ID("_ID", "_id", "INTEGER PRIMARY KEY AUTOINCREMENT"),
    /** Site ID.  */
    SITE_ID("SITE_ID", "siteid", "INTEGER NOT NULL"),
    /** Group ID.  */
    GROUP_ID("GROUP_ID", "groupid", "INTEGER NOT NULL"),
    /** Site name.  */
    SITE_NAME("SITE_NAME", "sitename", "TEXT NOT NULL"),
    /** Site URI.  */
    SITE_URI("SITE_URI", "siteuri", "TEXT NOT NULL"),
    /** Search URI.  */
    SEARCH_URI("SEARCH_URI", "searchuri", "TEXT NOT NULL"),
    /** Search result page URI encoding.  */
    RESULT_PAGE_URI_ENCODING("RESULT_PAGE_URI_ENCODING", "resultpageuriencoding", "TEXT NOT NULL"),
    /** Search result page encoding.  */
    RESULT_PAGE_ENCODING("RESULT_PAGE_ENCODING", "resultpageencoding", "TEXT NOT NULL"),
    /** Search result page parsing type.  */
    RESULT_PAGE_PARSE_TYPE("RESULT_PAGE_PARSE_TYPE", "resultpageparsetype", "TEXT NOT NULL"),
    /** Search result page parsing string.  */
    RESULT_PAGE_PARSE_TEXT("RESULT_PAGE_PARSE_TEXT", "resultpageparsetext", "TEXT NOT NULL"),
    /** Lyrics page encoding.  */
    LYRICS_PAGE_ENCODING("LYRICS_PAGE_ENCODING", "lyricspageencoding", "TEXT NOT NULL"),
    /** Lyrics page parsing type.  */
    LYRICS_PAGE_PARSE_TYPE("LYRICS_PAGE_PARSE_TYPE", "lyricspageparsetype", "TEXT NOT NULL"),
    /** Lyrics page parsing string.  */
    LYRICS_PAGE_PARSE_TEXT("LYRICS_PAGE_PARSE_TEXT", "lyricspageparsetext", "TEXT NOT NULL"),
    /** Loading delay.  */
    DELAY("DELAY", "delay", "TEXT NOT NULL"),
    /** Loading timeout.  */
    TIMEOUT("TIMEOUT", "timeout", "TEXT NOT NULL");

    /** Get column constraint.  */
    fun getConstraint(): String {
        return "$columnKey $constraint"
    }

    companion object {

        /** Parse type: XPath.  */
        var PARSE_TYPE_XPATH = "XPath"
        /** Parse type: Regular Expression.  */
        var PARSE_TYPE_REGEXP = "RegularExpression"
    }

}
