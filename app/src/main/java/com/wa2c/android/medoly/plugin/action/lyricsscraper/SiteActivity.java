package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;



public class SiteActivity extends Activity {

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

    /** Preferences. */
    private SharedPreferences preference;
    /** Cursor Adapter. */
    private SheetCursorAdapter cursorAdapter;
    /** Loader manager. */
    private LoaderManager loaderManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.preference = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_group);

        // action bar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // create loader manager
        Bundle bundle = new Bundle();
        bundle.putInt(CURSOR_TYPE, CURSOR_TYPE_GROUP);

        // create view
        cursorAdapter = new SheetCursorAdapter(this, CURSOR_TYPE_GROUP);
        ListView listView = (ListView) findViewById(R.id.groupListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SheetCursorAdapter.ListViewHolder holder = (SheetCursorAdapter.ListViewHolder) view.getTag();
                if (cursorAdapter.getCursorType() == CURSOR_TYPE_GROUP) {
                    openSiteList(holder.GroupId);
                } else if (cursorAdapter.getCursorType() == CURSOR_TYPE_SITE) {
                    preference.edit().putString(getString(R.string.prefkey_selected_site_id), holder.SiteId).apply();
                    cursorAdapter.notifyDataSetChanged();
                }
            }
        });
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
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
                            AppUtils.showToast(getApplicationContext(), R.string.message_renew_list_succeeded);
                        } else {
                            AppUtils.showToast(getApplicationContext(), R.string.message_renew_list_failed);
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
    private void openSiteList(String groupId) {
        cursorAdapter.setCursorType(CURSOR_TYPE_SITE);
        Bundle bundle = new Bundle();
        bundle.putInt(CURSOR_TYPE, CURSOR_TYPE_SITE);
        bundle.putString(GROUP_ID, groupId);
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
                            SiteActivity.this,
                            SiteProvider.GROUP_URI,
                            null,
                            null,
                            null,
                            GroupColumn.NAME.getColumnKey());
                } else if (type == CURSOR_TYPE_SITE) {
                    String groupId = args.getString(GROUP_ID, "-1");
                    return new CursorLoader(
                            SiteActivity.this,
                            SiteProvider.SITE_URI,
                            null,
                            SiteColumn.GROUP_ID.getColumnKey() + "=?",
                            new String[] { groupId },
                            SiteColumn.SITE_NAME.getColumnKey());
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

        /** Cursor type. */
        private int cursorType;
        /** Set cursor type. */
        public void setCursorType(int cursorType) {
            this.cursorType = cursorType;
        }
        /** Get cursor type. */
        public int getCursorType() {
            return this.cursorType;
        }

        private SharedPreferences preference;

        public SheetCursorAdapter(Activity context, int cursorType) {
            super(context, null, true);
            this.cursorType = cursorType;
            this.preference = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @Override
        public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
            final View view = View.inflate(context, R.layout.layout_site_item, null);
            final ListViewHolder holder = new ListViewHolder();
            holder.SelectRadioButton = (RadioButton)view.findViewById(R.id.siteSelectRadioButton);
            holder.TitleTextView = (TextView)view.findViewById(R.id.siteParamTitleTextView);
            holder.UriTextView = (TextView)view.findViewById(R.id.siteParamUriTextView);
            holder.LaunchImageButton = (ImageButton)view.findViewById(R.id.siteLaunchImageButton);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            final ListViewHolder holder = (ListViewHolder) view.getTag();
            if (cursorType == CURSOR_TYPE_GROUP) {
                holder.SelectRadioButton.setVisibility(View.GONE);
                holder.LaunchImageButton.setVisibility(View.GONE);

                String title = cursor.getString(cursor.getColumnIndexOrThrow(GroupColumn.NAME.getColumnKey()));
                holder.GroupId = cursor.getString(cursor.getColumnIndexOrThrow(GroupColumn.GROUP_ID.getColumnKey()));
                holder.TitleTextView.setText(title);
                holder.UriTextView.setVisibility(View.GONE);

            } else {
                // ID
                holder.GroupId = cursor.getString(cursor.getColumnIndexOrThrow(SiteColumn.GROUP_ID.getColumnKey()));
                holder.SiteId = cursor.getString(cursor.getColumnIndexOrThrow(SiteColumn.SITE_ID.getColumnKey()));

                // select
                holder.SelectRadioButton.setVisibility(View.VISIBLE);
                if (holder.SiteId.equals(preference.getString(context.getString(R.string.prefkey_selected_site_id), "-1"))) {
                    holder.SelectRadioButton.setChecked(true);
                } else {
                    holder.SelectRadioButton.setChecked(false);
                }
                holder.SelectRadioButton.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return view.onTouchEvent(event);
                    }
                });

                // title
                String title = cursor.getString(cursor.getColumnIndexOrThrow(SiteColumn.SITE_NAME.getColumnKey()));
                holder.TitleTextView.setText(title);
                holder.TitleTextView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return view.onTouchEvent(event);
                    }
                });

                // uri
                String uri = cursor.getString(cursor.getColumnIndexOrThrow(SiteColumn.SITE_URI.getColumnKey()));
                holder.UriTextView.setVisibility(View.VISIBLE);
                holder.UriTextView.setText(uri);
                holder.LaunchImageButton.setTag(uri);
                holder.LaunchImageButton.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(uri)) {
                    holder.LaunchImageButton.setEnabled(true);
                    holder.LaunchImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri sheetUri = Uri.parse((String) v.getTag());
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, sheetUri);
                            context.startActivity(browserIntent);
                        }
                    });
                } else {
                    holder.LaunchImageButton.setEnabled(false);
                }
            }
        }

        /**
         * ViewHolder for QueueListView.
         */
        private static class ListViewHolder {
            public String GroupId;
            public String SiteId;

            public RadioButton SelectRadioButton;
            public TextView TitleTextView;
            public TextView UriTextView;
            public ImageButton LaunchImageButton;
        }
    }

}
