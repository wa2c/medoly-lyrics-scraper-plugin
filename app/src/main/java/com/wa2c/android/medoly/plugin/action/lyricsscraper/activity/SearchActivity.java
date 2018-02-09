package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.wa2c.android.medoly.library.MediaProperty;
import com.wa2c.android.medoly.library.PropertyData;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCacheHelper;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog.ConfirmDialogFragment;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog.NormalizeDialogFragment;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotFoundException;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotSelectException;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.LyricsSearcherWebView;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.ResultItem;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;


/**
 * Search Activity.
 */
public class SearchActivity extends Activity {

    public static final String INTENT_SEARCH_TITLE = "INTENT_SEARCH_TITLE";
    public static final String INTENT_SEARCH_ARTIST = "INTENT_SEARCH_ARTIST";

    @BindView(R.id.searchTitleButton)
    Button searchTitleButton;
    @BindView(R.id.searchTitleEditText)
    EditText searchTitleEditText;
    @BindView(R.id.searchArtistButton)
    Button searchArtistButton;
    @BindView(R.id.searchArtistEditText)
    EditText searchArtistEditText;
    @BindView(R.id.searchClearButton)
    ImageButton searchClearButton;
    @BindView(R.id.searchStartButton)
    ImageButton searchStartButton;
    @BindView(R.id.searchResultListView)
    ListView searchResultListView;
    @BindView(R.id.searchLyricsScrollView)
    ScrollView searchLyricsScrollView;
    @BindView(R.id.searchLyricsTextView)
    TextView searchLyricsTextView;
    @BindView(R.id.searchResultLoadingLayout)
    View searchResultLoadingLayout;
    @BindView(R.id.searchLyricsLoadingLayout)
    View searchLyricsLoadingLayout;

    private Handler handler = new Handler();
    private LyricsSearcherWebView webView;
    private String intentSearchTitle = null;
    private String intentSearchArtist = null;

    /** Search list adapter. */
    private SearchResultAdapter searchResultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        // parameter
        if (getIntent().getExtras() != null) {
            intentSearchTitle = getIntent().getExtras().getString(INTENT_SEARCH_TITLE);
            intentSearchArtist = getIntent().getExtras().getString(INTENT_SEARCH_ARTIST);
        }

        // action bar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        searchResultAdapter = new SearchResultAdapter(this);
        searchResultListView.setAdapter(searchResultAdapter);

        searchTitleEditText.setText(intentSearchTitle);
        searchArtistEditText.setText(intentSearchArtist);

        // adjust size
        searchLyricsScrollView.post(new Runnable() {
            @Override
            public void run() {
                // adjust height
                int heightResult = searchResultListView.getMeasuredHeight();
                int heightLyrics = searchLyricsScrollView.getMeasuredHeight();
                int heightSum = heightResult + heightLyrics;
                if (heightResult == 0)
                    return;
                int height = getResources().getDimensionPixelSize(R.dimen.search_result_height);
                if (heightSum < height * 2) {
                    ViewGroup.LayoutParams params = searchResultListView.getLayoutParams();
                    params.height = heightSum / 2;
                    searchResultListView.setLayoutParams(params);
                }
            }
        });

