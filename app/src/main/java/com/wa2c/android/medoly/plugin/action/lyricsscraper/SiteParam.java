package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;

import java.util.HashMap;


public class SiteParam {

    private String SPREADSHEET_PATH = "https://docs.google.com/spreadsheets/d/1rAtZzpOmwkAigUJ4ENOCglaXPK5klmGfAhGasQrVg6U/edit#gid=1919240742";

//    public static final String ROW_NAMES[] = {
//            "SITE_ID",
//            "GROUP_ID",
//            "SITE_NAME",
//            "SITE_URI",
//            "SEARCH_URI",
//            "RESULT_PAGE_URI_ENCODING",
//            "RESULT_PAGE_ENCODING",
//            "RESULT_PAGE_PARSE_TYPE",
//            "RESULT_PAGE_PARSE_TEXT",
//            "LYRICS_PAGE_ENCODING",
//            "LYRICS_PAGE_PARSE_TYPE",
//            "LYRICS_PAGE_PARSE_TEXT",
//            "DELAY",
//            "TIMEOUT"
//    };
//
//    public static final String ROW_KEYS[] = {
//            "siteid",
//            "groupid",
//            "sitename",
//            "siteuri",
//            "searchuri",
//            "resultpageuriencoding",
//            "resultpageencoding",
//            "resultpageparsetype",
//            "resultpageparsetext",
//            "lyricspageencoding",
//            "lyricspageparsetype",
//            "lyricspageparsetext",
//            "delay",
//            "timeout"
//    };


    public static final String SHEET_NAME = "SITE";

    private HashMap<String, String> valueMap;



    /**
     * Site Column.
     */
    public enum SiteColumn {
        /** Site ID. */
        SITE_ID                  ("SITE_ID"                  , "siteid"                ),
        /** Group ID. */
        GROUP_ID                 ("GROUP_ID"                 , "groupid"               ),
        /** Site name. */
        SITE_NAME                ("SITE_NAME"                , "sitename"              ),
        /** Site URI. */
        SITE_URI                 ("SITE_URI"                 , "siteuri"               ),
        /** Search URI. */
        SEARCH_URI               ("SEARCH_URI"               , "searchuri"             ),
        /** Search result page URI encoding. */
        RESULT_PAGE_URI_ENCODING ("RESULT_PAGE_URI_ENCODING" , "resultpageuriencoding" ),
        /** Search result page encoding. */
        RESULT_PAGE_ENCODING     ("RESULT_PAGE_ENCODING"     , "resultpageencoding"    ),
        /** Search result page parsing type. */
        RESULT_PAGE_PARSE_TYPE   ("RESULT_PAGE_PARSE_TYPE"   , "resultpageparsetype"   ),
        /** Search result page parsing string. */
        RESULT_PAGE_PARSE_TEXT   ("RESULT_PAGE_PARSE_TEXT"   , "resultpageparsetext"   ),
        /** Lyrics page encoding. */
        LYRICS_PAGE_ENCODING     ("LYRICS_PAGE_ENCODING"     , "lyricspageencoding"    ),
        /** Lyrics page parsing type. */
        LYRICS_PAGE_PARSE_TYPE   ("LYRICS_PAGE_PARSE_TYPE"   , "lyricspageparsetype"   ),
        /** Lyrics page parsing string. */
        LYRICS_PAGE_PARSE_TEXT   ("LYRICS_PAGE_PARSE_TEXT"   , "lyricspageparsetext"   ),
        /** Loading delay. */
        DELAY                    ("DELAY"                    , "delay"                 ),
        /** Loading timeout. */
        TIMEOUT                  ("TIMEOUT"                  , "timeout"               );

        /** Constructor. */
        SiteColumn(String name, String key) {
            this.columnName = name;
            this.columnKey = key;
        }

        /** Column name. */
        private String columnName;
        /** Column key. */
        private String columnKey;

        /** Get column name. */
        public String getColumnName() {
            return this.columnName;
        }
        /** Get column key. */
        public String getColumnKey() {
            return this.columnKey;
        }

    }



    /**
     * Constructor
     * @param entry Spreadsheet worksheet entry.
     */
    public SiteParam(ListEntry entry) {
        super();

        valueMap = new HashMap<>();

        for (String tag : entry.getCustomElements().getTags()) {
            valueMap.put(tag, entry.getCustomElements().getValue(tag));
        }
    }

    /**
     * Get Value.
     * @param column Column.
     * @return Column value.
     */
    public String getValue(SiteColumn column) {
        return valueMap.get(column.getColumnKey());
    }

}

