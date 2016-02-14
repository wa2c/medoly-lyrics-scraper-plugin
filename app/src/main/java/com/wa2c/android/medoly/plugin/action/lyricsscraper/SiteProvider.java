package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.wa2c.android.medoly.utils.Logger;

import java.util.ArrayList;

public class SiteProvider extends ContentProvider {
    /** 再生キューテーブル。 */
    private static final String SITE_TABLE_NAME = "sites";
    /** プレイリストマップテーブル。 */
    private static final String GROUP_TABLE_NAME = "groups";

    /** URIのauthority。 */
    private static final String AUTHORITY = "com.wa2c.android.medoly.plugin.action.lyricsscraper.siteprovider";
    /** コンテンツアクセスURI。 */
    private static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    /** URIマッチング。 */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    /** 再生キューURIコード。 */
    private static final int SITE_URI_CODE = 1;
    /** プレイリストURIコード。 */
    private static final int GROUP_URI_CODE = 2;

    static {
        uriMatcher.addURI(AUTHORITY, SITE_TABLE_NAME, SITE_URI_CODE);
        uriMatcher.addURI(AUTHORITY, GROUP_TABLE_NAME, GROUP_URI_CODE);
    }

    /** 再生キューアクセスURI。 */
    public static final Uri SITE_URI = Uri.withAppendedPath(CONTENT_URI, SITE_TABLE_NAME);
    /** プレイリストアクセスURI。 */
    public static final Uri GROUP_URI = Uri.withAppendedPath(CONTENT_URI, GROUP_TABLE_NAME);

    private static String getTableName(Uri uri) {
        String tableName = null;
        switch (uriMatcher.match(uri)) {
            case SITE_URI_CODE:
                tableName = SITE_TABLE_NAME;
                break;
            case GROUP_URI_CODE:
                tableName = GROUP_TABLE_NAME;
                break;
            default:
                Logger.w("Unsupported uri: '" + uri + "'.");
                break;
        }
        return tableName;
    }

