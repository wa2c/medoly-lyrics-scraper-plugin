package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCache;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCacheHelper;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog.CacheDialogFragment;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog.ConfirmDialogFragment;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class CacheActivity extends Activity {

    public static final String INTENT_SEARCH_TITLE = "INTENT_SEARCH_TITLE";
    public static final String INTENT_SEARCH_ARTIST = "INTENT_SEARCH_ARTIST";

    /** Search list adapter. */
    private CacheAdapter cacheAdapter;
    /** Search cache helper. */
    private SearchCacheHelper searchCacheHelper;
    /** SearchCache list. */
    private List<SearchCache> cacheList = new ArrayList<>();


    @BindView(R.id.cacheTitleEditText)
    EditText cacheTitleEditText;
    @BindView(R.id.cacheArtistEditText)
    EditText cacheArtistEditText;
    @BindView(R.id.cacheListView)
    ListView cacheListView;

    private String intentSearchTitle;
    private String intentSearchArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache);
        ButterKnife.bind(this);

        // parameter
        if (getIntent().getExtras() != null) {
            intentSearchTitle = getIntent().getExtras().getString(INTENT_SEARCH_TITLE);
            intentSearchArtist = getIntent().getExtras().getString(INTENT_SEARCH_ARTIST);
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        searchCacheHelper = new SearchCacheHelper(this);
        cacheAdapter = new CacheAdapter(this);
        cacheListView.setAdapter(cacheAdapter);

        cacheTitleEditText.setText(intentSearchTitle);
        cacheArtistEditText.setText(intentSearchArtist);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_cache, menu);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return true;
    }

    /**
     * onOptionsItemSelected event.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.menu_cache_delete:
                if (cacheAdapter.getCheckedSet().size() == 0) {
                    AppUtils.showToast(this, R.string.error_delete_cache_check);
                    return true;
                }

                ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(
                        getString(R.string.message_dialog_cache_delete),
                        getString(R.string.label_confirmation),
                        getString(R.string.label_dialog_cache_delete),
                        null,
                        getString(android.R.string.cancel)
                );
                dialog.setClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            deleteCache(cacheAdapter.getCheckedSet());
                        }
                    }
                });
                dialog.show(this);
                return true;
            case R.id.menu_cache_open_search:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.INTENT_SEARCH_TITLE, cacheTitleEditText.getText().toString());
                intent.putExtra(SearchActivity.INTENT_SEARCH_ARTIST, cacheArtistEditText.getText().toString());
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.cacheInputClearButton)
    void cacheInputClearButtonClick() {
        cacheTitleEditText.setText(null);
        cacheArtistEditText.setText(null);
    }

    @OnClick(R.id.cacheInputSearchButton)
    void cacheInputSearchButtonClick(View view) {
        // Hide keyboard
        InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        String title = cacheTitleEditText.getText().toString();
        String artist = cacheArtistEditText.getText().toString();
        searchCache(title, artist, null);
        cacheTitleEditText.setTag(title);
        cacheArtistEditText.setTag(artist);
    }


    // Search

//    @Background
    void searchCache(String title, String artist, String album) {
        try {
            cacheList = searchCacheHelper.search(title, artist, album);
            showCacheList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    @Background
    void deleteCache(Collection<SearchCache> caches) {
        if (searchCacheHelper.delete(cacheAdapter.getCheckedSet())) {
            cacheList.removeAll(caches);
            AppUtils.showToast(this, R.string.message_delete_cache);
            showCacheList();
        }
    }

//    @UiThread
    void showCacheList() {
        cacheAdapter.clear();
        cacheAdapter.addAll(cacheList);
        cacheAdapter.notifyDataSetChanged();
    }


    // Item

    private SearchCache currentCacheItem;

    @OnItemClick(R.id.cacheListView)
    void cacheListViewItemClick(int position) {
        final SearchCache item = cacheList.get(position);
        CacheDialogFragment dialog = CacheDialogFragment.newInstance(item);
        dialog.setClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    // Save
                    currentCacheItem = item;
                    AppUtils.saveFile(CacheActivity.this, item.title, item.artist);
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    // Research
                    currentCacheItem = item;
                    Intent intent = new Intent(CacheActivity.this, SearchActivity.class);
                    intent.putExtra(SearchActivity.INTENT_SEARCH_TITLE, item.title);
                    intent.putExtra(SearchActivity.INTENT_SEARCH_ARTIST, item.artist);
                    startActivity(intent);
                } else if (which == CacheDialogFragment.DIALOG_RESULT_DELETE_LYRICS) {
                    //searchCache((String)cacheTitleEditText.getTag(), (String)cacheArtistEditText.getTag());
                } else if (which == CacheDialogFragment.DIALOG_RESULT_DELETE_CACHE) {
                    //searchCache((String)cacheTitleEditText.getTag(), (String)cacheArtistEditText.getTag());
                }
            }
        });
        dialog.show(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == AppUtils.REQUEST_CODE_SAVE_FILE) {
            // 歌詞のファイル保存
            if (resultCode == RESULT_OK) {
                Uri uri = resultData.getData();
                OutputStream stream;
                BufferedWriter writer;
                try {
                    stream = getContentResolver().openOutputStream(uri);
                    writer = new BufferedWriter(new OutputStreamWriter(stream,  "UTF-8"));
                    writer.write(currentCacheItem.makeResultItem().getLyrics());
                    writer.flush();
                    AppUtils.showToast(this, R.string.message_lyrics_save_succeeded);

                    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
                    p.edit().putString("pref_prev_file_uri", uri.toString()).apply();
                } catch (IOException e) {
                    Logger.e(e);
                    AppUtils.showToast(this, R.string.message_lyrics_save_failed);
                }
            }
        }

        currentCacheItem = null;

        // Hide keyboard
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodMgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}

/**
 * Search result adapter.
 */
