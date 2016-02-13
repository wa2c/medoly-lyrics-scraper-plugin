package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import com.google.gdata.data.spreadsheet.ListEntry;

import java.util.HashMap;

/**
 * Created by wa2c on 2016/02/08.
 */
public class GroupParam {

    public static final String SHEET_NAME = "GROUP";


    private HashMap<String, String> valueMap;

    private String SPREADSHEET_PATH = "https://docs.google.com/spreadsheets/d/1rAtZzpOmwkAigUJ4ENOCglaXPK5klmGfAhGasQrVg6U/edit#gid=1919240742";

//    public static final String ROW_NAMES[] = {
//            "GROUP_ID",
//            "NAME",
//            "NAME_JA",
//    };
//
//    public static final String ROW_KEYS[] = {
//            "groupid",
//            "name",
//            "nameja",
//    };


    /**
     * Group Column.
     */
    public enum GroupColumn {
        /** Group ID */
        GROUP_ID ("GROUP_ID" , "groupid" ),
        /** Group Name */
        NAME     ("NAME"     , "name"     ),
        /** Group Name (ja) */
        NAME_JA  ("NAME_JA"  , "nameja"   );

        /** Constructor. */
        GroupColumn(String name, String key) {
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
    public GroupParam(ListEntry entry) {
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
    public String getValue(GroupColumn column) {
        return valueMap.get(column.getColumnKey());
    }




}