    /** データベースヘルパー。 */
    private DBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = DBHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName))
            return null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            return db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
        } catch (Exception e) {
            Logger.e(e);
            return null;
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName))
            return null;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            long id = db.insertOrThrow(tableName, null, values);
            return Uri.withAppendedPath(uri, String.valueOf(id));
        } catch (Exception e) {
            Logger.e(e);
            return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName))
            return -1;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            if (selection == null && selectionArgs == null) {
                // 全削除の場合はシーケンス番号リセット
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + tableName + "'");
            }
            return db.delete(tableName, selection, selectionArgs);
        } catch (Exception e) {
            Logger.e(e);
            return -1;
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName))
            return -1;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            return db.update(tableName, values, selection, selectionArgs);
        } catch (Exception e) {
            Logger.e(e);
            return -1;
        }
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentProviderResult[] result = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return result;
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int insertCount = 0;
            boolean isFirst = true;
            StringBuilder insertField = new StringBuilder();
            StringBuilder insertParam = new StringBuilder();
            switch (uriMatcher.match(uri)) {
                case SITE_URI_CODE:
                    // コンパイルステートメント作成
                    SiteColumn[] siteColumns = SiteColumn.values();
                    for (SiteColumn col : siteColumns) {
                        if (isFirst) {
                            insertField.append(col.getColumnKey());
                            insertParam.append("?");
                            isFirst = false;
                        } else {
                            insertField.append(",").append(col.getColumnKey());
                            insertParam.append(",?");
                        }
                    }
                    SQLiteStatement siteStatement = db.compileStatement(
                            "INSERT INTO " + SITE_TABLE_NAME + " (" + insertField.toString() + ") VALUES (" + insertParam.toString() + ");");

                    // 挿入
                    for (ContentValues value : values) {
                        siteStatement.clearBindings();
                        int index = 1;
                        for (SiteColumn col : siteColumns) {
                            String val = value.getAsString(col.getColumnKey());
                            if (val != null) siteStatement.bindString(index, val);
                            index++;
                        }
                        if (siteStatement.executeInsert() > 0) {
                            insertCount++;
                        }
                    }

                    db.setTransactionSuccessful();
                    return insertCount;
                case GROUP_URI_CODE:
                    // コンパイルステートメント作成
                    GroupColumn[] groupColumns = GroupColumn.values();
                    for (GroupColumn col : groupColumns) {
                        if (isFirst) {
                            insertField.append(col.getColumnKey());
                            insertParam.append("?");
                            isFirst = false;
                        } else {
                            insertField.append(",").append(col.getColumnKey());
                            insertParam.append(",?");
                        }
                    }
                    SQLiteStatement groupStatement = db.compileStatement(
                            "INSERT INTO " + GROUP_TABLE_NAME + " (" + insertField.toString() + ") VALUES (" + insertParam.toString() + ");");

                    // 挿入
                    for (ContentValues value : values) {
                        groupStatement.clearBindings();
                        int index = 1;
                        for (GroupColumn col : groupColumns) {
                            String val = value.getAsString(col.getColumnKey());
                            if (val != null) groupStatement.bindString(index, val);
                            index++;
                        }
                        if (groupStatement.executeInsert() > 0) {
                            insertCount++;
                        }
                    }

                    db.setTransactionSuccessful();
                    return insertCount;
                default:
                    Logger.w("Unsupported uri: '" + uri + "'.");
                    return -1;
            }
        } catch (Exception e) {
            Logger.e(e);
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * DB Open Helper
     */
    private static class DBHelper extends SQLiteOpenHelper {

        /** データベース名。 */
        private static final String DB_NAME = "medoly_lyricsscraper.db";
        /** データベースバージョン。
         * 1: 初期、再生キューテーブル作成
         */
        private static final int DB_VERSION = 1;
        /** データベースオブジェクト。 */
        private static DBHelper helper = null;

        /**
         * インスタンスを取得。シングルトンパターンを使用する。
         * @param context コンテキスト。
         * @return インスタンス。
         */
        public static synchronized DBHelper getInstance(Context context) {
            if (helper == null) {
                helper = new DBHelper(context);
            }
            return helper;
        }

        /**
         * コンストラクタ。
         * @param context コンテキスト。
         */
        private DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        /**
         * DB作成時。
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            createSiteTable(db);
            createGroupTable(db);
        }

        /**
         * Create site table.
         * @param db  database.
         */
        private void createSiteTable(SQLiteDatabase db) {
            // Make constraint
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE IF NOT EXISTS " + SITE_TABLE_NAME + " (");
            SiteColumn[] columns = SiteColumn.values();
            for (int i = 0; i < columns.length; i++) {
                if (i != 0) builder.append(",");
                builder.append(columns[i].getConstraint());
            }
            builder.append(");");

            // Create table
            db.execSQL(builder.toString());

            // Create index
            db.execSQL("CREATE INDEX IF NOT EXISTS " + SiteColumn.SITE_ID.getColumnKey() + "_idx on " + SITE_TABLE_NAME + "(" + SiteColumn.SITE_ID.getColumnKey() + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS " + SiteColumn.GROUP_ID.getColumnKey() + "_idx on " + SITE_TABLE_NAME + "(" + SiteColumn.GROUP_ID.getColumnKey() + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS " + SiteColumn.SITE_NAME.getColumnKey() + "_idx on " + SITE_TABLE_NAME + "(" + SiteColumn.SITE_NAME.getColumnKey() + ")");
        }

        /**
         * Create group table.
         * @param db database.
         */
        private void createGroupTable(SQLiteDatabase db) {
            // Make constraint
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE IF NOT EXISTS " + GROUP_TABLE_NAME + " (");
            GroupColumn[] columns = GroupColumn.values();
            for (int i = 0; i < columns.length; i++) {
                if (i != 0) builder.append(",");
                builder.append(columns[i].getConstraint());
            }
            builder.append(");");

            // Create table
            db.execSQL(builder.toString());

            // Create index
            db.execSQL("CREATE INDEX IF NOT EXISTS " + GroupColumn.GROUP_ID.getColumnKey() + "_idx on " + GROUP_TABLE_NAME + "(" + GroupColumn.GROUP_ID.getColumnKey() + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS " + GroupColumn.NAME.getColumnKey() + "_idx on " + GROUP_TABLE_NAME + "(" + GroupColumn.NAME.getColumnKey() + ")");
        }

        /**
         * DBアップデート時。
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

    }

}