class CacheAdapter extends ArrayAdapter<SearchCache> {

    CacheAdapter(Context context) {
        super(context, R.layout.layout_search_item);
    }

    private HashSet<SearchCache> checkedSet = new HashSet<>();

    /**
     * Get checked item positions.
     * @return Checked position.
     */
    HashSet<SearchCache> getCheckedSet() {
        return checkedSet;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        // view
        final ListItemViewHolder holder;
        if (convertView == null) {
            final View view = View.inflate(parent.getContext(), R.layout.layout_cache_item, null);
            holder = new ListItemViewHolder();
            holder.checkBox = view.findViewById(R.id.cacheItemCheckBox);
            holder.titleTextView  = view.findViewById(R.id.cacheItemTitleTextView);
            holder.artistTextView = view.findViewById(R.id.cacheItemArtistTextView);
            holder.albumTextView = view.findViewById(R.id.cacheItemAlbumTextView);
            holder.hasLyricsImageView = view.findViewById(R.id.cacheItemHasLyricsImageView);
            view.setTag(holder);
            convertView = view;
        } else {
            holder = (ListItemViewHolder) convertView.getTag();
        }

        // data
        final SearchCache item = getItem(position);
        holder.checkBox.setChecked(checkedSet.contains(item));
        holder.titleTextView.setText(getContext().getString(R.string.label_cache_item_title, AppUtils.coalesce(item.title)));
        holder.artistTextView.setText(getContext().getString(R.string.label_cache_item_artist, AppUtils.coalesce(item.artist)));
        holder.albumTextView.setText(getContext().getString(R.string.label_cache_item_album, AppUtils.coalesce(item.album)));
        if (item.has_lyrics == null || !item.has_lyrics) {
            holder.hasLyricsImageView.setVisibility(View.GONE);
        } else {
            holder.hasLyricsImageView.setVisibility(View.VISIBLE);
        }

        // event
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    checkedSet.add(item);
                else
                    checkedSet.remove(item);
            }
        });

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        checkedSet.clear();
    }

    /** List item view holder. */
    private static class ListItemViewHolder {
        CheckBox checkBox;
        TextView titleTextView;
        TextView artistTextView;
        TextView albumTextView;
        ImageView hasLyricsImageView;
    }
}



