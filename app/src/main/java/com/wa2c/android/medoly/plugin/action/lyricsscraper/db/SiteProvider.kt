package com.wa2c.android.medoly.plugin.action.lyricsscraper.db

import android.content.ContentProvider
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentValues
import android.content.Context
import android.content.OperationApplicationException
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import android.net.Uri
import android.text.TextUtils

import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger

import java.util.ArrayList

class SiteProvider : ContentProvider() {

    /** データベースヘルパー。  */
    private var dbHelper: DBHelper? = null

    override fun onCreate(): Boolean {
        dbHelper = DBHelper.getInstance(context)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val tableName = getTableName(uri)
        if (tableName.isNullOrEmpty())
            return null

        val db = dbHelper!!.readableDatabase
        return try {
            db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder)
        } catch (e: Exception) {
            Logger.e(e)
            null
        }

    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val tableName = getTableName(uri)
        if (tableName.isNullOrEmpty())
            return null

        val db = dbHelper!!.writableDatabase
        try {
            val id = db.insertOrThrow(tableName, null, values)
            return Uri.withAppendedPath(uri, id.toString())
        } catch (e: Exception) {
            Logger.e(e)
            return null
        }

    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val tableName = getTableName(uri)
        if (tableName.isNullOrEmpty())
            return -1

        val db = dbHelper!!.writableDatabase
        return try {
            if (selection == null && selectionArgs == null) {
                // 全削除の場合はシーケンス番号リセット
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '$tableName'")
            }
            db.delete(tableName, selection, selectionArgs)
        } catch (e: Exception) {
            Logger.e(e)
            -1
        }

    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val tableName = getTableName(uri)
        if (tableName.isNullOrEmpty())
            return -1