        // Set web view
        webView = new LyricsSearcherWebView(this);
        webView.setOnHandleListener(new LyricsSearcherWebView.HandleListener() {
            @Override
            public void onSearchResult(List<ResultItem> list) {
                showSearchResult(list);
                showLyrics(null);
//                    if (list == null) {
//                        Logger.d("■");
//                    }
            }

            @Override
            public void onGetLyrics(String lyrics) {
                showLyrics(lyrics);
            }

            @Override
            public void onError(String message) {
                Logger.d(message);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search, menu);
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
            case R.id.menu_search_save_file:
                if (!existsLyrics()) {
                    AppUtils.showToast(this, R.string.error_exists_lyrics);
                    return true;
                }

                String title = searchTitleEditText.getText().toString();
                String artist = searchArtistEditText.getText().toString();
                AppUtils.saveFile(this, title, artist);
                return true;
            case R.id.menu_search_save_cache:

                if (!existsLyrics()) {
                    AppUtils.showToast(this, R.string.error_exists_lyrics);
                    return true;
                }

                ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(
                        getString(R.string.message_dialog_confirm_save_cache),
                        getString(R.string.label_confirmation),
                        getString(R.string.label_dialog_confirm_save_cache),
                        null,
                        getString(android.R.string.cancel)
                );
                dialog.setClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            String title = searchTitleEditText.getText().toString();
                            String artist = searchArtistEditText.getText().toString();
                            saveToCacheBackground(title, artist, searchResultAdapter.getSelectedItem());
                        }
                    }
                });
                dialog.show(this);

                return true;
            case R.id.menu_search_open_cache:
                Intent intent = new Intent(this, CacheActivity.class);
                intent.putExtra(CacheActivity.INTENT_SEARCH_TITLE, searchTitleEditText.getText().toString());
                intent.putExtra(CacheActivity.INTENT_SEARCH_ARTIST, searchArtistEditText.getText().toString());
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void saveToCacheBackground(final String title, final String artist, final ResultItem item) {
        final SearchCacheHelper searchCacheHelper = new SearchCacheHelper(this);
        HandlerThread saveHandlerThread = new HandlerThread("saveThreadHandler");
        saveHandlerThread.start();
        Handler saveHandler = new Handler(saveHandlerThread.getLooper());
        saveHandler.post(new Runnable() {
            public void run() {
                if (searchCacheHelper.insertOrUpdate(title, artist, null, item))
                    AppUtils.showToast(getApplicationContext(), R.string.message_save_cache);
            }
        });
    }



    @OnClick(R.id.searchTitleButton)
    void searchTitleButtonClick(View view) {
        final NormalizeDialogFragment dialogFragment = NormalizeDialogFragment.newInstance(searchTitleEditText.getText().toString(), intentSearchTitle);
        dialogFragment.setClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    searchTitleEditText.setText(dialogFragment.getInputText());
                }
            }
        });
        dialogFragment.show(this);
    }

    @OnClick(R.id.searchArtistButton)
    void searchArtistButtonClick(View view) {
        final NormalizeDialogFragment dialogFragment = NormalizeDialogFragment.newInstance(searchArtistEditText.getText().toString(), intentSearchArtist);
        dialogFragment.setClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    searchArtistEditText.setText(dialogFragment.getInputText());
                }
            }
        });
        dialogFragment.show(this);
    }

    @OnClick(R.id.searchClearButton)
    void searchClearButtonClick(View view) {
        searchTitleEditText.setText(null);
        searchArtistEditText.setText(null);
    }

    @OnClick(R.id.searchStartButton)
    void searchStartButtonClick(View view) {
        // Hide keyboard
        InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        String title = searchTitleEditText.getText().toString();
        String artist = searchArtistEditText.getText().toString();
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(artist)) {
            AppUtils.showToast(this, R.string.error_input_condition);
            return;
        }


        try {
            // Clear view
            showSearchResult(null);
            showLyrics(null);
            searchResultAdapter.setSelectedItem(null);

            PropertyData propertyData = new PropertyData();
            propertyData.put(MediaProperty.TITLE, title);
            propertyData.put(MediaProperty.ARTIST, artist);
            webView.search(propertyData);
            searchResultListView.setVisibility(View.INVISIBLE);
            searchResultLoadingLayout.setVisibility(View.VISIBLE);
        } catch (SiteNotSelectException e) {
            AppUtils.showToast(this, R.string.message_no_select_site);
        } catch (SiteNotFoundException e) {
            AppUtils.showToast(this, R.string.message_no_site);
        } catch (Exception e) {
            Logger.e(e);
        }

    }

    @OnItemClick(R.id.searchResultListView)
    void searchResultListViewClick(int position) {
        ResultItem item = searchResultAdapter.getItem(position);
        if (item == null)
            return;

        try {
            webView.download(item.getPageUrl());
            searchResultAdapter.setSelectedItem(item);
            searchLyricsScrollView.setVisibility(View.INVISIBLE);
            searchLyricsLoadingLayout.setVisibility(View.VISIBLE);
        } catch (SiteNotSelectException e) {
            AppUtils.showToast(this, R.string.message_no_select_site);
        } catch (SiteNotFoundException e) {
            AppUtils.showToast(this, R.string.message_no_site);
        } catch (Exception e) {
            Logger.e(e);
        }

    }

    void showSearchResult(List<ResultItem> itemList) {
        try {
            searchResultAdapter.clear();
            if (itemList != null)
                searchResultAdapter.addAll(itemList);
            searchResultAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            searchResultListView.setVisibility(View.VISIBLE);
            searchResultLoadingLayout.setVisibility(View.INVISIBLE);
        }
    }

    void showLyrics(String lyrics) {
        ResultItem item = searchResultAdapter.getSelectedItem();
        if (item == null)
            return;
        item.setLyrics(lyrics);

        searchLyricsTextView.setText(lyrics);
        searchResultAdapter.notifyDataSetChanged();
        searchLyricsScrollView.setVisibility(View.VISIBLE);
        searchLyricsLoadingLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * Check existence of lyrics
     * @return true if exists lyrics.
     */
    private synchronized boolean existsLyrics() {
        return (searchResultAdapter.getSelectedItem() != null) && !TextUtils.isEmpty(searchResultAdapter.getSelectedItem().getPageUrl());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == AppUtils.REQUEST_CODE_SAVE_FILE) {
            // 歌詞のファイル保存
            if (resultCode == RESULT_OK) {
                if (!existsLyrics()) {
                    AppUtils.showToast(this, R.string.error_exists_lyrics);
                    return;
                }

                Uri uri = resultData.getData();
                OutputStream stream = null;
                BufferedWriter writer = null;
                try {
                    stream = getContentResolver().openOutputStream(uri);
                    writer = new BufferedWriter(new OutputStreamWriter(stream,  "UTF-8"));
                    writer.write(searchResultAdapter.getSelectedItem().getLyrics());
                    writer.flush();
                    AppUtils.showToast(this, R.string.message_lyrics_save_succeeded);
                } catch (Exception e) {
                    Logger.e(e);
                    AppUtils.showToast(this, R.string.message_lyrics_save_failed);
                } finally {
                    if (writer != null)
                        try { writer.close(); } catch (Exception ignore) { }
                    if (stream != null)
                        try { stream.close(); } catch (Exception ignore) { }
                }
            }
        }

        // Hide keyboard
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodMgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }



