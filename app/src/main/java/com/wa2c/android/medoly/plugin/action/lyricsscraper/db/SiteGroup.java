package com.wa2c.android.medoly.plugin.action.lyricsscraper.db;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

/**
 * Site group. (Orma Model)
 */
@Table(value = "site_group")
public class SiteGroup implements Serializable {

    @PrimaryKey(autoincrement = true)
    public long _id;

    @Column(indexed = true)
    public long group_id;

    @Column(indexed = true)
    public String name;

    @Column
    public String name_ja;


    /**
     * Get locale name;
     * @param locale A locale. Default if null.
     * @return A name;
     */
    public String findLocaleName(Locale locale) {
        if (locale == null)
            locale = Locale.getDefault();

        String lang = locale.getLanguage();
        try {
            if (lang.equals("en"))
                return name;
            Field field = this.getClass().getField("name_" + lang);
            return field.get(this).toString();
        } catch (Exception e) {
            return name;
        }
    }

}