        val db = dbHelper!!.writableDatabase
        return try {
            db.update(tableName, values, selection, selectionArgs)
        } catch (e: Exception) {
            Logger.e(e)
            -1
        }

    }

    @Throws(OperationApplicationException::class)
    override fun applyBatch(operations: ArrayList<ContentProviderOperation>): Array<ContentProviderResult?> {
        val db = dbHelper!!.writableDatabase
        db.beginTransaction()
        var result = arrayOfNulls<ContentProviderResult>(0)
        try {
            result = super.applyBatch(operations)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Logger.e(e)
        }

        db.endTransaction()
        return result
    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        val db = dbHelper!!.writableDatabase
        db.beginTransaction()

        try {
            var insertCount = 0
            var isFirst = true
            val insertField = StringBuilder()
            val insertParam = StringBuilder()
            when (uriMatcher.match(uri)) {
                SITE_URI_CODE -> {
                    // コンパイルステートメント作成
                    val siteColumns = SiteColumn.values()
                    for (col in siteColumns) {
                        if (isFirst) {
                            insertField.append(col.columnKey)
                            insertParam.append("?")
                            isFirst = false
                        } else {
                            insertField.append(",").append(col.columnKey)
                            insertParam.append(",?")
                        }
                    }
                    val siteStatement = db.compileStatement(
                            "INSERT INTO " + SITE_TABLE_NAME + " (" + insertField.toString() + ") VALUES (" + insertParam.toString() + ");")

                    // 挿入
                    for (value in values) {
                        siteStatement.clearBindings()
                        var index = 1
                        for (col in siteColumns) {
                            val `val` = value.getAsString(col.columnKey)
                            if (`val` != null) siteStatement.bindString(index, `val`)
                            index++
                        }
                        if (siteStatement.executeInsert() > 0) {
                            insertCount++
                        }
                    }

                    db.setTransactionSuccessful()
                    return insertCount
                }
                GROUP_URI_CODE -> {
                    // コンパイルステートメント作成
                    val groupColumns = GroupColumn.values()
                    for (col in groupColumns) {
                        if (isFirst) {
                            insertField.append(col.columnKey)
                            insertParam.append("?")
                            isFirst = false
                        } else {
                            insertField.append(",").append(col.columnKey)
                            insertParam.append(",?")
                        }
                    }
                    val groupStatement = db.compileStatement(
                            "INSERT INTO " + GROUP_TABLE_NAME + " (" + insertField.toString() + ") VALUES (" + insertParam.toString() + ");")

                    // 挿入
                    for (value in values) {
                        groupStatement.clearBindings()
                        var index = 1
                        for (col in groupColumns) {
                            val `val` = value.getAsString(col.columnKey)
                            if (`val` != null) groupStatement.bindString(index, `val`)
                            index++
                        }
                        if (groupStatement.executeInsert() > 0) {
                            insertCount++
                        }
                    }

                    db.setTransactionSuccessful()
                    return insertCount
                }
                else -> {
                    Logger.w("Unsupported uri: '$uri'.")
                    return -1
                }
            }
        } catch (e: Exception) {
            Logger.e(e)
            return -1
        } finally {
            db.endTransaction()
        }
    }

    /**
     * DB Open Helper
     */
    private class DBHelper private constructor(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        /**
         * DB作成時。
         */
        override fun onCreate(db: SQLiteDatabase) {
            createSiteTable(db)
            createGroupTable(db)
        }

        /**
         * Create site table.
         * @param db  database.
         */
        private fun createSiteTable(db: SQLiteDatabase) {
            // Make constraint
            val builder = StringBuilder()
            builder.append("CREATE TABLE IF NOT EXISTS $SITE_TABLE_NAME (")
            val columns = SiteColumn.values()
            for (i in columns.indices) {
                if (i != 0) builder.append(",")
                builder.append(columns[i].getConstraint())
            }
            builder.append(");")

            // Create table
            db.execSQL(builder.toString())

            // Create index
            db.execSQL("CREATE INDEX IF NOT EXISTS " + SiteColumn.SITE_ID.columnKey + "_idx on " + SITE_TABLE_NAME + "(" + SiteColumn.SITE_ID.columnKey + ")")
            db.execSQL("CREATE INDEX IF NOT EXISTS " + SiteColumn.GROUP_ID.columnKey + "_idx on " + SITE_TABLE_NAME + "(" + SiteColumn.GROUP_ID.columnKey + ")")
            db.execSQL("CREATE INDEX IF NOT EXISTS " + SiteColumn.SITE_NAME.columnKey + "_idx on " + SITE_TABLE_NAME + "(" + SiteColumn.SITE_NAME.columnKey + ")")
        }

        /**
         * Create group table.
         * @param db database.
         */
        private fun createGroupTable(db: SQLiteDatabase) {
            // Make constraint
            val builder = StringBuilder()
            builder.append("CREATE TABLE IF NOT EXISTS $GROUP_TABLE_NAME (")
            val columns = GroupColumn.values()
            for (i in columns.indices) {
                if (i != 0) builder.append(",")
                builder.append(columns[i].getConstraint())
            }
            builder.append(");")

            // Create table
            db.execSQL(builder.toString())

            // Create index
            db.execSQL("CREATE INDEX IF NOT EXISTS " + GroupColumn.GROUP_ID.columnKey + "_idx on " + GROUP_TABLE_NAME + "(" + GroupColumn.GROUP_ID.columnKey + ")")
            db.execSQL("CREATE INDEX IF NOT EXISTS " + GroupColumn.NAME.columnKey + "_idx on " + GROUP_TABLE_NAME + "(" + GroupColumn.NAME.columnKey + ")")
        }

        /**
         * DBアップデート時。
         */
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

        companion object {

            /** データベース名。  */
            private const val DB_NAME = "medoly_lyricsscraper.db"
            /** データベースバージョン。
             * 1: 初期、再生キューテーブル作成
             */
            private const val DB_VERSION = 1
            /** データベースオブジェクト。  */
            private var helper: DBHelper? = null

            /**
             * インスタンスを取得。シングルトンパターンを使用する。
             * @param context コンテキスト。
             * @return インスタンス。
             */
            @Synchronized
            fun getInstance(context: Context): DBHelper {
                if (helper == null) {
                    helper = DBHelper(context)
                }
                return helper!!
            }
        }

    }

    companion object {
        /** 再生キューテーブル。  */
        private const val SITE_TABLE_NAME = "sites"
        /** プレイリストマップテーブル。  */
        private const val GROUP_TABLE_NAME = "groups"

        /** URIのauthority。  */
        private const val AUTHORITY = "com.wa2c.android.medoly.plugin.action.lyricsscraper.siteprovider"
        /** コンテンツアクセスURI。  */
        private val CONTENT_URI = Uri.parse("content://" + AUTHORITY)

        /** URIマッチング。  */
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        /** 再生キューURIコード。  */
        private const val SITE_URI_CODE = 1
        /** プレイリストURIコード。  */
        private const val GROUP_URI_CODE = 2

        init {
            uriMatcher.addURI(AUTHORITY, SITE_TABLE_NAME, SITE_URI_CODE)
            uriMatcher.addURI(AUTHORITY, GROUP_TABLE_NAME, GROUP_URI_CODE)
        }

        /** 再生キューアクセスURI。  */
        val SITE_URI = Uri.withAppendedPath(CONTENT_URI, SITE_TABLE_NAME)!!
        /** プレイリストアクセスURI。  */
        val GROUP_URI = Uri.withAppendedPath(CONTENT_URI, GROUP_TABLE_NAME)!!

        private fun getTableName(uri: Uri): String? {
            var tableName: String? = null
            when (uriMatcher.match(uri)) {
                SITE_URI_CODE -> tableName = SITE_TABLE_NAME
                GROUP_URI_CODE -> tableName = GROUP_TABLE_NAME
                else -> Logger.w("Unsupported uri: '$uri'.")
            }
            return tableName
        }
    }

}