//    public static final String INTENT_SEARCH_TITLE = "INTENT_SEARCH_TITLE";
//    public static final String INTENT_SEARCH_ARTIST = "INTENT_SEARCH_ARTIST";

//    /** Search list adapter. */
//    private SearchResultAdapter searchResultAdapter;
//    /** Search cache helper. */
//    private SearchCacheHelper searchCacheHelper;
//
//    @Extra(INTENT_SEARCH_TITLE)
//    String intentSearchTitle;
//    @Extra(INTENT_SEARCH_ARTIST)
//    String intentSearchArtist;
//
//    @ViewById
//    Button searchTitleButton;
//    @ViewById
//    EditText searchTitleEditText;
//    @ViewById
//    Button searchArtistButton;
//    @ViewById
//    EditText searchArtistEditText;
//    @ViewById
//    ImageButton searchClearButton;
//    @ViewById
//    ImageButton searchStartButton;
//    @ViewById
//    ListView searchResultListView;
//    @ViewById
//    ScrollView searchLyricsScrollView;
//    @ViewById
//    TextView searchLyricsTextView;
//
//    @ViewById
//    View searchResultLoadingLayout;
//    @ViewById
//    View searchLyricsLoadingLayout;
//
//    @DimensionPixelSizeRes
//    int search_result_height;
//
//
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent);
//
//        searchResultListView.setAdapter(searchResultAdapter);
//        searchTitleEditText.setText(intentSearchTitle);
//        searchArtistEditText.setText(intentSearchArtist);
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
//        searchResultAdapter = new SearchResultAdapter(this);
//        searchResultListView.setAdapter(searchResultAdapter);
//
//        searchTitleEditText.setText(intentSearchTitle);
//        searchArtistEditText.setText(intentSearchArtist);
//
//        // adjust size
//        searchLyricsScrollView.post(new Runnable() {
//            @Override
//            public void run() {
//                // adjust height
//                int heightResult = searchResultListView.getMeasuredHeight();
//                int heightLyrics = searchLyricsScrollView.getMeasuredHeight();
//                int heightSum = heightResult + heightLyrics;
//                if (heightResult == 0)
//                    return;
//                if (heightSum < search_result_height * 2) {
//                    ViewGroup.LayoutParams params = searchResultListView.getLayoutParams();
//                    params.height = heightSum / 2;
//                    searchResultListView.setLayoutParams(params);
//                }
//            }
//        });
//    }
//
//    @OptionsItem(android.R.id.home)
//    void menuHomeClick() {
//        startActivity(new Intent(this, MainActivity_.class));
//    }
//
//    @OptionsItem(R.id.menu_search_save_file)
//    void menuSaveFileClick() {
//        if (!existsLyrics()) {
//            AppUtils.showToast(this, R.string.error_exists_lyrics);
//            return;
//        }
//
//        String title = searchTitleEditText.getText().toString();
//        String artist = searchArtistEditText.getText().toString();
//        AppUtils.saveFile(this, title, artist);
//    }
//
//    @OptionsItem(R.id.menu_search_save_cache)
//    void menuSaveCacheClick() {
//        if (!existsLyrics()) {
//            AppUtils.showToast(this, R.string.error_exists_lyrics);
//            return;
//        }
//
//        ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(
//                getString(R.string.message_dialog_confirm_save_cache),
//                getString(R.string.label_confirmation),
//                getString(R.string.label_dialog_confirm_save_cache),
//                null,
//                getString(android.R.string.cancel)
//        );
//        dialog.setClickListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (which == DialogInterface.BUTTON_POSITIVE) {
//                    String title = searchTitleEditText.getText().toString();
//                    String artist = searchArtistEditText.getText().toString();
//                    saveToCacheBackground(title, artist, searchResultAdapter.getSelectedItem());
//                }
//            }
//        });
//        dialog.show(this);
//    }
//
//    @OptionsItem(R.id.menu_search_open_cache)
//    void menuOpenCacheClick() {
//        Intent intent = new Intent(this, CacheActivity_.class);
//        intent.putExtra(CacheActivity.INTENT_SEARCH_TITLE, searchTitleEditText.getText().toString());
//        intent.putExtra(CacheActivity.INTENT_SEARCH_ARTIST, searchArtistEditText.getText().toString());
//        startActivity(intent);
//    }
//
//    @Background
//    void saveToCacheBackground(String title, String artist, ResultItem item) {
//        if (searchCacheHelper.insertOrUpdate(title, artist, item))
//            AppUtils.showToast(this, R.string.message_save_cache);
//    }
//
//    @Click(R.id.searchTitleButton)
//    void searchTitleButtonClick() {
//        final NormalizeDialogFragment dialogFragment = NormalizeDialogFragment.newInstance(searchTitleEditText.getText().toString(), intentSearchTitle);
//        dialogFragment.setClickListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (which == DialogInterface.BUTTON_POSITIVE) {
//                    searchTitleEditText.setText(dialogFragment.getInputText());
//                }
//            }
//        });
//        dialogFragment.show(this);
//    }
//
//    @Click(R.id.searchArtistButton)
//    void searchArtistButtonClick() {
//        final NormalizeDialogFragment dialogFragment = NormalizeDialogFragment.newInstance(searchArtistEditText.getText().toString(), intentSearchArtist);
//        dialogFragment.setClickListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (which == DialogInterface.BUTTON_POSITIVE) {
//                    searchArtistEditText.setText(dialogFragment.getInputText());
//                }
//            }
//        });
//        dialogFragment.show(this);
//    }
//
//    @Click(R.id.searchClearButton)
//    void searchClearButtonClick() {
//        searchTitleEditText.setText(null);
//        searchArtistEditText.setText(null);
//    }
//
//    @Click(R.id.searchStartButton)
//    void searchStartButtonClick(View view) {
//        // Hide keyboard
//        InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
//        inputMethodMgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//
//        String title = searchTitleEditText.getText().toString();
//        String artist = searchArtistEditText.getText().toString();
//        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(artist)) {
//            AppUtils.showToast(this, R.string.error_input_condition);
//            return;
//        }
//
//        // Clear view
//        showSearchResult(null);
//        showLyrics(null);
//        searchResultAdapter.setSelectedItem(null);
//
//        searchResultListView.setVisibility(View.INVISIBLE);
//        searchResultLoadingLayout.setVisibility(View.VISIBLE);
//        searchLyrics(title, artist);
//    }
//
//    // Search
//
//    @Background
//    void searchLyrics(String title, String artist) {
//        Result result = null;
//        try {
//            result = ViewLyricsSearcher.search(title, artist, 0);
//        } catch (Exception e) {
//            Logger.e(e);
//        } finally {
//            showSearchResult(result);
//        }
//    }
//
//    @UiThread
//    void showSearchResult(Result result) {
//        try {
//            searchResultAdapter.clear();
//            if (result != null)
//                searchResultAdapter.addAll(result.getInfoList());
//            searchResultAdapter.notifyDataSetChanged();
//        } finally {
//            searchResultListView.setVisibility(View.VISIBLE);
//            searchResultLoadingLayout.setVisibility(View.INVISIBLE);
//        }
//    }
//
//    // Download
//
//    @ItemClick(R.id.searchResultListView)
//    void searchResultListViewItemClick(@NonNull ResultItem item) {
//        // Hide keyboard
//        InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
//        inputMethodMgr.hideSoftInputFromWindow(searchResultListView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//
//        // Clear view
//        showLyrics(null);
//
//        searchLyricsScrollView.setVisibility(View.INVISIBLE);
//        searchLyricsLoadingLayout.setVisibility(View.VISIBLE);
//        downloadLyrics(item);
//    }
//
//    @Background
//    void downloadLyrics(ResultItem item) {
//        try {
//            if (item != null) {
//                String lyrics = ViewLyricsSearcher.downloadLyricsText(item.getPageUrl());
//                item.setLyrics(lyrics);
//            }
//        } catch (Exception e) {
//            Logger.e(e);
//        } finally {
//            showLyrics(item);
//        }
//    }
//
//    @UiThread
//    void showLyrics(ResultItem item) {
//        if (item == null) {
//            searchLyricsTextView.setText(null);
//        } else {
//            searchLyricsTextView.setText(item.getLyrics());
//        }
//        searchResultAdapter.setSelectedItem(item);
//        searchResultAdapter.notifyDataSetChanged();
//        searchLyricsScrollView.setVisibility(View.VISIBLE);
//        searchLyricsLoadingLayout.setVisibility(View.INVISIBLE);
//    }
//
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//        if (requestCode == AppUtils.REQUEST_CODE_SAVE_FILE) {
//            // 歌詞のファイル保存
//            if (resultCode == RESULT_OK) {
//                if (!existsLyrics()) {
//                    AppUtils.showToast(this, R.string.error_exists_lyrics);
//                    return;
//                }
//
//                Uri uri = resultData.getData();
//                try (OutputStream stream = getContentResolver().openOutputStream(uri);
//                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream,  "UTF-8"))) {
//                    writer.write(searchResultAdapter.getSelectedItem().getLyrics());
//                    writer.flush();
//                    AppUtils.showToast(this, R.string.message_lyrics_save_succeeded);
//                } catch (IOException e) {
//                    Logger.e(e);
//                    AppUtils.showToast(this, R.string.message_lyrics_save_failed);
//                }
//            }
//        }
//
//        // Hide keyboard
//        if (getCurrentFocus() != null) {
//            InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            inputMethodMgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//        }    }
//
//    /**
//     * Check existence of lyrics
//     * @return true if exists lyrics.
//     */
//    private synchronized boolean existsLyrics() {
//        return (searchResultAdapter.getSelectedItem() != null) && !TextUtils.isEmpty(searchResultAdapter.getSelectedItem().getPageUrl());
//    }
//
//    /**
//     * Search result adapter.
//     */
//    private static class SearchResultAdapter extends ArrayAdapter<ResultItem> {
//
//        /** Selected item. */
//        private ResultItem selectedItem = null;
//        /** Set selected item. */
//        void setSelectedItem(ResultItem item) {
//            this.selectedItem = item;
//        }
//        /** Get selected item. */
//        ResultItem getSelectedItem() {
//            return this.selectedItem;
//        }
//
//
//        SearchResultAdapter(Context context) {
//            super(context, R.layout.layout_search_item);
//        }
//
//        @NonNull
//        @Override
//        public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
//            // view
//            final ListView listView = (ListView)parent;
//            final ListItemViewHolder holder;
//            if (convertView == null) {
//                final View view = View.inflate(parent.getContext(), R.layout.layout_search_item, null);
//                holder = new ListItemViewHolder();
//                holder.searchItemRadioButton = (RadioButton)view.findViewById(R.id.searchItemRadioButton);
//                holder.searchItemTitleTextView  = (TextView)view.findViewById(R.id.searchItemTitleTextView);
//                holder.searchItemArtistTextView = (TextView)view.findViewById(R.id.searchItemArtistTextView);
//                holder.searchItemAlbumTextView  = (TextView)view.findViewById(R.id.searchItemAlbumTextView);
//                holder.searchItemDownloadTextView = (TextView)view.findViewById(R.id.searchItemDownloadTextView);
//                holder.searchItemRatingTextView = (TextView)view.findViewById(R.id.searchItemRatingTextView);
//                holder.searchItemFromTextView   = (TextView)view.findViewById(R.id.searchItemFromTextView);
//                view.setTag(holder);
//                convertView = view;
//            } else {
//                holder = (ListItemViewHolder) convertView.getTag();
//            }
//
//            // data
//            ResultItem item = getItem(position);
//            if (item == null)
//                item = new ResultItem();
//            holder.searchItemRadioButton.setChecked((item == selectedItem));
//            holder.searchItemTitleTextView.setText(item.getMusicTitle());
//            holder.searchItemArtistTextView.setText(AppUtils.coalesce(item.getMusicArtist(), "-"));
//            holder.searchItemAlbumTextView.setText(AppUtils.coalesce(item.getMusicAlbum(), "-"));
//            holder.searchItemDownloadTextView.setText(getContext().getString(R.string.label_search_item_download, item.getLyricDownloadsCount()));
//            holder.searchItemRatingTextView.setText(getContext().getString(R.string.label_search_item_rating, item.getLyricRate(), item.getLyricRatesCount()));
//            holder.searchItemFromTextView.setText(getContext().getString(R.string.label_search_item_from, item.getLyricUploader()));
//
//            holder.searchItemRadioButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    listView.performItemClick(v, position, getItemId(position));
//                }
//            });
//
//            return convertView;
//        }
//
//    }
//
//    /** リスト項目のビュー情報を保持するHolder。 */
//    private static class ListItemViewHolder {
//        RadioButton searchItemRadioButton;
//        TextView searchItemTitleTextView;
//        TextView searchItemArtistTextView;
//        TextView searchItemAlbumTextView;
//        TextView searchItemDownloadTextView;
//        TextView searchItemRatingTextView;
//        TextView searchItemFromTextView;
//    }

}


