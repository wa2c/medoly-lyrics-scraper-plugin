package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.wa2c.android.medoly.plugin.action.lyricsscraper.BuildConfig
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.databinding.ActivityGroupBinding
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.DbHelper
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.Site
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SiteGroup
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.prefs.Prefs
import de.siegmar.fastcsv.reader.CsvReader
import kotlinx.android.synthetic.main.layout_site_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL


/**
 * Site activity
 */
class SiteActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var binding: ActivityGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group)
        prefs = Prefs(this)

        binding.groupListView.visibility = View.VISIBLE
        binding.loadingLayout.visibility = View.INVISIBLE

        // action bar
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)

        binding.groupListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val item = binding.groupListView.adapter.getItem(position)
            if (binding.groupListView.adapter is SiteGroupListAdapter) {
                openSiteList((item as SiteGroup).group_id)
            } else if (binding.groupListView.adapter is SiteListAdapter) {
                prefs.putLong(R.string.prefkey_selected_site_id, (item as Site).site_id)
                (binding.groupListView.adapter as SiteListAdapter).notifyDataSetChanged()
            }
        }
        binding.groupListView.choiceMode = AbsListView.CHOICE_MODE_SINGLE
        binding.groupListView.adapter = SiteGroupListAdapter(this)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.site_list, menu)
        for (i in 0 until menu.size()) {
            menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // home
                if (binding.groupListView.adapter is SiteListAdapter) {
                    openGroupList()
                } else {
                    finish()
                }
                return true
            }
            R.id.menu_update_list -> {
                GlobalScope.launch(Dispatchers.Main) {
                    val sheetId = getString(if (BuildConfig.DEBUG) R.string.sheet_id_debug else R.string.sheet_id)
                    val siteResult = async(coroutineContext + Dispatchers.Default) {
                        downloadSiteList(sheetId)
                    }
                    if (!siteResult.await()) {
                        AppUtils.showToast(applicationContext, R.string.message_renew_list_failed)
                        return@launch
                    }

                    val groupResult = async (coroutineContext + Dispatchers.Default) {
                        downloadGroupList(sheetId)
                    }
                    if (!groupResult.await()) {
                        AppUtils.showToast(applicationContext, R.string.message_renew_list_failed)
                        return@launch
                    }

                    openGroupList()
                    AppUtils.showToast(applicationContext, R.string.message_renew_list_succeeded)

                    binding.groupListView.visibility = View.VISIBLE
                    binding.loadingLayout.visibility = View.INVISIBLE
                }

                binding.groupListView.visibility = View.INVISIBLE
                binding.loadingLayout.visibility = View.VISIBLE
                return true
            }
            R.id.menu_open_sheet -> {
                val sheetId = getString(if (BuildConfig.DEBUG) R.string.sheet_id_debug else R.string.sheet_id)
                val sheetUri = Uri.parse(getString(R.string.sheet_uri, sheetId))
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
            KeyEvent.KEYCODE_BACK -> if (binding.groupListView.adapter is SiteListAdapter) {
                openGroupList()
                return true
            }
        }
        return super.dispatchKeyEvent(e)
    }



    /**
     * Download site list.
     */
    private fun downloadSiteList(sheetId: String): Boolean {
        var con: HttpURLConnection? = null
        try {
            val siteGid = getString(if (BuildConfig.DEBUG) R.string.sheet_site_gid_debug else R.string.sheet_site_gid)
            val siteExportUrl = getString(R.string.sheet_export_uri, sheetId, siteGid)
            val siteUrl = URL(siteExportUrl)
            con = siteUrl.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.doInput = true
            con.connect()
            con.inputStream.bufferedReader(Charsets.UTF_8).use {
                try {
                    val csvReader = CsvReader()
                    csvReader.setContainsHeader(true)
                    val csv = csvReader.read(it)
                    val siteList = mutableListOf<Site>()
                    for (row in csv.rows) {
                        val site = Site(
                                0,
                                row.getField("SITE_ID").toLong(),
                                row.getField("GROUP_ID").toLong(),
                                row.getField("SITE_NAME"),
                                row.getField("SITE_URI"),
                                row.getField("SEARCH_URI"),
                                row.getField("RESULT_PAGE_URI_ENCODING"),
                                row.getField("RESULT_PAGE_ENCODING"),
                                row.getField("RESULT_PAGE_PARSE_TYPE"),
                                row.getField("RESULT_PAGE_PARSE_TEXT"),
                                row.getField("LYRICS_PAGE_ENCODING"),
                                row.getField("LYRICS_PAGE_PARSE_TYPE"),
                                row.getField("LYRICS_PAGE_PARSE_TEXT"),
                                row.getField("DELAY").toLong(),
                                row.getField("TIMEOUT").toLong()
                        )
//                        val site = Site()
//                        site.site_id = row.getField("SITE_ID").toLong()
//                        site.group_id = row.getField("GROUP_ID").toLong()
//                        site.site_name = row.getField("SITE_NAME")
//                        site.site_uri = row.getField("SITE_URI")
//                        site.search_uri = row.getField("SEARCH_URI")
//                        site.result_page_uri_encoding = row.getField("RESULT_PAGE_URI_ENCODING")
//                        site.result_page_encoding = row.getField("RESULT_PAGE_ENCODING")
//                        site.result_page_parse_type = row.getField("RESULT_PAGE_PARSE_TYPE")
//                        site.result_page_parse_text = row.getField("RESULT_PAGE_PARSE_TEXT")
//                        site.lyrics_page_encoding = row.getField("LYRICS_PAGE_ENCODING")
//                        site.lyrics_page_parse_type = row.getField("LYRICS_PAGE_PARSE_TYPE")
//                        site.lyrics_page_parse_text = row.getField("LYRICS_PAGE_PARSE_TEXT")
//                        site.delay = row.getField("DELAY").toLong()
//                        site.timeout = row.getField("TIMEOUT").toLong()
                        siteList.add(site)
                    }
                    DbHelper(this@SiteActivity).renewSite(siteList)
                } catch (ex: Exception) {
                    Timber.d(ex)
                }
            }
            return true
        }  catch (e: Exception) {
            Timber.d(e)
            return false
        } finally {
            con?.disconnect()
        }
    }

    /**
     * Download group list.
     */
    private fun downloadGroupList(sheetId: String): Boolean {
        var con: HttpURLConnection? = null
        try {
            val groupGid = getString(if (BuildConfig.DEBUG) R.string.sheet_group_gid_debug else R.string.sheet_group_gid)
            val groupUri = getString(R.string.sheet_export_uri, sheetId, groupGid)
            val siteUrl = URL(groupUri)
            con = siteUrl.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.doInput = true
            con.connect()
            con.inputStream.bufferedReader(Charsets.UTF_8).use {
                try {
                    val csvReader = CsvReader()
                    csvReader.setContainsHeader(true)
                    val csv = csvReader.read(it)
                    val groupList = mutableListOf<SiteGroup>()
                    for (row in csv.rows) {
                        val group = SiteGroup(
                                0,
                                row.getField("GROUP_ID").toLong(),
                                row.getField("NAME"),
                                row.getField("NAME_JA")
                        )
//                        val group = SiteGroup()
//                        group.group_id = row.getField("GROUP_ID").toLong()
//                        group.name = row.getField("NAME")
//                        group.name_ja = row.getField("NAME_JA")
                        groupList.add(group)
                    }
                    DbHelper(this@SiteActivity).renewSiteGroup(groupList)
                } catch (ex: Exception) {
                    Timber.d(ex)
                }
            }
            return true
        }  catch (e: Exception) {
            Timber.d(e)
            return false
        } finally {
            con?.disconnect()
        }
    }

    /**
     * Open site list.
     */
    private fun openSiteList(groupId: Long) {
        binding.groupListView.adapter = SiteListAdapter(this, groupId)
    }

    /**
     * Open group list.
     */
    private fun openGroupList() {
        binding.groupListView.adapter = SiteGroupListAdapter(this)
    }



    companion object {
//        private const val SITE_SHEET_NAME = "SITE"
//        private const val GROUP_SHEET_NAME = "GROUP"

        /**
         * Site group adapter.
         */
        private class SiteGroupListAdapter internal constructor(context: Context) : ArrayAdapter<SiteGroup>(context, R.layout.layout_site_item) {
            init {
                addAll(DbHelper(context).selectSiteGroupList())
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
                addAll(DbHelper(context).selectSiteListByGroupId(groupId))
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
    }

}
