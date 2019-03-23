package com.wa2c.android.medoly.plugin.action.lyricsscraper.db

import com.github.gfx.android.orma.annotation.*
import com.google.gson.Gson
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.ResultItem
import java.io.Serializable
import java.util.*

/**
 * Search condition. (Orma Model)
 */
@Table(value = "search_cache", indexes = [Index(value = ["title", "artist"])])
data class SearchCache(
        @Setter @PrimaryKey(autoincrement = true) var _id: Long,
        @Setter @Column(indexed = true) var title: String?,
        @Setter @Column(indexed = true) var artist: String?,
        @Setter @Column var language: String?,
        @Setter @Column var from: String?,
        @Setter @Column var file_name: String?,
        @Setter @Column var has_lyrics: Boolean?,
        @Setter @Column var result: String?,
        @Setter @Column(defaultExpr = "CURRENT_TIMESTAMP") var date_added: Date?,
        @Setter @Column(defaultExpr = "CURRENT_TIMESTAMP") var date_modified: Date?
): Serializable {

    /**
     * Get ResultItem from result field.
     * @return result item.
     */
    fun makeResultItem(): ResultItem? {
        return if (result == null || result!!.isEmpty()) null else Gson().fromJson(result, ResultItem::class.java)
    }

    /**
     * Set ResultItem to result field.
     * @param item result item.
     */
    fun updateResultItem(item: ResultItem?) {
        if (item == null)
            return
        result = Gson().toJson(item)
    }

}