class SearchResultAdapter  extends ArrayAdapter<ResultItem> {

    /** Selected item. */
    private ResultItem selectedItem = null;
    /** Set selected item. */
    public void setSelectedItem(ResultItem item) {
        this.selectedItem = item;
    }
    /** Get selected item. */
    public ResultItem getSelectedItem() {
        return this.selectedItem;
    }


    public SearchResultAdapter(Context context) {
        super(context, R.layout.layout_search_item);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        // view
        final ListView listView = (ListView)parent;
        final ListItemViewHolder holder;
        if (convertView == null) {
            final View view = View.inflate(parent.getContext(), R.layout.layout_search_item, null);
            holder = new ListItemViewHolder();
            holder.searchItemRadioButton = view.findViewById(R.id.searchItemRadioButton);
            holder.searchItemTitleTextView  = view.findViewById(R.id.searchItemTitleTextView);
            holder.searchItemUrlTextView = view.findViewById(R.id.searchItemUrlTextView);
            view.setTag(holder);
            convertView = view;
        } else {
            holder = (ListItemViewHolder) convertView.getTag();
        }

        // data
        ResultItem item = getItem(position);
        if (item == null)
            item = new ResultItem();
        holder.searchItemRadioButton.setChecked((item == selectedItem));
        holder.searchItemRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.performItemClick(v, position, getItemId(position));
            }
        });
        holder.searchItemTitleTextView.setText(item.getMusicTitle());
        holder.searchItemUrlTextView.setText(item.getPageUrl());

        return convertView;
    }


    /** リスト項目のビュー情報を保持するHolder。 */
    static private class ListItemViewHolder {
        RadioButton searchItemRadioButton;
        TextView searchItemTitleTextView;
        TextView searchItemUrlTextView;
    }
}
