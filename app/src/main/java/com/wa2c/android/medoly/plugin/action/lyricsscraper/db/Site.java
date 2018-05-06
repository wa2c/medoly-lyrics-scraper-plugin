package com.wa2c.android.medoly.plugin.action.lyricsscraper.db;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import java.io.Serializable;

/**
 * Site. (Orma Model)
 */
@Table(value = "site")
public class Site implements Serializable {

    @PrimaryKey(autoincrement = true)
    public long _id;

    @Column(indexed = true)
    public long site_id;

    @Column
    public long group_id;

    @Column(indexed = true)
    public String site_name;

    @Column
    public String site_uri;

    @Column
    public String search_uri;

    @Column
    public String result_page_uri_encoding;

    @Column
    public String result_page_encoding;

    @Column
    public String result_page_parse_type;

    @Column
    public String result_page_parse_text;

    @Column
    public String lyrics_page_encoding;

    @Column
    public String lyrics_page_parse_type;

    @Column
    public String lyrics_page_parse_text;

    @Column
    public Long delay;

    @Column
    public Long timeout;



    /** Parse type: XPath.  */
    public static final String PARSE_TYPE_XPATH = "XPath";
    /** Parse type: Regular Expression.  */
    public static final String PARSE_TYPE_REGEXP = "RegularExpression";

}
