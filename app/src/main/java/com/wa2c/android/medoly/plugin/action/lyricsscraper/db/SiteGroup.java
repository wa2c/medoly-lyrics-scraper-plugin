package com.wa2c.android.medoly.plugin.action.lyricsscraper.db;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import java.io.Serializable;

/**
 * Site group. (Orma Model)
 */
@Table(value = "site_group")
public class SiteGroup implements Serializable {

    @PrimaryKey(autoincrement = true)
    public long _id;

    @Column(indexed = true)
    public long group_id;

    @Column
    public String name;

    @Column
    public String name_ja;

}
