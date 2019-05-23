package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wa2c.android.medoly.library.MediaProperty
import com.wa2c.android.medoly.library.PropertyData
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.databinding.ActivitySearchBinding
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.DbHelper
import com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog.ConfirmDialogFragment
import com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog.NormalizeDialogFragment
import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotFoundException
import com.wa2c.android.medoly.plugin.action.lyricsscraper.exception.SiteNotSelectException
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.LyricsSearcherWebView
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.ResultItem
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.prefs.Prefs
import kotlinx.android.synthetic.main.layout_search_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Search Activity.
 */
class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    private lateinit var webView: LyricsSearcherWebView

    private var intentSearchTitle: String? = null
    private var intentSearchArtist: String? = null

    /** Search list adapter.  */
    private lateinit var searchResultAdapter: SearchResultAdapter
    private lateinit var prefs: Prefs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        prefs = Prefs(this)

        // action bar
        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }

        searchResultAdapter = SearchResultAdapter(this)
        binding.searchResultListView.adapter = searchResultAdapter

        intentSearchTitle = intent.extras?.getString(INTENT_SEARCH_TITLE)
        intentSearchArtist = intent.extras?.getString(INTENT_SEARCH_ARTIST)
        binding.searchTitleEditText.setText(intentSearchTitle)
        binding.searchArtistEditText.setText(intentSearchArtist)

        val helper = DbHelper(this)
        val siteList = helper.selectSiteList()

        val initSiteId = try {
            prefs.getLong(R.string.prefkey_selected_site_id, -1)
        } catch (e: Exception) {
            prefs.remove(R.string.prefkey_selected_site_id)
            -1
        }

        // Get selected position
        val selectedPosition = if (siteList.isEmpty()) {
            binding.searchStartButton.isEnabled = false
            -1
        } else {
           siteList.indexOfFirst { i -> i.site_id == initSiteId }

        }

        // Get selected site
        var currentSite = if (selectedPosition < 0 || selectedPosition >= siteList.size) {
            if (siteList.isEmpty())
                null
            else
                siteList[0]
        } else {
            siteList[selectedPosition]
        }

        // Set list adapter
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll(siteList.map { i -> i.site_name })
        binding.searchSiteSpinner.adapter = adapter
        binding.searchSiteSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                currentSite = if (siteList.isEmpty()) null else siteList[0]
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSite = siteList[position]
            }
        }
        binding.searchSiteSpinner.setSelection(selectedPosition)

        // Set web view
        webView = LyricsSearcherWebView(this)
        webView.setOnHandleListener(object : LyricsSearcherWebView.HandleListener {
            override fun onSearchResult(list: List<ResultItem>) {
                showSearchResult(list)
                showLyrics(null)
            }

            override fun onGetLyrics(lyrics: String?) {
                if (lyrics.isNullOrEmpty()) {
                    AppUtils.showToast(this@SearchActivity, R.string.message_lyrics_failure)
                }
                showLyrics(lyrics)
            }

            override fun onError(message: String?) {
                val text = message ?: getString(R.string.message_lyrics_failure)
                AppUtils.showToast(this@SearchActivity, text)
                Timber.d(text)
                if (webView.currentState == LyricsSearcherWebView.STATE_SEARCH) {
                    showSearchResult(null)
                }
                showLyrics(null)
            }
        })

        // Title button
        binding.searchTitleButton.setOnClickListener {
            val dialogFragment = NormalizeDialogFragment.newInstance(binding.searchTitleEditText.text.toString(), intentSearchTitle)
            dialogFragment.clickListener = listener@{ _, which, _ ->
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    binding.searchTitleEditText.setText(dialogFragment.inputText)
                }
            }
            dialogFragment.show(this)
        }

        // Artist button
        binding.searchArtistButton.setOnClickListener {
            val dialogFragment = NormalizeDialogFragment.newInstance(binding.searchArtistEditText.text.toString(), intentSearchArtist)
            dialogFragment.clickListener = listener@{ _, which, _ ->
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    binding.searchArtistEditText.setText(dialogFragment.inputText)
                }
            }
            dialogFragment.show(this)
        }

        // Clear[x] button
        binding.searchClearButton.setOnClickListener {
            binding.searchTitleEditText.text = null
            binding.searchArtistEditText.text = null
        }

        // Search button
        binding.searchStartButton.setOnClickListener {
            webView.stopLoading()

            // Hide keyboard
            val inputMethodMgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodMgr.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val title = binding.searchTitleEditText.text.toString()
            val artist = binding.searchArtistEditText.text.toString()
            if (title.isEmpty() && artist.isEmpty()) {
                AppUtils.showToast(this, R.string.error_input_condition)
                return@setOnClickListener
            }

            try {
                // Clear view
                showSearchResult(null)
                showLyrics(null)
                searchResultAdapter.selectedItem = null

                val propertyData = PropertyData()
                propertyData[MediaProperty.TITLE] = title
                propertyData[MediaProperty.ARTIST] = artist
                //webView.searchCache(propertyData)
                webView.search(propertyData, currentSite!!)
                binding.searchResultListView.visibility = View.INVISIBLE
                binding.searchResultLoadingLayout.visibility = View.VISIBLE
            } catch (e: SiteNotSelectException) {
                AppUtils.showToast(this, R.string.message_no_select_site)
            } catch (e: SiteNotFoundException) {
                AppUtils.showToast(this, R.string.message_no_site)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        // List item
        binding.searchResultListView.setOnItemClickListener { _, _, position, _ ->
            webView.stopLoading()

            val item = searchResultAdapter.getItem(position) ?: return@setOnItemClickListener
            if (item.pageUrl == null)
                return@setOnItemClickListener

            try {
                webView.download(item.pageUrl!!)
                searchResultAdapter.selectedItem = item
                binding.searchLyricsScrollView.visibility = View.INVISIBLE
                binding.searchLyricsLoadingLayout.visibility = View.VISIBLE
            } catch (e: SiteNotSelectException) {
                AppUtils.showToast(this, R.string.message_no_select_site)
            } catch (e: SiteNotFoundException) {
                AppUtils.showToast(this, R.string.message_no_site)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        // adjust size
        binding.searchLyricsScrollView.post(Runnable {
            // adjust height
            val heightResult = binding.searchResultListView.measuredHeight
            val heightLyrics = binding.searchLyricsScrollView.measuredHeight
            val heightSum = heightResult + heightLyrics
            if (heightResult == 0)
                return@Runnable
            val height = resources.getDimensionPixelSize(R.dimen.search_result_height)
            if (heightSum < height * 2) {
                val params = binding.searchResultListView.layoutParams
                params.height = heightSum / 2
                binding.searchResultListView.layoutParams = params
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        binding.searchResultListView.adapter = searchResultAdapter

        binding.searchTitleEditText.setText(intent.getStringExtra(INTENT_SEARCH_TITLE))
        binding.searchArtistEditText.setText(intent.getStringExtra(INTENT_SEARCH_ARTIST))

        showSearchResult(null)
        showLyrics(null)
    }

    public override fun onDestroy() {
        super.onDestroy()
        webView.stopLoading()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.activity_search, menu)
        for (i in 0 until menu.size()) {
            menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        return true
    }

    /**
     * onOptionsItemSelected event.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            R.id.menu_search_save_file -> {
                if (!existsLyrics()) {
                    AppUtils.showToast(this, R.string.error_exists_lyrics)
                    return true
                }

                val title = binding.searchTitleEditText.text.toString()
                val artist = binding.searchArtistEditText.text.toString()
                AppUtils.saveFile(this, title, artist)
                return true
            }
            R.id.menu_search_save_cache -> {

                if (!existsLyrics()) {
                    AppUtils.showToast(this, R.string.error_exists_lyrics)
                    return true
                }

                val dialog = ConfirmDialogFragment.newInstance(
                        getString(R.string.message_dialog_confirm_save_cache),
                        getString(R.string.label_confirmation),
                        getString(R.string.label_dialog_confirm_save_cache), null,
                        getString(android.R.string.cancel)
                )
                dialog.clickListener = listener@{ _, which, _ ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        val title = binding.searchTitleEditText.text.toString()
                        val artist = binding.searchArtistEditText.text.toString()
                        GlobalScope.launch(Dispatchers.Main) {
                            val result = async(Dispatchers.Default) {
                                return@async DbHelper(this@SearchActivity).insertOrUpdateCache(title, artist, searchResultAdapter.selectedItem)
                            }
                            if (result.await())
                                AppUtils.showToast(this@SearchActivity, R.string.message_save_cache)
                        }
                    }
                }
                dialog.show(this)

                return true
            }
            R.id.menu_search_open_cache -> {
                val intent = Intent(this, CacheActivity::class.java)
                intent.putExtra(CacheActivity.INTENT_SEARCH_TITLE, binding.searchTitleEditText.text.toString())
                intent.putExtra(CacheActivity.INTENT_SEARCH_ARTIST, binding.searchArtistEditText.text.toString())
                startActivity(intent)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * On activity result
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == AppUtils.REQUEST_CODE_SAVE_FILE && resultCode == RESULT_OK) {
            // Save to lyrics file
            if (!existsLyrics()) {
                AppUtils.showToast(this, R.string.error_exists_lyrics)
                return
            }

            try {
                val uri = resultData?.data!!
                contentResolver.openOutputStream(uri)!!.bufferedWriter(Charsets.UTF_8).use {
                    it.write(searchResultAdapter.selectedItem!!.lyrics)
                }
                AppUtils.showToast(this, R.string.message_lyrics_save_succeeded)
            } catch (e: Exception) {
                Timber.e(e)
                AppUtils.showToast(this, R.string.message_lyrics_save_failed)
            }
        }

        // Hide keyboard
        if (currentFocus != null) {
            val inputMethodMgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodMgr.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    /**
     * Show search result list.
     */
    internal fun showSearchResult(itemList: List<ResultItem>?) {
        try {
            searchResultAdapter.clear()
            if (itemList != null)
                searchResultAdapter.addAll(itemList)
            searchResultAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            binding.searchResultListView.visibility = View.VISIBLE
            binding.searchResultLoadingLayout.visibility = View.INVISIBLE
        }
    }

    /**
     * Show lyrics.
     */
    internal fun showLyrics(lyrics: String?) {
        val item = searchResultAdapter.selectedItem ?: return
        item.lyrics = lyrics

        binding.searchLyricsTextView.text = lyrics
        searchResultAdapter.notifyDataSetChanged()
        binding.searchLyricsScrollView.visibility = View.VISIBLE
        binding.searchLyricsLoadingLayout.visibility = View.INVISIBLE
    }

    /**
     * Check existence of lyrics
     * @return true if exists lyrics.
     */
    @Synchronized
    private fun existsLyrics(): Boolean {
        return searchResultAdapter.selectedItem != null && !searchResultAdapter.selectedItem!!.pageUrl.isNullOrEmpty()
    }



    companion object {
        const val INTENT_SEARCH_TITLE = "INTENT_SEARCH_TITLE"
        const val INTENT_SEARCH_ARTIST = "INTENT_SEARCH_ARTIST"

        /**
         * Search result adapter.
         */
        private class SearchResultAdapter(context: Context) : ArrayAdapter<ResultItem>(context, R.layout.layout_search_item) {

            /** Selected item.  */
            var selectedItem: ResultItem? = null

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val listView = parent as ListView
                var itemView = convertView
                val holder: ListItemViewHolder
                if (itemView == null) {
                    holder = ListItemViewHolder(parent.context)
                    itemView = holder.itemView
                } else {
                    holder = itemView.tag as ListItemViewHolder
                }

                val item = getItem(position)
                val listener : (View) -> Unit = {
                    listView.performItemClick(it, position, getItemId(position))
                }
                holder.bind(item!!, (item == selectedItem), listener)

                return itemView
            }

            /** List item view holder.  */
            private class ListItemViewHolder(context: Context) {
                val itemView = View.inflate(context, R.layout.layout_search_item, null)!!
                init {
                    itemView.tag = this
                }

                fun bind(item: ResultItem, selected: Boolean, listener: (View) -> Unit) {
                    itemView.searchItemRadioButton.isChecked = selected
                    itemView.searchItemTitleTextView.text = item.musicTitle
                    itemView.searchItemUrlTextView.text = item.pageUrl
                    itemView.searchItemRadioButton.setOnClickListener(listener)
                }
            }
        }
    }
}

