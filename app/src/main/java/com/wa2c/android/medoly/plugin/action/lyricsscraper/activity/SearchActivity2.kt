//package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity
//
//import android.app.Activity
//import android.content.Context
//import android.content.DialogInterface
//import android.content.Intent
//import android.os.Bundle
//import android.os.Handler
//import android.os.HandlerThread
//import android.text.TextUtils
//import android.view.Menu
//import android.view.MenuItem
//import android.view.View
//import android.view.ViewGroup
//import android.view.inputmethod.InputMethodManager
//import android.widget.*
//import com.wa2c.android.medoly.library.MediaProperty
//import com.wa2c.android.medoly.library.PropertyData
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCacheHelper
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog.ConfirmDialogFragment
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog.NormalizeDialogFragment
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotFoundException
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotSelectException
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.LyricsSearcherWebView2
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.ResultItem
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger
//import kotlinx.android.synthetic.main.activity_search.*
//import java.io.BufferedWriter
//import java.io.OutputStream
//import java.io.OutputStreamWriter
//import android.widget.ArrayAdapter
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Prefs
//
//
///**
// * Search Activity.
// */
//class SearchActivity : Activity() {
//
//    private lateinit var webView: LyricsSearcherWebView
//    private lateinit var webView2: LyricsSearcherWebView2
//
//    private var intentSearchTitle: String? = null
//    private var intentSearchArtist: String? = null
//
//    /** Search list adapter.  */
//    private lateinit var searchResultAdapter: SearchResultAdapter
//    private lateinit var prefs: Prefs
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_search)
//        prefs = Prefs(this)
//
//        // action bar
//        actionBar.setDisplayShowHomeEnabled(true)
//        actionBar.setDisplayHomeAsUpEnabled(true)
//
//        searchResultAdapter = SearchResultAdapter(this)
//        searchResultListView.adapter = searchResultAdapter
//
//        intentSearchTitle = intent.extras?.getString(INTENT_SEARCH_TITLE)
//        intentSearchArtist = intent.extras?.getString(INTENT_SEARCH_ARTIST)
//        searchTitleEditText.setText(intentSearchTitle)
//        searchArtistEditText.setText(intentSearchArtist)
//
//        val helper = SearchCacheHelper(this)
//        val siteList = helper.selectSiteList()
//
//        val initSiteId = try {
//            prefs.getLong(R.string.prefkey_selected_site_id, -1)
//        } catch (e: Exception) {
//            prefs.remove(R.string.prefkey_selected_site_id)
//            -1
//        }
//        var currentSite = siteList?.firstOrNull { i -> i.site_id == initSiteId } ?: if (siteList.isNotEmpty()) siteList[0] else null
//        if (currentSite == null) {
//            searchStartButton.isEnabled = false
//        }
//
//        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        adapter.addAll(siteList.map { i -> i.site_name })
//        searchSiteSpinner.adapter = adapter
//        searchSiteSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                currentSite = if (siteList.isEmpty()) null else siteList[0]
//            }
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                currentSite = siteList[position]
//            }
//        }
//
//        // Set web view
//        webView = LyricsSearcherWebView(this)
//        webView.setOnHandleListener(object : LyricsSearcherWebView.HandleListener {
//            override fun onSearchResult(list: List<ResultItem>) {
//                showSearchResult(list)
//                showLyrics(null)
//            }
//
//            override fun onGetLyrics(lyrics: String?) {
//                showLyrics(lyrics)
//            }
//
//            override fun onError(message: String?) {
//                Logger.d(message)
//            }
//        })
//
//        webView2 = LyricsSearcherWebView2(this)
//        webView2.setOnHandleListener(object : LyricsSearcherWebView2.HandleListener {
//            override fun onSearchResult(list: List<ResultItem>) {
//                showSearchResult(list)
//                showLyrics(null)
//            }
//
//            override fun onGetLyrics(lyrics: String?) {
//                showLyrics(lyrics)
//            }
//
//            override fun onError(message: String?) {
//                Logger.d(message)
//            }
//        })
//
//
//        searchTitleButton.setOnClickListener {
//            val dialogFragment = NormalizeDialogFragment.newInstance(searchTitleEditText.text.toString(), intentSearchTitle)
//            dialogFragment.clickListener = DialogInterface.OnClickListener { _, which ->
//                if (which == DialogInterface.BUTTON_POSITIVE) {
//                    searchTitleEditText.setText(dialogFragment.inputText)
//                }
//            }
//            dialogFragment.show(this)
//        }
//
//        searchArtistButton.setOnClickListener {
//            val dialogFragment = NormalizeDialogFragment.newInstance(searchArtistEditText.text.toString(), intentSearchArtist)
//            dialogFragment.clickListener = DialogInterface.OnClickListener { _, which ->
//                if (which == DialogInterface.BUTTON_POSITIVE) {
//                    searchArtistEditText.setText(dialogFragment.inputText)
//                }
//            }
//            dialogFragment.show(this)
//        }
//
//        searchClearButton.setOnClickListener {
//            searchTitleEditText.text = null
//            searchArtistEditText.text = null
//        }
//
//        searchStartButton.setOnClickListener {
//            // Hide keyboard
//            val inputMethodMgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            inputMethodMgr.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
//
//            val title = searchTitleEditText.text.toString()
//            val artist = searchArtistEditText.text.toString()
//            if (title.isEmpty() && artist.isEmpty()) {
//                AppUtils.showToast(this, R.string.error_input_condition)
//                return@setOnClickListener
//            }
//
//            try {
//                // Clear view
//                showSearchResult(null)
//                showLyrics(null)
//                searchResultAdapter.selectedItem = null
//
//                val propertyData = PropertyData()
//                propertyData[MediaProperty.TITLE] = title
//                propertyData[MediaProperty.ARTIST] = artist
//                //webView.search(propertyData)
//                webView2.search(propertyData, currentSite!!)
//                searchResultListView.visibility = View.INVISIBLE
//                searchResultLoadingLayout.visibility = View.VISIBLE
//            } catch (e: SiteNotSelectException) {
//                AppUtils.showToast(this, R.string.message_no_select_site)
//            } catch (e: SiteNotFoundException) {
//                AppUtils.showToast(this, R.string.message_no_site)
//            } catch (e: Exception) {
//                Logger.e(e)
//            }
//        }
//
//        searchResultListView.setOnItemClickListener { _, _, position, _ ->
//            val item = searchResultAdapter.getItem(position) ?: return@setOnItemClickListener
//            if (item.pageUrl == null)
//                return@setOnItemClickListener
//
//            try {
//                //webView.download(item.pageUrl!!)
//                webView2.download(item.pageUrl!!)
//                searchResultAdapter.selectedItem = item
//                searchLyricsScrollView.visibility = View.INVISIBLE
//                searchLyricsLoadingLayout.visibility = View.VISIBLE
//            } catch (e: SiteNotSelectException) {
//                AppUtils.showToast(this, R.string.message_no_select_site)
//            } catch (e: SiteNotFoundException) {
//                AppUtils.showToast(this, R.string.message_no_site)
//            } catch (e: Exception) {
//                Logger.e(e)
//            }
//        }
//
//        // adjust size
//        searchLyricsScrollView.post(Runnable {
//            // adjust height
//            val heightResult = searchResultListView.measuredHeight
//            val heightLyrics = searchLyricsScrollView.measuredHeight
//            val heightSum = heightResult + heightLyrics
//            if (heightResult == 0)
//                return@Runnable
//            val height = resources.getDimensionPixelSize(R.dimen.search_result_height)
//            if (heightSum < height * 2) {
//                val params = searchResultListView.layoutParams
//                params.height = heightSum / 2
//                searchResultListView.layoutParams = params
//            }
//        })
//    }
//
//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        setIntent(intent)
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.activity_search, menu)
//        for (i in 0 until menu.size()) {
//            menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
//        }
//        return true
//    }
//
//    /**
//     * onOptionsItemSelected event.
//     */
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            android.R.id.home -> {
//                startActivity(Intent(this, MainActivity::class.java))
//                return true
//            }
//            R.id.menu_search_save_file -> {
//                if (!existsLyrics()) {
//                    AppUtils.showToast(this, R.string.error_exists_lyrics)
//                    return true
//                }
//
//                val title = searchTitleEditText.text.toString()
//                val artist = searchArtistEditText.text.toString()
//                AppUtils.saveFile(this, title, artist)
//                return true
//            }
//            R.id.menu_search_save_cache -> {
//
//                if (!existsLyrics()) {
//                    AppUtils.showToast(this, R.string.error_exists_lyrics)
//                    return true
//                }
//
//                val dialog = ConfirmDialogFragment.newInstance(
//                        getString(R.string.message_dialog_confirm_save_cache),
//                        getString(R.string.label_confirmation),
//                        getString(R.string.label_dialog_confirm_save_cache), null,
//                        getString(android.R.string.cancel)
//                )
//                dialog.clickListener = DialogInterface.OnClickListener { _, which ->
//                    if (which == DialogInterface.BUTTON_POSITIVE) {
//                        val title = searchTitleEditText.text.toString()
//                        val artist = searchArtistEditText.text.toString()
//                        saveToCacheBackground(title, artist, searchResultAdapter.selectedItem)
//                    }
//                }
//                dialog.show(this)
//
//                return true
//            }
//            R.id.menu_search_open_cache -> {
//                val intent = Intent(this, CacheActivity::class.java)
//                intent.putExtra(CacheActivity.INTENT_SEARCH_TITLE, searchTitleEditText.text.toString())
//                intent.putExtra(CacheActivity.INTENT_SEARCH_ARTIST, searchArtistEditText.text.toString())
//                startActivity(intent)
//                return true
//            }
//        }
//
//        return super.onOptionsItemSelected(item)
//    }
//
//    private fun saveToCacheBackground(title: String, artist: String, item: ResultItem?) {
//        val searchCacheHelper = SearchCacheHelper(this)
//        val saveHandlerThread = HandlerThread("saveThreadHandler")
//        saveHandlerThread.start()
//        val saveHandler = Handler(saveHandlerThread.looper)
//        saveHandler.post {
//            if (searchCacheHelper.insertOrUpdate(title, artist, item))
//                AppUtils.showToast(applicationContext, R.string.message_save_cache)
//        }
//    }
//
//
////    @OnClick(R.id.searchTitleButton)
////    internal fun searchTitleButtonClick(view: View) {
////        val dialogFragment = NormalizeDialogFragment.newInstance(searchTitleEditText!!.text.toString(), intentSearchTitle)
////        dialogFragment.setClickListener { dialog, which ->
////            if (which == DialogInterface.BUTTON_POSITIVE) {
////                searchTitleEditText!!.setText(dialogFragment.inputText)
////            }
////        }
////        dialogFragment.show(this)
////    }
//
////    @OnClick(R.id.searchArtistButton)
////    internal fun searchArtistButtonClick(view: View) {
////        val dialogFragment = NormalizeDialogFragment.newInstance(searchArtistEditText!!.text.toString(), intentSearchArtist)
////        dialogFragment.setClickListener { dialog, which ->
////            if (which == DialogInterface.BUTTON_POSITIVE) {
////                searchArtistEditText!!.setText(dialogFragment.inputText)
////            }
////        }
////        dialogFragment.show(this)
////    }
//
////    @OnClick(R.id.searchClearButton)
////    internal fun searchClearButtonClick(view: View) {
////        searchTitleEditText!!.setText(null)
////        searchArtistEditText!!.setText(null)
////    }
//
////    @OnClick(R.id.searchStartButton)
////    internal fun searchStartButtonClick(view: View) {
////        // Hide keyboard
////        val inputMethodMgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
////        inputMethodMgr.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
////
////        val title = searchTitleEditText!!.text.toString()
////        val artist = searchArtistEditText!!.text.toString()
////        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(artist)) {
////            AppUtils.showToast(this, R.string.error_input_condition)
////            return
////        }
////
////
////        try {
////            // Clear view
////            showSearchResult(null)
////            showLyrics(null)
////            searchResultAdapter!!.selectedItem = null
////
////            val propertyData = PropertyData()
////            propertyData[MediaProperty.TITLE] = title
////            propertyData[MediaProperty.ARTIST] = artist
////            webView!!.search(propertyData)
////            searchResultListView!!.visibility = View.INVISIBLE
////            searchResultLoadingLayout!!.visibility = View.VISIBLE
////        } catch (e: SiteNotSelectException) {
////            AppUtils.showToast(this, R.string.message_no_select_site)
////        } catch (e: SiteNotFoundException) {
////            AppUtils.showToast(this, R.string.message_no_site)
////        } catch (e: Exception) {
////            Logger.e(e)
////        }
////
////    }
//
////    @OnItemClick(R.id.searchResultListView)
////    internal fun searchResultListViewClick(position: Int) {
////        val item = searchResultAdapter!!.getItem(position) ?: return
////
////        try {
////            webView!!.download(item.pageUrl)
////            searchResultAdapter!!.selectedItem = item
////            searchLyricsScrollView!!.visibility = View.INVISIBLE
////            searchLyricsLoadingLayout!!.visibility = View.VISIBLE
////        } catch (e: SiteNotSelectException) {
////            AppUtils.showToast(this, R.string.message_no_select_site)
////        } catch (e: SiteNotFoundException) {
////            AppUtils.showToast(this, R.string.message_no_site)
////        } catch (e: Exception) {
////            Logger.e(e)
////        }
////
////    }
//
//    internal fun showSearchResult(itemList: List<ResultItem>?) {
//        try {
//            searchResultAdapter.clear()
//            if (itemList != null)
//                searchResultAdapter.addAll(itemList)
//            searchResultAdapter.notifyDataSetChanged()
//        } catch (e: Exception) {
//            Logger.e(e)
//        } finally {
//            searchResultListView.visibility = View.VISIBLE
//            searchResultLoadingLayout.visibility = View.INVISIBLE
//        }
//    }
//
//    internal fun showLyrics(lyrics: String?) {
//        val item = searchResultAdapter.selectedItem ?: return
//        item.lyrics = lyrics
//
//        searchLyricsTextView.text = lyrics
//        searchResultAdapter.notifyDataSetChanged()
//        searchLyricsScrollView.visibility = View.VISIBLE
//        searchLyricsLoadingLayout.visibility = View.INVISIBLE
//    }
//
//    /**
//     * Check existence of lyrics
//     * @return true if exists lyrics.
//     */
//    @Synchronized
//    private fun existsLyrics(): Boolean {
//        return searchResultAdapter.selectedItem != null && !searchResultAdapter.selectedItem!!.pageUrl.isNullOrEmpty()
//    }
//
//    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent) {
//        if (requestCode == AppUtils.REQUEST_CODE_SAVE_FILE) {
//            // 歌詞のファイル保存
//            if (resultCode == Activity.RESULT_OK) {
//                if (!existsLyrics()) {
//                    AppUtils.showToast(this, R.string.error_exists_lyrics)
//                    return
//                }
//
//                val uri = resultData.data
//                var stream: OutputStream? = null
//                var writer: BufferedWriter? = null
//                try {
//                    stream = contentResolver.openOutputStream(uri!!)
//                    writer = BufferedWriter(OutputStreamWriter(stream!!, "UTF-8"))
//                    writer.write(searchResultAdapter.selectedItem!!.lyrics!!)
//                    writer.flush()
//                    AppUtils.showToast(this, R.string.message_lyrics_save_succeeded)
//                } catch (e: Exception) {
//                    Logger.e(e)
//                    AppUtils.showToast(this, R.string.message_lyrics_save_failed)
//                } finally {
//                    if (writer != null)
//                        try {
//                            writer.close()
//                        } catch (ignore: Exception) {
//                        }
//
//                    if (stream != null)
//                        try {
//                            stream.close()
//                        } catch (ignore: Exception) {
//                        }
//
//                }
//            }
//        }
//
//        // Hide keyboard
//        if (currentFocus != null) {
//            val inputMethodMgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            inputMethodMgr.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
//        }
//    }
//
//
//
//
//    companion object {
//        const val INTENT_SEARCH_TITLE = "INTENT_SEARCH_TITLE"
//        const val INTENT_SEARCH_ARTIST = "INTENT_SEARCH_ARTIST"
//    }
//
//
//
//
//    //    public static final String INTENT_SEARCH_TITLE = "INTENT_SEARCH_TITLE";
//    //    public static final String INTENT_SEARCH_ARTIST = "INTENT_SEARCH_ARTIST";
//
//    //    /** Search list adapter. */
//    //    private SearchResultAdapter searchResultAdapter;
//    //    /** Search cache helper. */
//    //    private SearchCacheHelper searchCacheHelper;
//    //
//    //    @Extra(INTENT_SEARCH_TITLE)
//    //    String intentSearchTitle;
//    //    @Extra(INTENT_SEARCH_ARTIST)
//    //    String intentSearchArtist;
//    //
//    //    @ViewById
//    //    Button searchTitleButton;
//    //    @ViewById
//    //    EditText searchTitleEditText;
//    //    @ViewById
//    //    Button searchArtistButton;
//    //    @ViewById
//    //    EditText searchArtistEditText;
//    //    @ViewById
//    //    ImageButton searchClearButton;
//    //    @ViewById
//    //    ImageButton searchStartButton;
//    //    @ViewById
//    //    ListView searchResultListView;
//    //    @ViewById
//    //    ScrollView searchLyricsScrollView;
//    //    @ViewById
//    //    TextView searchLyricsTextView;
//    //
//    //    @ViewById
//    //    View searchResultLoadingLayout;
//    //    @ViewById
//    //    View searchLyricsLoadingLayout;
//    //
//    //    @DimensionPixelSizeRes
//    //    int search_result_height;
//    //
//    //
//    //
//    //    @Override
//    //    protected void onNewIntent(Intent intent) {
//    //        super.onNewIntent(intent);
//    //        setIntent(intent);
//    //
//    //        searchResultListView.setAdapter(searchResultAdapter);
//    //        searchTitleEditText.setText(intentSearchTitle);
//    //        searchArtistEditText.setText(intentSearchArtist);
//    //    }
//    //
//    //    @AfterViews
//    //    void afterViews() {
//    //        ActionBar actionBar = getActionBar();
//    //        if (actionBar != null) {
//    //            actionBar.setDisplayShowHomeEnabled(true);
//    //            actionBar.setDisplayHomeAsUpEnabled(true);
//    //            actionBar.setDisplayShowTitleEnabled(true);
//    //        }
//    //
//    //        searchCacheHelper = new SearchCacheHelper(this);
//    //        searchResultAdapter = new SearchResultAdapter(this);
//    //        searchResultListView.setAdapter(searchResultAdapter);
//    //
//    //        searchTitleEditText.setText(intentSearchTitle);
//    //        searchArtistEditText.setText(intentSearchArtist);
//    //
//    //        // adjust size
//    //        searchLyricsScrollView.post(new Runnable() {
//    //            @Override
//    //            public void run() {
//    //                // adjust height
//    //                int heightResult = searchResultListView.getMeasuredHeight();
//    //                int heightLyrics = searchLyricsScrollView.getMeasuredHeight();
//    //                int heightSum = heightResult + heightLyrics;
//    //                if (heightResult == 0)
//    //                    return;
//    //                if (heightSum < search_result_height * 2) {
//    //                    ViewGroup.LayoutParams params = searchResultListView.getLayoutParams();
//    //                    params.height = heightSum / 2;
//    //                    searchResultListView.setLayoutParams(params);
//    //                }
//    //            }
//    //        });
//    //    }
//    //
//    //    @OptionsItem(android.R.id.home)
//    //    void menuHomeClick() {
//    //        startActivity(new Intent(this, MainActivity_.class));
//    //    }
//    //
//    //    @OptionsItem(R.id.menu_search_save_file)
//    //    void menuSaveFileClick() {
//    //        if (!existsLyrics()) {
//    //            AppUtils.showToast(this, R.string.error_exists_lyrics);
//    //            return;
//    //        }
//    //
//    //        String title = searchTitleEditText.getText().toString();
//    //        String artist = searchArtistEditText.getText().toString();
//    //        AppUtils.saveFile(this, title, artist);
//    //    }
//    //
//    //    @OptionsItem(R.id.menu_search_save_cache)
//    //    void menuSaveCacheClick() {
//    //        if (!existsLyrics()) {
//    //            AppUtils.showToast(this, R.string.error_exists_lyrics);
//    //            return;
//    //        }
//    //
//    //        ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(
//    //                getString(R.string.message_dialog_confirm_save_cache),
//    //                getString(R.string.label_confirmation),
//    //                getString(R.string.label_dialog_confirm_save_cache),
//    //                null,
//    //                getString(android.R.string.cancel)
//    //        );
//    //        dialog.setClickListener(new DialogInterface.OnClickListener() {
//    //            @Override
//    //            public void onClick(DialogInterface dialog, int which) {
//    //                if (which == DialogInterface.BUTTON_POSITIVE) {
//    //                    String title = searchTitleEditText.getText().toString();
//    //                    String artist = searchArtistEditText.getText().toString();
//    //                    saveToCacheBackground(title, artist, searchResultAdapter.getSelectedItem());
//    //                }
//    //            }
//    //        });
//    //        dialog.show(this);
//    //    }
//    //
//    //    @OptionsItem(R.id.menu_search_open_cache)
//    //    void menuOpenCacheClick() {
//    //        Intent intent = new Intent(this, CacheActivity_.class);
//    //        intent.putExtra(CacheActivity.INTENT_SEARCH_TITLE, searchTitleEditText.getText().toString());
//    //        intent.putExtra(CacheActivity.INTENT_SEARCH_ARTIST, searchArtistEditText.getText().toString());
//    //        startActivity(intent);
//    //    }
//    //
//    //    @Background
//    //    void saveToCacheBackground(String title, String artist, ResultItem item) {
//    //        if (searchCacheHelper.insertOrUpdate(title, artist, item))
//    //            AppUtils.showToast(this, R.string.message_save_cache);
//    //    }
//    //
//    //    @Click(R.id.searchTitleButton)
//    //    void searchTitleButtonClick() {
//    //        final NormalizeDialogFragment dialogFragment = NormalizeDialogFragment.newInstance(searchTitleEditText.getText().toString(), intentSearchTitle);
//    //        dialogFragment.setClickListener(new DialogInterface.OnClickListener() {
//    //            @Override
//    //            public void onClick(DialogInterface dialog, int which) {
//    //                if (which == DialogInterface.BUTTON_POSITIVE) {
//    //                    searchTitleEditText.setText(dialogFragment.getInputText());
//    //                }
//    //            }
//    //        });
//    //        dialogFragment.show(this);
//    //    }
//    //
//    //    @Click(R.id.searchArtistButton)
//    //    void searchArtistButtonClick() {
//    //        final NormalizeDialogFragment dialogFragment = NormalizeDialogFragment.newInstance(searchArtistEditText.getText().toString(), intentSearchArtist);
//    //        dialogFragment.setClickListener(new DialogInterface.OnClickListener() {
//    //            @Override
//    //            public void onClick(DialogInterface dialog, int which) {
//    //                if (which == DialogInterface.BUTTON_POSITIVE) {
//    //                    searchArtistEditText.setText(dialogFragment.getInputText());
//    //                }
//    //            }
//    //        });
//    //        dialogFragment.show(this);
//    //    }
//    //
//    //    @Click(R.id.searchClearButton)
//    //    void searchClearButtonClick() {
//    //        searchTitleEditText.setText(null);
//    //        searchArtistEditText.setText(null);
//    //    }
//    //
//    //    @Click(R.id.searchStartButton)
//    //    void searchStartButtonClick(View view) {
//    //        // Hide keyboard
//    //        InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
//    //        inputMethodMgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//    //
//    //        String title = searchTitleEditText.getText().toString();
//    //        String artist = searchArtistEditText.getText().toString();
//    //        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(artist)) {
//    //            AppUtils.showToast(this, R.string.error_input_condition);
//    //            return;
//    //        }
//    //
//    //        // Clear view
//    //        showSearchResult(null);
//    //        showLyrics(null);
//    //        searchResultAdapter.setSelectedItem(null);
//    //
//    //        searchResultListView.setVisibility(View.INVISIBLE);
//    //        searchResultLoadingLayout.setVisibility(View.VISIBLE);
//    //        searchLyrics(title, artist);
//    //    }
//    //
//    //    // Search
//    //
//    //    @Background
//    //    void searchLyrics(String title, String artist) {
//    //        Result result = null;
//    //        try {
//    //            result = ViewLyricsSearcher.search(title, artist, 0);
//    //        } catch (Exception e) {
//    //            Logger.e(e);
//    //        } finally {
//    //            showSearchResult(result);
//    //        }
//    //    }
//    //
//    //    @UiThread
//    //    void showSearchResult(Result result) {
//    //        try {
//    //            searchResultAdapter.clear();
//    //            if (result != null)
//    //                searchResultAdapter.addAll(result.getInfoList());
//    //            searchResultAdapter.notifyDataSetChanged();
//    //        } finally {
//    //            searchResultListView.setVisibility(View.VISIBLE);
//    //            searchResultLoadingLayout.setVisibility(View.INVISIBLE);
//    //        }
//    //    }
//    //
//    //    // Download
//    //
//    //    @ItemClick(R.id.searchResultListView)
//    //    void searchResultListViewItemClick(@NonNull ResultItem item) {
//    //        // Hide keyboard
//    //        InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
//    //        inputMethodMgr.hideSoftInputFromWindow(searchResultListView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//    //
//    //        // Clear view
//    //        showLyrics(null);
//    //
//    //        searchLyricsScrollView.setVisibility(View.INVISIBLE);
//    //        searchLyricsLoadingLayout.setVisibility(View.VISIBLE);
//    //        downloadLyrics(item);
//    //    }
//    //
//    //    @Background
//    //    void downloadLyrics(ResultItem item) {
//    //        try {
//    //            if (item != null) {
//    //                String lyrics = ViewLyricsSearcher.downloadLyricsText(item.getPageUrl());
//    //                item.setLyrics(lyrics);
//    //            }
//    //        } catch (Exception e) {
//    //            Logger.e(e);
//    //        } finally {
//    //            showLyrics(item);
//    //        }
//    //    }
//    //
//    //    @UiThread
//    //    void showLyrics(ResultItem item) {
//    //        if (item == null) {
//    //            searchLyricsTextView.setText(null);
//    //        } else {
//    //            searchLyricsTextView.setText(item.getLyrics());
//    //        }
//    //        searchResultAdapter.setSelectedItem(item);
//    //        searchResultAdapter.notifyDataSetChanged();
//    //        searchLyricsScrollView.setVisibility(View.VISIBLE);
//    //        searchLyricsLoadingLayout.setVisibility(View.INVISIBLE);
//    //    }
//    //
//    //
//    //    @Override
//    //    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//    //        if (requestCode == AppUtils.REQUEST_CODE_SAVE_FILE) {
//    //            // 歌詞のファイル保存
//    //            if (resultCode == RESULT_OK) {
//    //                if (!existsLyrics()) {
//    //                    AppUtils.showToast(this, R.string.error_exists_lyrics);
//    //                    return;
//    //                }
//    //
//    //                Uri uri = resultData.getData();
//    //                try (OutputStream stream = getContentResolver().openOutputStream(uri);
//    //                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream,  "UTF-8"))) {
//    //                    writer.write(searchResultAdapter.getSelectedItem().getLyrics());
//    //                    writer.flush();
//    //                    AppUtils.showToast(this, R.string.message_lyrics_save_succeeded);
//    //                } catch (IOException e) {
//    //                    Logger.e(e);
//    //                    AppUtils.showToast(this, R.string.message_lyrics_save_failed);
//    //                }
//    //            }
//    //        }
//    //
//    //        // Hide keyboard
//    //        if (getCurrentFocus() != null) {
//    //            InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//    //            inputMethodMgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//    //        }    }
//    //
//    //    /**
//    //     * Check existence of lyrics
//    //     * @return true if exists lyrics.
//    //     */
//    //    private synchronized boolean existsLyrics() {
//    //        return (searchResultAdapter.getSelectedItem() != null) && !TextUtils.isEmpty(searchResultAdapter.getSelectedItem().getPageUrl());
//    //    }
//    //
//    //    /**
//    //     * Search result adapter.
//    //     */
//    //    private static class SearchResultAdapter extends ArrayAdapter<ResultItem> {
//    //
//    //        /** Selected item. */
//    //        private ResultItem selectedItem = null;
//    //        /** Set selected item. */
//    //        void setSelectedItem(ResultItem item) {
//    //            this.selectedItem = item;
//    //        }
//    //        /** Get selected item. */
//    //        ResultItem getSelectedItem() {
//    //            return this.selectedItem;
//    //        }
//    //
//    //
//    //        SearchResultAdapter(Context context) {
//    //            super(context, R.layout.layout_search_item);
//    //        }
//    //
//    //        @NonNull
//    //        @Override
//    //        public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
//    //            // view
//    //            final ListView listView = (ListView)parent;
//    //            final ListItemViewHolder holder;
//    //            if (convertView == null) {
//    //                final View view = View.inflate(parent.getContext(), R.layout.layout_search_item, null);
//    //                holder = new ListItemViewHolder();
//    //                holder.searchItemRadioButton = (RadioButton)view.findViewById(R.id.searchItemRadioButton);
//    //                holder.searchItemTitleTextView  = (TextView)view.findViewById(R.id.searchItemTitleTextView);
//    //                holder.searchItemArtistTextView = (TextView)view.findViewById(R.id.searchItemArtistTextView);
//    //                holder.searchItemAlbumTextView  = (TextView)view.findViewById(R.id.searchItemAlbumTextView);
//    //                holder.searchItemDownloadTextView = (TextView)view.findViewById(R.id.searchItemDownloadTextView);
//    //                holder.searchItemRatingTextView = (TextView)view.findViewById(R.id.searchItemRatingTextView);
//    //                holder.searchItemFromTextView   = (TextView)view.findViewById(R.id.searchItemFromTextView);
//    //                view.setTag(holder);
//    //                convertView = view;
//    //            } else {
//    //                holder = (ListItemViewHolder) convertView.getTag();
//    //            }
//    //
//    //            // data
//    //            ResultItem item = getItem(position);
//    //            if (item == null)
//    //                item = new ResultItem();
//    //            holder.searchItemRadioButton.setChecked((item == selectedItem));
//    //            holder.searchItemTitleTextView.setText(item.getMusicTitle());
//    //            holder.searchItemArtistTextView.setText(AppUtils.coalesce(item.getMusicArtist(), "-"));
//    //            holder.searchItemAlbumTextView.setText(AppUtils.coalesce(item.getMusicAlbum(), "-"));
//    //            holder.searchItemDownloadTextView.setText(getContext().getString(R.string.label_search_item_download, item.getLyricDownloadsCount()));
//    //            holder.searchItemRatingTextView.setText(getContext().getString(R.string.label_search_item_rating, item.getLyricRate(), item.getLyricRatesCount()));
//    //            holder.searchItemFromTextView.setText(getContext().getString(R.string.label_search_item_from, item.getLyricUploader()));
//    //
//    //            holder.searchItemRadioButton.setOnClickListener(new View.OnClickListener() {
//    //                @Override
//    //                public void onClick(View v) {
//    //                    listView.performItemClick(v, position, getItemId(position));
//    //                }
//    //            });
//    //
//    //            return convertView;
//    //        }
//    //
//    //    }
//    //
//    //    /** リスト項目のビュー情報を保持するHolder。 */
//    //    private static class ListItemViewHolder {
//    //        RadioButton searchItemRadioButton;
//    //        TextView searchItemTitleTextView;
//    //        TextView searchItemArtistTextView;
//    //        TextView searchItemAlbumTextView;
//    //        TextView searchItemDownloadTextView;
//    //        TextView searchItemRatingTextView;
//    //        TextView searchItemFromTextView;
//    //    }
//
//}
//
//
//internal class SearchResultAdapter(context: Context) : ArrayAdapter<ResultItem>(context, R.layout.layout_search_item) {
//
//    /** Selected item.  */
//    /** Get selected item.  */
//    /** Set selected item.  */
//    var selectedItem: ResultItem? = null
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        var itemView = convertView
//        // view
//        val listView = parent as ListView
//        val holder: ListItemViewHolder
//        if (itemView == null) {
//            val view = View.inflate(parent.getContext(), R.layout.layout_search_item, null)
//            holder = ListItemViewHolder()
//            holder.searchItemRadioButton = view.findViewById(R.id.searchItemRadioButton)
//            holder.searchItemTitleTextView = view.findViewById(R.id.searchItemTitleTextView)
//            holder.searchItemUrlTextView = view.findViewById(R.id.searchItemUrlTextView)
//            view.tag = holder
//            itemView = view
//        } else {
//            holder = itemView.tag as ListItemViewHolder
//        }
//
//        // data
//        var item = getItem(position)
//        if (item == null)
//            item = ResultItem()
//        holder.searchItemRadioButton!!.isChecked = item === selectedItem
//        holder.searchItemRadioButton!!.setOnClickListener { v -> listView.performItemClick(v, position, getItemId(position)) }
//        holder.searchItemTitleTextView!!.text = item.musicTitle
//        holder.searchItemUrlTextView!!.text = item.pageUrl
//
//        return itemView!!
//    }
//
//
//    /** リスト項目のビュー情報を保持するHolder。  */
//    private class ListItemViewHolder {
//        internal var searchItemRadioButton: RadioButton? = null
//        internal var searchItemTitleTextView: TextView? = null
//        internal var searchItemUrlTextView: TextView? = null
//    }
//}
