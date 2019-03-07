package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.DbHelper
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCache
import com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog.CacheDialogFragment
import com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog.ConfirmDialogFragment
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import kotlinx.android.synthetic.main.activity_cache.*
import kotlinx.android.synthetic.main.layout_cache_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*


/**
 * Cache activity
 */
class CacheActivity : Activity() {

    /** Search list adapter.  */
    private lateinit var cacheAdapter: CacheAdapter
    /** Search cache helper.  */
    private lateinit var dbHelper: DbHelper
    /** Current cache item. */
    private var currentCacheItem: SearchCache? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cache)

        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowTitleEnabled(true)

        dbHelper = DbHelper(this)
        cacheAdapter = CacheAdapter(this)
        cacheListView.adapter = cacheAdapter

        cacheTitleEditText.setText(intent.getStringExtra(INTENT_SEARCH_TITLE))
        cacheArtistEditText.setText(intent.getStringExtra(INTENT_SEARCH_ARTIST))

        cacheInputClearButton.setOnClickListener{
            cacheTitleEditText.text = null
            cacheArtistEditText.text = null
        }

        cacheInputSearchButton.setOnClickListener {
            // Hide keyboard
            val inputMethodMgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodMgr.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val title = cacheTitleEditText.text.toString()
            val artist = cacheArtistEditText.text.toString()

            searchCache(title, artist)
            cacheTitleEditText.tag = title
            cacheArtistEditText.tag = artist

        }

        cacheListView.setOnItemClickListener { _, _, position, _ ->
            val item = cacheAdapter.getItem(position)
            val dialog = CacheDialogFragment.newInstance(item)
            dialog.clickListener = listener@{ _, which, _ ->
                when (which) {
                    // Save
                    DialogInterface.BUTTON_POSITIVE -> {
                        currentCacheItem = item
                        AppUtils.saveFile(this@CacheActivity, item.title, item.artist)
                    }
                    // Re-searchCache
                    DialogInterface.BUTTON_NEGATIVE -> {
                        currentCacheItem = item
                        val intent = Intent(this@CacheActivity, SearchActivity::class.java)
                        intent.putExtra(SearchActivity.INTENT_SEARCH_TITLE, item.title)
                        intent.putExtra(SearchActivity.INTENT_SEARCH_ARTIST, item.artist)
                        startActivity(intent)
                    }
                    // Delete lyrics
                    CacheDialogFragment.DIALOG_RESULT_DELETE_LYRICS -> searchCache(cacheTitleEditText.tag as String, cacheArtistEditText.tag as String)
                    // Delete cache
                    CacheDialogFragment.DIALOG_RESULT_DELETE_CACHE -> searchCache(cacheTitleEditText.tag as String, cacheArtistEditText.tag as String)
                }
            }
            dialog.show(this)
        }

    }

    /**
     * On New Intent
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    /**
     * On create options menu.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.activity_cache, menu)
        for (i in 0 until menu.size()) {
            menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        return true
    }

    /**
     * On options item selected.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.menu_cache_delete -> {
                if (cacheAdapter.checkedSet.size == 0) {
                    AppUtils.showToast(this, R.string.error_delete_cache_check)
                    return true
                }

                val dialog = ConfirmDialogFragment.newInstance(
                        getString(R.string.message_dialog_cache_delete),
                        getString(R.string.label_confirmation),
                        getString(R.string.label_dialog_cache_delete),
                        null,
                        getString(android.R.string.cancel)
                )
                dialog.clickListener = listener@{ _, which, _ ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        GlobalScope.launch(Dispatchers.Main) {
                            val result = async(Dispatchers.Default) {
                                return@async dbHelper.deleteCache(cacheAdapter.checkedSet)
                            }
                            if (result.await()) {
                                cacheAdapter.removeCheckedItem()
                                AppUtils.showToast(this@CacheActivity, R.string.message_delete_cache)
                            }
                        }
                    }
                }
                dialog.show(this)
            }
            R.id.menu_cache_open_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(SearchActivity.INTENT_SEARCH_TITLE, cacheTitleEditText.text.toString())
                intent.putExtra(SearchActivity.INTENT_SEARCH_ARTIST, cacheArtistEditText.text.toString())
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * On activity result
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == AppUtils.REQUEST_CODE_SAVE_FILE && resultCode == Activity.RESULT_OK) {
            // Save to lyrics file
            val uri = resultData?.data
            try {
                contentResolver.openOutputStream(uri).bufferedWriter(Charsets.UTF_8).use {
                    it.write(currentCacheItem!!.makeResultItem()!!.lyrics)
                }
                AppUtils.showToast(this, R.string.message_lyrics_save_succeeded)
            } catch (e: Exception) {
                Timber.e(e)
                AppUtils.showToast(this, R.string.message_lyrics_save_failed)
            }
        }

        currentCacheItem = null

        // Hide keyboard
        if (currentFocus != null) {
            val inputMethodMgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodMgr.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    /**
     * Search cache.
     */
    private fun searchCache(title: String, artist: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = async(Dispatchers.Default) {
                return@async dbHelper.searchCache(title, artist)
            }
            cacheAdapter.setList(result.await().toMutableList())
        }
    }

    companion object {
        const val INTENT_SEARCH_TITLE = "INTENT_SEARCH_TITLE"
        const val INTENT_SEARCH_ARTIST = "INTENT_SEARCH_ARTIST"

        /**
         * Search result adapter.
         */
        private class CacheAdapter internal constructor(context: Context) : ArrayAdapter<SearchCache>(context, R.layout.layout_search_item) {
            /** Checked item. */
            val checkedSet = HashSet<SearchCache>()
            /** SearchCache list.  */
            private var cacheList: MutableList<SearchCache> = ArrayList()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var itemView = convertView
                // itemView
                val holder: ListItemViewHolder
                if (itemView == null) {
                    holder = ListItemViewHolder(parent.context)
                    itemView = holder.itemView
                } else {
                    holder = itemView.tag as ListItemViewHolder
                }

                holder.bind(getItem(position), checkedSet)

                return itemView
            }

            fun setList(list: MutableList<SearchCache>) {
                cacheList = list
                showCacheList()
            }

            fun removeCheckedItem() {
                cacheList.removeAll(checkedSet)
                showCacheList()
            }

            fun showCacheList() {
                clear()
                addAll(cacheList)
                notifyDataSetChanged()
            }

            override fun notifyDataSetChanged() {
                super.notifyDataSetChanged()
                checkedSet.clear()
            }

            /** List item view holder.  */
            private class ListItemViewHolder(val context: Context) {
                val itemView = View.inflate(context, R.layout.layout_cache_item, null)!!
                init {
                    itemView.tag = this
                }

                fun bind(item: SearchCache, checkedSet: HashSet<SearchCache>) {
                    itemView.cacheItemCheckBox.isChecked = checkedSet.contains(item)
                    itemView.cacheItemTitleTextView.text = context.getString(R.string.label_cache_item_title, AppUtils.coalesce(item.title))
                    itemView.cacheItemArtistTextView.text = context.getString(R.string.label_cache_item_artist, AppUtils.coalesce(item.artist))
                    if (item.has_lyrics == true) {
                        itemView.cacheItemHasLyricsImageView.visibility = View.VISIBLE
                    } else {
                        itemView.cacheItemHasLyricsImageView.visibility = View.GONE
                    }

                    // event
                    itemView.cacheItemCheckBox.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked)
                            checkedSet.add(item)
                        else
                            checkedSet.remove(item)
                    }
                }
            }
        }
    }

}
