package com.wa2c.android.medoly.plugin.action.lyricsscraper.db;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Index;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;
import com.google.gson.Gson;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.ResultItem;

import java.io.Serializable;
import java.util.Date;

/**
 * Search condition. (Orma Model)
 */
@Table(value = "search_cache", indexes = @Index(value= {"title", "artist"}))
public class SearchCache implements Serializable {

    /** Key. */
    @PrimaryKey(autoincrement = true)
    public long _id;

    /** Search title. */
    @Column(indexed = true)
    public String title;

    /** Search artist. */
    @Nullable
    @Column(indexed = true)
    public String artist;

    /** Search album. */
    @Nullable
    @Column(indexed = true)
    public String album;

    /** Search result has lyrics. */
    @Nullable
    @Column
    public Boolean has_lyrics;

    /** Search result. (JSON)  */
    @Column
    public String result;

    /** Added date. */
    @Nullable
    @Column(defaultExpr = "CURRENT_TIMESTAMP")
    public Date date_added;

    /** Modified date. */
    @Nullable
    @Column(defaultExpr = "CURRENT_TIMESTAMP")
    public Date date_modified;




    /**
     * Get ResultItem from result field.
     * @return result item.
     */
    public ResultItem makeResultItem() {
        if (TextUtils.isEmpty(result))
            return null;
        return (new Gson()).fromJson(result, ResultItem.class);
    }

    /**
     * Set ResultItem to result field.
     * @param item result item.
     */
    public void updateResultItem(ResultItem item) {
        if (item == null)
            return;
        result = (new Gson()).toJson(item);
    }

}
