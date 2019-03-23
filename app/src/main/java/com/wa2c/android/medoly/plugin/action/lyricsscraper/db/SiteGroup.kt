package com.wa2c.android.medoly.plugin.action.lyricsscraper.db

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Setter
import com.github.gfx.android.orma.annotation.Table
import java.io.Serializable
import java.util.*

/**
 * Site group. (Orma Model)
 */
@Table(value = "site_group")
data class SiteGroup (
        @Setter @PrimaryKey(autoincrement = true) var _id: Long,
        @Setter @Column(indexed = true) var group_id: Long,
        @Setter @Column(indexed = true) var name: String?,
        @Setter @Column var name_ja: String?
): Serializable {

    /**
     * Get locale name;
     * @param localeParam A locale. Default if null.
     * @return A name;
     */
    fun findLocaleName(localeParam: Locale?): String? {
        var locale = localeParam
        if (locale == null)
            locale = Locale.getDefault()

        val lang = locale!!.language
        try {
            if (lang == "en")
                return name
            val field = this.javaClass.getField("name_$lang")
            return field.get(this).toString()
        } catch (e: Exception) {
            return name
        }
    }

}