//
//@EActivity(R.layout.activity_cache)
//@OptionsMenu(R.menu.activity_cache)
//public class CacheActivity extends Activity {
//
//    public static final String INTENT_SEARCH_TITLE = "INTENT_SEARCH_TITLE";
//    public static final String INTENT_SEARCH_ARTIST = "INTENT_SEARCH_ARTIST";
//
//    /** Search list adapter. */
//    private CacheAdapter cacheAdapter;
//    /** Search cache helper. */
//    private SearchCacheHelper searchCacheHelper;
//    /** SearchCache list. */
//    private List<SearchCache> cacheList = new ArrayList<>();
//
//    @Pref
//    AppPrefs_ appPrefs;
//
//    @Extra(INTENT_SEARCH_TITLE)
//    String intentSearchTitle;
//    @Extra(INTENT_SEARCH_ARTIST)
//    String intentSearchArtist;
//
//    @ViewById
//    EditText cacheTitleEditText;
//    @ViewById
//    EditText cacheArtistEditText;
//    @ViewById
//    ListView cacheListView;
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent);
//    }
//
//    @AfterViews
//    void afterViews() {
//        ActionBar actionBar = getActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayShowHomeEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowTitleEnabled(true);
//        }
//
//        searchCacheHelper = new SearchCacheHelper(this);
//        cacheAdapter = new CacheAdapter(this);
//        cacheListView.setAdapter(cacheAdapter);
//
//        cacheTitleEditText.setText(intentSearchTitle);
//        cacheArtistEditText.setText(intentSearchArtist);
//    }
//
//    @OptionsItem(android.R.id.home)
//    void menuHomeClick() {
//        startActivity(new Intent(this, MainActivity_.class));
//    }
//
//    @OptionsItem(R.id.menu_cache_delete)
//    void menuDeleteClick() {
//        if (cacheAdapter.getCheckedSet().size() == 0) {
//            AppUtils.showToast(this, R.string.error_delete_cache_check);
//            return;
//        }
//
//        ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(
//                getString(R.string.message_dialog_cache_delete),
//                getString(R.string.label_confirmation),
//                getString(R.string.label_dialog_cache_delete),
//                null,
//                getString(android.R.string.cancel)
//        );
//        dialog.setClickListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (which == DialogInterface.BUTTON_POSITIVE) {
//                    deleteCache(cacheAdapter.getCheckedSet());
//                }
//            }
//        });
//        dialog.show(this);
//    }
//
//    @OptionsItem(R.id.menu_cache_open_search)
//    void menuOpenCacheClick() {
//        Intent intent = new Intent(this, SearchActivity_.class);
//        intent.putExtra(SearchActivity.INTENT_SEARCH_TITLE, cacheTitleEditText.getText().toString());
//        intent.putExtra(SearchActivity.INTENT_SEARCH_ARTIST, cacheArtistEditText.getText().toString());
//        startActivity(intent);
//    }
//
//    @Background
//    void deleteCache(Collection<SearchCache> caches) {
//        if (searchCacheHelper.delete(cacheAdapter.getCheckedSet())) {
//            cacheList.removeAll(caches);
//            AppUtils.showToast(this, R.string.message_delete_cache);
//            showCacheList();
//        }
//    }
//
//
//    @Click(R.id.cacheInputClearButton)
//    void cacheInputClearButtonClick() {
//        cacheTitleEditText.setText(null);
//        cacheArtistEditText.setText(null);
//    }
//
//    @Click(R.id.cacheInputSearchButton)
//    void cacheInputSearchButtonClick(View view) {
//        // Hide keyboard
//        InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
//        inputMethodMgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//
//        String title = cacheTitleEditText.getText().toString();
//        String artist = cacheArtistEditText.getText().toString();
//        searchCache(title, artist);
//        cacheTitleEditText.setTag(title);
//        cacheArtistEditText.setTag(artist);
//    }
//
//    // Search
//
//    @Background
//    void searchCache(String title, String artist) {
//        try {
//            cacheList = searchCacheHelper.search(title, artist);
//            showCacheList();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @UiThread
//    void showCacheList() {
//        cacheAdapter.clear();
//        cacheAdapter.addAll(cacheList);
//        cacheAdapter.notifyDataSetChanged();
//    }
//
//    // Item
//
//    private SearchCache currentCacheItem;
//
//    @ItemClick(R.id.cacheListView)
//    void cacheListViewItemClick(@NonNull final SearchCache item) {
//        CacheDialogFragment dialog = CacheDialogFragment.newInstance(item);
//        dialog.setClickListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (which == DialogInterface.BUTTON_POSITIVE) {
//                    // Save
//                    currentCacheItem = item;
//                    AppUtils.saveFile(CacheActivity.this, item.title, item.artist);
//                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
//                    // Research
//                    currentCacheItem = item;
//                    Intent intent = new Intent(CacheActivity.this, SearchActivity_.class);
//                    intent.putExtra(SearchActivity.INTENT_SEARCH_TITLE, item.title);
//                    intent.putExtra(SearchActivity.INTENT_SEARCH_ARTIST, item.artist);
//                    startActivity(intent);
//                } else if (which == CacheDialogFragment.DIALOG_RESULT_DELETE_LYRICS) {
//                    searchCache((String)cacheTitleEditText.getTag(), (String)cacheArtistEditText.getTag());
//                } else if (which == CacheDialogFragment.DIALOG_RESULT_DELETE_CACHE) {
//                    searchCache((String)cacheTitleEditText.getTag(), (String)cacheArtistEditText.getTag());
//                }
//            }
//        });
//        dialog.show(this);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//        if (requestCode == AppUtils.REQUEST_CODE_SAVE_FILE) {
//            // 歌詞のファイル保存
//            if (resultCode == RESULT_OK) {
//                Uri uri = resultData.getData();
//                try (OutputStream stream = getContentResolver().openOutputStream(uri);
//                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream,  "UTF-8"))) {
//                    writer.write(currentCacheItem.makeResultItem().getLyrics());
//                    writer.flush();
//                    AppUtils.showToast(this, R.string.message_lyrics_save_succeeded);
//
//
//
//                    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
//                    p.edit().putString("pref_prev_file_uri", uri.toString()).apply();
//                } catch (IOException e) {
//                    Logger.e(e);
//                    AppUtils.showToast(this, R.string.message_lyrics_save_failed);
//                }
//            }
//        }
//
//        currentCacheItem = null;
//
//        // Hide keyboard
//        if (getCurrentFocus() != null) {
//            InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            inputMethodMgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//        }
//    }
//
//
//    /**
//     * Search result adapter.
//     */
//    private static class CacheAdapter extends ArrayAdapter<SearchCache> {
//
//        CacheAdapter(Context context) {
//            super(context, R.layout.layout_search_item);
//        }
//
//        private HashSet<SearchCache> checkedSet = new HashSet<>();
//
//        /**
//         * Get checked item positions.
//         * @return Checked position.
//         */
//        HashSet<SearchCache> getCheckedSet() {
//            return checkedSet;
//        }
//
//        @NonNull
//        @Override
//        public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
//            // view
//            final ListItemViewHolder holder;
//            if (convertView == null) {
//                final View view = View.inflate(parent.getContext(), R.layout.layout_cache_item, null);
//                holder = new ListItemViewHolder();
//                holder.checkBox = (CheckBox)view.findViewById(R.id.cacheItemCheckBox);
//                holder.titleTextView  = (TextView)view.findViewById(R.id.cacheItemTitleTextView);
//                holder.artistTextView = (TextView)view.findViewById(R.id.cacheItemArtistTextView);
//                holder.fromTextView  = (TextView)view.findViewById(R.id.cacheItemFromTextView);
//                holder.fileTextView = (TextView)view.findViewById(R.id.cacheItemFileTextView);
//                holder.langTextView = (TextView)view.findViewById(R.id.cacheItemLangTextView);
//                holder.hasLyricsImageView = (ImageView)view.findViewById(R.id.cacheItemHasLyricsImageView);
//                view.setTag(holder);
//                convertView = view;
//            } else {
//                holder = (ListItemViewHolder) convertView.getTag();
//            }
//
//            // data
//            final SearchCache item = getItem(position);
//            holder.checkBox.setChecked(checkedSet.contains(item));
//            holder.titleTextView.setText(getContext().getString(R.string.label_cache_item_title, AppUtils.coalesce(item.title)));
//            holder.artistTextView.setText(getContext().getString(R.string.label_cache_item_artist, AppUtils.coalesce(item.artist)));
//            holder.fromTextView.setText(getContext().getString(R.string.label_cache_item_from, AppUtils.coalesce(item.from)));
//            holder.fileTextView.setText(getContext().getString(R.string.label_cache_item_file, AppUtils.coalesce(item.file_name)));
//            holder.langTextView.setText(getContext().getString(R.string.label_cache_item_lang, AppUtils.coalesce(item.language)));
//            if (item.has_lyrics == null || !item.has_lyrics) {
//                holder.hasLyricsImageView.setVisibility(View.GONE);
//            } else {
//                holder.hasLyricsImageView.setVisibility(View.VISIBLE);
//            }
//
//            // event
//            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (isChecked)
//                        checkedSet.add(item);
//                    else
//                        checkedSet.remove(item);
//                }
//            });
//
//            return convertView;
//        }
//
//        @Override
//        public void notifyDataSetChanged() {
//            super.notifyDataSetChanged();
//            checkedSet.clear();
//        }
//
//    }
//
//    /** List item view holder. */
//    private static class ListItemViewHolder {
//        CheckBox checkBox;
//        TextView titleTextView;
//        TextView artistTextView;
//        TextView fromTextView;
//        TextView fileTextView;
//        TextView langTextView;
//        ImageView hasLyricsImageView;
//    }
//
//
//}
