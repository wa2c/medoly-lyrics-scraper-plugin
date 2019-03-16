package com.wa2c.android.medoly.plugin.action.lyricsscraper.db

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Setter
import com.github.gfx.android.orma.annotation.Table

import java.io.Serializable

/**
 * Site. (Orma Model)
 */
@Table(value = "site")
data class Site (
        @Setter @PrimaryKey(autoincrement = true) var _id: Long,
        @Setter @Column(indexed = true) var site_id: Long,
        @Setter @Column var group_id: Long = 0,
        @Setter @Column(indexed = true) var site_name: String?,
        @Setter @Column var site_uri: String?,
        @Setter @Column var search_uri: String?,
        @Setter @Column var result_page_uri_encoding: String?,
        @Setter @Column var result_page_encoding: String?,
        @Setter @Column var result_page_parse_type: String?,
        @Setter @Column var result_page_parse_text: String?,
        @Setter @Column var lyrics_page_encoding: String?,
        @Setter @Column var lyrics_page_parse_type: String?,
        @Setter @Column var lyrics_page_parse_text: String?,
        @Setter @Column var delay: Long?,
        @Setter @Column var timeout: Long?
): Serializable {
    companion object {
        /** Parse type: XPath.   */
        const val PARSE_TYPE_XPATH = "XPath"
        /** Parse type: Regular Expression.   */
        const val PARSE_TYPE_REGEXP = "RegularExpression"
    }
}
