package com.wa2c.android.medoly.plugin.action.lyricsscraper.db

import java.util.Locale

/**
 * Group Column.
 */
enum class GroupColumn constructor(val columnName: String, val columnKey: String, private val constraint: String) {
    /** ID.  */
    _ID("_ID", "_id", "INTEGER PRIMARY KEY AUTOINCREMENT"),
    /** Group ID  */
    GROUP_ID("GROUP_ID", "groupid", "INTEGER NOT NULL"),
    /** Group Name  */
    NAME("NAME", "name", "TEXT NOT NULL"),
    /** Group Name (ja)  */
    NAME_JA("NAME_JA", "nameja", "TEXT NOT NULL");

    /** Get column constraint.  */
    fun getConstraint(): String {
        return "$columnKey $constraint"
    }

    companion object {

        fun findLocaleColumn(locale: Locale): GroupColumn {
            val colName = "NAME_" + locale.language.toUpperCase()
            for (c in GroupColumn.values()) {
                if (c.columnName == colName) {
                    return c
                }
            }
            return NAME
        }
    }

}
