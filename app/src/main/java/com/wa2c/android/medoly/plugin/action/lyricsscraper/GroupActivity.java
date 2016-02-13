package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wa2c.android.medoly.utils.Logger;

import java.net.URISyntaxException;
import java.security.acl.Group;

public class GroupActivity extends Activity {

    /** Cursor loader ID. */
    private static final int CURSOR_LOADER_ID = 1;
    /** Cursor type. */
    private static final String CURSOR_TYPE = "cursor_type";
    /** Cursor type: site. */
    private static final int CURSOR_TYPE_SITE = 0;
    /** Cursor type: group. */
    private static final int CURSOR_TYPE_GROUP = 1;
    /** Group ID. */
    private static final String GROUP_ID = "group_id";

    /** List view. */
    private ListView listView;
    /** Cursor Adapter. */
    private SheetCursorAdapter cursorAdapter;
    /** Loader manager. */
    private LoaderManager loaderManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // action bar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // create loader manager
        Bundle bundle = new Bundle();
        bundle.putInt("group_id", 1);
        bundle.putInt(CURSOR_TYPE, CURSOR_TYPE_GROUP);

        // create view
        cursorAdapter = new SheetCursorAdapter(this, CURSOR_TYPE_GROUP);
        listView = (ListView) findViewById(R.id.groupListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (cursorAdapter.getCursorType() == CURSOR_TYPE_GROUP) {
                    SheetCursorAdapter.ListViewHolder holder = (SheetCursorAdapter.ListViewHolder) view.getTag();
                    openSiteList(holder.GroupId);
                }
            }
        });
        listView.setAdapter(cursorAdapter);

        // create load manager
        loaderManager = this.getLoaderManager();
        loaderManager.restartLoader(CURSOR_LOADER_ID, bundle, cursorLoaderCallbacks);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.site_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // home
                if (cursorAdapter.getCursorType() == CURSOR_TYPE_SITE) {
                    openGroupList();
                } else {
                    finish();
                }
                return true;
            case R.id.menu_update_list:
                // update list
                SpreadSheetReadTask task = new SpreadSheetReadTask(getApplicationContext());
                task.setOnPropertyActionListener(new SpreadSheetReadTask.SiteUpdateListener() {
                    @Override
                    public void onListUpdated(boolean isSucceeded) {
                        if (isSucceeded) {
                            openGroupList();
                            AppUtils.showToast(getApplicationContext(), "書き込みかんりょー");
                        }
                    }
                });
                task.execute();
                return true;
            case R.id.menu_open_sheet:
                String sheetUrl = getString(R.string.sheet_uri, getString(R.string.sheet_id));
                Uri sheetUri = Uri.parse(sheetUrl);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, sheetUri);
                startActivity(browserIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        switch (e.getKeyCode()) {
            // back key
            case KeyEvent.KEYCODE_BACK:
                if (cursorAdapter.getCursorType() == CURSOR_TYPE_SITE) {
                    openGroupList();
                    return true;
                }
                break;
        }
        return super.dispatchKeyEvent(e);
    }

    /**
     * Open site list.
     * @param groupId Group ID.
     */
    private void openSiteList(int groupId) {
        cursorAdapter.setCursorType(CURSOR_TYPE_SITE);
        Bundle bundle = new Bundle();
        bundle.putInt(CURSOR_TYPE, CURSOR_TYPE_SITE);
        bundle.putInt(GROUP_ID, groupId);
        loaderManager.restartLoader(CURSOR_LOADER_ID, bundle, cursorLoaderCallbacks);
    }

    /**
     * Open group list.
     */
    private void openGroupList() {
        cursorAdapter.setCursorType(CURSOR_TYPE_GROUP);
        Bundle bundle = new Bundle();
        bundle.putInt(CURSOR_TYPE, CURSOR_TYPE_GROUP);
        loaderManager.restartLoader(CURSOR_LOADER_ID, bundle, cursorLoaderCallbacks);
    }


    /**
     * Cursor loader.
     */
    private final LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (args != null) {
                int type = args.getInt(CURSOR_TYPE, CURSOR_TYPE_GROUP);
                if (type == CURSOR_TYPE_GROUP) {
                    return new CursorLoader(
                            GroupActivity.this,
                            SiteProvider.GROUP_URI,
                            null,
                            null,
                            null,
                            SiteProvider.GroupColumn.NAME.getColumnKey());
                } else if (type == CURSOR_TYPE_SITE) {
                    int groupId = args.getInt(GROUP_ID, -1);
                    return new CursorLoader(
                            GroupActivity.this,
                            SiteProvider.SITE_URI,
                            null,
                            SiteProvider.SiteColumn.GROUP_ID.getColumnKey() + "=?",
                            new String[] { String.valueOf(groupId) },
                            SiteProvider.SiteColumn.SITE_NAME.getColumnKey());
                }
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (loader.isReset() || loader.isAbandoned()) return;
            cursorAdapter.swapCursor(data);
            cursorAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            cursorAdapter.swapCursor(null);
        }
    };


    /**
     * Spreadsheet data cursor adapter.
     */
    private static class SheetCursorAdapter extends CursorAdapter {

        private int cursorType;
        public void setCursorType(int cursorType) {
            this.cursorType = cursorType;
        }
        public int getCursorType() {
            return this.cursorType;
        }

        public SheetCursorAdapter(Activity context, int cursorType) {
            super(context, null, true);
            this.cursorType = cursorType;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = View.inflate(context, android.R.layout.simple_list_item_1, null);
            final ListViewHolder holder = new ListViewHolder();
            holder.TitleTextView = (TextView)view.findViewById(android.R.id.text1);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ListViewHolder holder = (ListViewHolder) view.getTag();
            if (cursorType == CURSOR_TYPE_GROUP) {
                final String title = cursor.getString(cursor.getColumnIndexOrThrow(SiteProvider.GroupColumn.NAME.getColumnKey()));
                holder.GroupId = cursor.getInt(cursor.getColumnIndexOrThrow(SiteProvider.GroupColumn.GROUP_ID.getColumnKey()));
                holder.TitleTextView.setText(title);
            } else {
                final String title = cursor.getString(cursor.getColumnIndexOrThrow(SiteProvider.SiteColumn.SITE_NAME.getColumnKey()));
                holder.GroupId = cursor.getInt(cursor.getColumnIndexOrThrow(SiteProvider.SiteColumn.GROUP_ID.getColumnKey()));
                holder.TitleTextView.setText(title);
            }
        }

        /**
         * ViewHolder for QueueListView.
         */
        private static class ListViewHolder {
            public int GroupId;

            public TextView TitleTextView;
        }
    }

}
