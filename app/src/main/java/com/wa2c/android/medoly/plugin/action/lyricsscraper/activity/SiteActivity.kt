package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.gdata.client.spreadsheet.FeedURLFactory
import com.google.gdata.client.spreadsheet.ListQuery
import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet.ListFeed
import com.google.gdata.data.spreadsheet.WorksheetFeed
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCacheHelper
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.Site
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SiteGroup
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Prefs
import kotlinx.android.synthetic.main.activity_group.*
import kotlinx.android.synthetic.main.layout_site_item.view.*
import java.util.*


/**
 * Site activity
 */
class SiteActivity : Activity() {

    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)

        setContentView(R.layout.activity_group)
        groupListView.visibility = View.VISIBLE
        loadingLayout.visibility = View.INVISIBLE

        // action bar
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)

        groupListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val item = groupListView.adapter.getItem(position)
            if (groupListView.adapter is SiteGroupListAdapter) {
                openSiteList((item as SiteGroup).group_id)
            } else if (groupListView.adapter is SiteListAdapter) {
                prefs.putValue(R.string.prefkey_selected_site_id, (item as Site).site_id)
                (groupListView.adapter as SiteListAdapter).notifyDataSetChanged()
            }
        }
        groupListView.choiceMode = AbsListView.CHOICE_MODE_SINGLE
        groupListView.adapter = SiteGroupListAdapter(this)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.site_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // home
                if (groupListView.adapter is SiteListAdapter) {
                    openGroupList()
                } else {
                    finish()
                }
                return true
            }
            R.id.menu_update_list -> {
                // update list
                val task = SpreadSheetReadTask(applicationContext)
                task.setOnPropertyActionListener(object: SpreadSheetReadTask.SiteUpdateListener {
                    override fun onListUpdated(isSucceeded: Boolean) {
                        if (isSucceeded) {
                            openGroupList()
                            AppUtils.showToast(applicationContext, R.string.message_renew_list_succeeded)
                        } else {
                            AppUtils.showToast(applicationContext, R.string.message_renew_list_failed)
                        }
                        groupListView.visibility = View.VISIBLE
                        loadingLayout.visibility = View.INVISIBLE
                    }
                })

                groupListView.visibility = View.INVISIBLE
                loadingLayout.visibility = View.VISIBLE
                task.execute()
                return true
            }
            R.id.menu_open_sheet -> {
                val sheetUri = Uri.parse(getString(R.string.sheet_uri, getString(R.string.sheet_id)))
                val browserIntent = Intent(Intent.ACTION_VIEW, sheetUri)
                startActivity(browserIntent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        when (e.keyCode) {
            // back key
            KeyEvent.KEYCODE_BACK -> if (groupListView.adapter is SiteListAdapter) {
                openGroupList()
                return true
            }
        }
        return super.dispatchKeyEvent(e)
    }

    private fun openSiteList(groupId: Long) {
        groupListView.adapter = SiteListAdapter(this, groupId)
    }

    private fun openGroupList() {
        groupListView.adapter = SiteGroupListAdapter(this)
    }



    companion object {
        private const val SITE_SHEET_NAME = "SITE"
        private const val GROUP_SHEET_NAME = "GROUP"

        /**
         * Site group adapter.
         */
        private class SiteGroupListAdapter internal constructor(context: Context) : ArrayAdapter<SiteGroup>(context, R.layout.layout_site_item) {
            init {
                addAll(SearchCacheHelper(context).selectSiteGroupList())
            }

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

                holder.bind(getItem(position))

                return itemView
            }

            /** List item view holder.  */
            private class ListItemViewHolder(context: Context) {
                val itemView = View.inflate(context, R.layout.layout_site_item, null)!!

                init {
                    itemView.tag = this
                }

                fun bind(item: SiteGroup) {
                    itemView.siteSelectRadioButton.visibility = View.GONE
                    itemView.siteLaunchImageButton.visibility = View.GONE
                    itemView.siteParamUriTextView.visibility = View.GONE
                    itemView.siteParamTitleTextView.text = item.findLocaleName(null)
                }
            }
        }

        /**
         * Site adapter.
         */
        private class SiteListAdapter internal constructor(context: Context, groupId: Long) : ArrayAdapter<Site>(context, R.layout.layout_site_item) {

            init {
                addAll(SearchCacheHelper(context).selectSiteListByGroupId(groupId))
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var itemView = convertView
                val holder: ListItemViewHolder
                if (itemView == null) {
                    holder = ListItemViewHolder(parent.context)
                    itemView = holder.itemView
                } else {
                    holder = itemView.tag as ListItemViewHolder
                }

                holder.bind(getItem(position))

                return itemView
            }

            /** List item view holder.  */
            private class ListItemViewHolder(val context: Context) {
                val prefs = Prefs(context)
                val itemView = View.inflate(context, R.layout.layout_site_item, null)!!

                init {
                    itemView.tag = this
                }

                fun bind(site: Site) {
                    itemView.siteSelectRadioButton.visibility = View.VISIBLE
                    itemView.siteLaunchImageButton.visibility = View.VISIBLE
                    itemView.siteParamUriTextView.visibility = View.VISIBLE

                    itemView.siteSelectRadioButton.isChecked = (site.site_id == prefs.getLong(R.string.prefkey_selected_site_id))
                    itemView.siteSelectRadioButton.setOnTouchListener { _, event -> itemView.onTouchEvent(event) }
                    itemView.siteSelectRadioButton.isClickable = false

                    itemView.siteParamTitleTextView.text = site.site_name
                    itemView.siteParamUriTextView.text = site.site_uri
                    itemView.siteLaunchImageButton.tag = site.site_uri
                    if (!site.site_uri.isNullOrEmpty()) {
                        itemView.siteLaunchImageButton.isEnabled = true
                        itemView.siteLaunchImageButton.setOnClickListener { v ->
                            val sheetUri = Uri.parse(v.tag as String)
                            val browserIntent = Intent(Intent.ACTION_VIEW, sheetUri)
                            context.startActivity(browserIntent)
                        }
                    } else {
                        itemView.siteLaunchImageButton.isEnabled = false
                    }
                }
            }
        }

        /**
         * Spread sheet reading task class.
         */
        class SpreadSheetReadTask(context: Context) : AsyncTask<String, Void, Boolean>() {
            private val service: SpreadsheetService = SpreadsheetService(context.getString(R.string.app_name))
            private val db = SearchCacheHelper(context)
            private val sheetId = context.getString(R.string.sheet_id)
            //if (BuildConfig.DEBUG)
            //    sheetId = context.getString(R.string.sheet_id_debug);

            /** Event listener.  */
            private var actionListener: SiteUpdateListener? = null

            override fun doInBackground(vararg params: String): Boolean? {
                try {
                    val feedURL = FeedURLFactory.getDefault().getWorksheetFeedUrl(sheetId, "public", "values")
                    val feed = service.getFeed<WorksheetFeed>(feedURL, WorksheetFeed::class.java)
                    val worksheetList = feed.entries

                    var result = 1
                    for (entry in worksheetList) {
                        val query = ListQuery(entry.listFeedUrl)
                        val listFeed = service.query<ListFeed>(query, ListFeed::class.java)

                        val title = entry.title.plainText
                        if (title == SITE_SHEET_NAME) {
                            result *= writeSiteTable(listFeed)
                        } else if (title == GROUP_SHEET_NAME) {
                            result *= writeGroupTable(listFeed)
                        }
                    }

                    return result > 0
                } catch (e: Exception) {
                    Logger.e(e)
                    return false
                }

            }

            private fun writeSiteTable(listFeed: ListFeed): Int {
                try {
                    val siteList = mutableListOf<Site>()

                    val list = listFeed.entries
                    for (row in list) {
                        val site = Site()
                        site.site_id = row.customElements.getValue("siteid").toLong()
                        site.group_id = row.customElements.getValue("groupid").toLong()
                        site.site_name = row.customElements.getValue("sitename")
                        site.site_uri = row.customElements.getValue("siteuri")
                        site.search_uri = row.customElements.getValue("searchuri")
                        site.result_page_uri_encoding = row.customElements.getValue("resultpageuriencoding")
                        site.result_page_encoding = row.customElements.getValue("resultpageencoding")
                        site.result_page_parse_type = row.customElements.getValue("resultpageparsetype")
                        site.result_page_parse_text = row.customElements.getValue("resultpageparsetext")
                        site.lyrics_page_encoding = row.customElements.getValue("lyricspageencoding")
                        site.lyrics_page_parse_type = row.customElements.getValue("lyricspageparsetype")
                        site.lyrics_page_parse_text = row.customElements.getValue("lyricspageparsetext")
                        site.delay = row.customElements.getValue("delay").toLong()
                        site.timeout = row.customElements.getValue("timeout").toLong()
                        siteList.add(site)
                    }

                    db.renewSite(siteList)

                    return siteList.size
                } catch (e: Exception) {
                    Logger.e(e)
                    return -1
                }

            }

            private fun writeGroupTable(listFeed: ListFeed): Int {
                try {

                    val groupList = mutableListOf<SiteGroup>()

                    // insert
                    val list = listFeed.entries
                    for (row in list) {
                        val g = SiteGroup()
                        g.group_id = row.customElements.getValue("groupid").toLong()
                        g.name = row.customElements.getValue("name")
                        g.name_ja = row.customElements.getValue("nameja")
                        groupList.add(g)
                    }

                    db.renewSiteGroup(groupList)
                    return groupList.size
                } catch (e: Exception) {
                    Logger.e(e)
                    return -1
                }

            }

            override fun onPostExecute(result: Boolean?) {
                if (actionListener != null) {
                    actionListener!!.onListUpdated(result!!)
                }
            }

            // Event Listener

            /**
             * Event listener class.
             */
            interface SiteUpdateListener : EventListener {
                fun onListUpdated(isSucceeded: Boolean)
            }

            /**
             * Set event listener.
             * @param listener event listener.
             */
            fun setOnPropertyActionListener(listener: SiteUpdateListener) {
                this.actionListener = listener
            }
        }

    }
}
