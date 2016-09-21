package com.wa2c.android.medoly.plugin.action.lyricsscraper;

/**
 * Group Column.
 */
public enum GroupColumn {
    /** ID. */
    _ID      ( "_ID"     , "_id"     , "INTEGER PRIMARY KEY AUTOINCREMENT" ),
    /** Group ID */
    GROUP_ID ("GROUP_ID" , "groupid" , "INTEGER NOT NULL" ),
    /** Group Name */
    NAME     ("NAME"     , "name"    , "TEXT NOT NULL" ),
    /** Group Name (ja) */
    NAME_JA  ("NAME_JA"  , "nameja"  , "TEXT NOT NULL" );

    /** Constructor. */
    GroupColumn(String name, String key, String constraint) {
        this.columnName = name;
        this.columnKey = key;
        this.constraint = constraint;
    }

    /** Column name. */
    private String columnName;
    /** Column key. */
    private String columnKey;
    /** Column constraint. */
    private String constraint;

    /** Get column name. */
    public String getColumnName() {
        return this.columnName;
    }
    /** Get column key. */
    public String getColumnKey() {
        return this.columnKey;
    }
    /** Get collumn constraint. */
    public String getConstraint() {
        return columnKey + " " + constraint;
    }
}
