package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity

import android.app.ActionBar
import android.app.Activity
import android.app.LoaderManager
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.CursorAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.RadioButton
import android.widget.TextView

import com.wa2c.android.medoly.plugin.action.lyricsscraper.BuildConfig
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.GroupColumn
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SiteColumn
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SiteProvider
import com.wa2c.android.medoly.plugin.action.lyricsscraper.service.SpreadSheetReadTask
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Prefs
import kotlinx.android.synthetic.main.activity_group.*

import java.util.Locale


class SiteActivity : Activity() {

//    /** Preferences.  */
//    private var preference: SharedPreferences? = null

    private lateinit var prefs: Prefs

    /** Cursor Adapter.  */
    private lateinit var cursorAdapter: SheetCursorAdapter
    /** Loader manager.  */
//    private var loaderManager: LoaderManager? = null

//    private var listLayout: ViewGroup? = null
//
//    private var loadingLayout: ViewGroup? = null


    /**
     * Cursor loader.
     */
    private val cursorLoaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor>? {
            if (args != null) {
                val type = args.getInt(CURSOR_TYPE, CURSOR_TYPE_GROUP)
                if (type == CURSOR_TYPE_GROUP) {
                    return CursorLoader(
                            this@SiteActivity,
                            SiteProvider.GROUP_URI, null, null, null,
                            GroupColumn.NAME.columnKey)
                } else if (type == CURSOR_TYPE_SITE) {
                    val groupId = args.getString(GROUP_ID, "-1")
                    return CursorLoader(
                            this@SiteActivity,
                            SiteProvider.SITE_URI, null,
                            SiteColumn.GROUP_ID.columnKey + "=?",
                            arrayOf(groupId),
                            SiteColumn.SITE_NAME.columnKey)
                }
            }
            return null
        }

        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
            if (loader.isReset || loader.isAbandoned) return
            cursorAdapter.swapCursor(data)
            cursorAdapter.notifyDataSetChanged()
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            cursorAdapter.swapCursor(null)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //this.preference = PreferenceManager.getDefaultSharedPreferences(this)
        prefs = Prefs(this)


        setContentView(R.layout.activity_group)
//        listLayout = findViewById<View>(R.id.groupListView) as ViewGroup
//        loadingLayout = findViewById<View>(R.id.loadingLayout) as ViewGroup
        groupListView.visibility = View.VISIBLE
        loadingLayout.visibility = View.INVISIBLE

        // action bar
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)

        // create loader manager
        val bundle = Bundle()
        bundle.putInt(CURSOR_TYPE, CURSOR_TYPE_GROUP)

        // create view
        cursorAdapter = SheetCursorAdapter(this, CURSOR_TYPE_GROUP)
        val listView = findViewById<View>(R.id.groupListView) as ListView
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val holder = view.tag as SheetCursorAdapter.ListViewHolder
            if (cursorAdapter.cursorType == CURSOR_TYPE_GROUP) {
                openSiteList(holder.GroupId)
            } else if (cursorAdapter.cursorType == CURSOR_TYPE_SITE) {
                //preference!!.edit().putString(getString(R.string.prefkey_selected_site_id), holder.SiteId).apply()
                prefs.putValue(R.string.prefkey_selected_site_id, holder.SiteId)
                cursorAdapter.notifyDataSetChanged()
            }
        }
        listView.choiceMode = AbsListView.CHOICE_MODE_SINGLE
        listView.adapter = cursorAdapter

        // create load manager
//        loaderManager = this.getLoaderManager()
        loaderManager.restartLoader(CURSOR_LOADER_ID, bundle, cursorLoaderCallbacks)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.site_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // home
                if (cursorAdapter.cursorType == CURSOR_TYPE_SITE) {
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
                        val bundle = Bundle()
                        bundle.putInt(CURSOR_TYPE, CURSOR_TYPE_GROUP)
                        loaderManager.restartLoader(CURSOR_LOADER_ID, bundle, cursorLoaderCallbacks)
                        groupListView.visibility = View.VISIBLE
                        loadingLayout.visibility = View.INVISIBLE
                    }
                })

//                task.setOnPropertyActionListener { isSucceeded ->
//                    if (isSucceeded) {
//                        openGroupList()
//                        AppUtils.showToast(applicationContext, R.string.message_renew_list_succeeded)
//                    } else {
//                        AppUtils.showToast(applicationContext, R.string.message_renew_list_failed)
//                    }
//                    val bundle = Bundle()
//                    bundle.putInt(CURSOR_TYPE, CURSOR_TYPE_GROUP)
//                    loaderManager!!.restartLoader(CURSOR_LOADER_ID, bundle, cursorLoaderCallbacks)
//                    groupListView!!.visibility = View.VISIBLE
//                    loadingLayout!!.visibility = View.INVISIBLE
//                }
                groupListView.visibility = View.INVISIBLE
                loadingLayout.visibility = View.VISIBLE
                loaderManager.destroyLoader(CURSOR_LOADER_ID)
                task.execute()
                return true
            }
            R.id.menu_open_sheet -> {
                val sheetUrl = getString(R.string.sheet_uri, getString(R.string.sheet_id))
                //if (BuildConfig.DEBUG)
                //    sheetUrl = getString(R.string.sheet_uri, getString(R.string.sheet_id_debug));
                val sheetUri = Uri.parse(sheetUrl)
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
            KeyEvent.KEYCODE_BACK -> if (cursorAdapter.cursorType == CURSOR_TYPE_SITE) {
                openGroupList()
                return true
            }
        }
        return super.dispatchKeyEvent(e)
    }

    /**
     * Open site list.
     * @param groupId Group ID.
     */
    private fun openSiteList(groupId: String?) {
        cursorAdapter.cursorType = CURSOR_TYPE_SITE
        val bundle = Bundle()
        bundle.putInt(CURSOR_TYPE, CURSOR_TYPE_SITE)
        bundle.putString(GROUP_ID, groupId)
        loaderManager!!.restartLoader(CURSOR_LOADER_ID, bundle, cursorLoaderCallbacks)
    }

    /**
     * Open group list.
     */
    private fun openGroupList() {
        cursorAdapter.cursorType = CURSOR_TYPE_GROUP
        val bundle = Bundle()
        bundle.putInt(CURSOR_TYPE, CURSOR_TYPE_GROUP)
        loaderManager.restartLoader(CURSOR_LOADER_ID, bundle, cursorLoaderCallbacks)
    }


    /**
     * Spreadsheet data cursor adapter.
     */
    private class SheetCursorAdapter internal constructor(context: Activity, internal var cursorType: Int) : CursorAdapter(context, null, true) {

//        private val preference: SharedPreferences
        private val prefs = Prefs(context)

        private val localeGroupNameCol: GroupColumn = GroupColumn.findLocaleColumn(Locale.getDefault())

//        init {
//            this.preference = PreferenceManager.getDefaultSharedPreferences(context)
//            this.localeGroupNameCol = GroupColumn.findLocaleColumn(Locale.getDefault())
//        }

        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            val view = View.inflate(context, R.layout.layout_site_item, null)
            val holder = ListViewHolder()
            holder.SelectRadioButton = view.findViewById<View>(R.id.siteSelectRadioButton) as RadioButton
            holder.TitleTextView = view.findViewById<View>(R.id.siteParamTitleTextView) as TextView
            holder.UriTextView = view.findViewById<View>(R.id.siteParamUriTextView) as TextView
            holder.LaunchImageButton = view.findViewById<View>(R.id.siteLaunchImageButton) as ImageButton
            view.tag = holder
            return view
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            val holder = view.tag as ListViewHolder
            if (cursorType == CURSOR_TYPE_GROUP) {
                holder.SelectRadioButton!!.visibility = View.GONE
                holder.LaunchImageButton!!.visibility = View.GONE

                val title = cursor.getString(cursor.getColumnIndexOrThrow(localeGroupNameCol.columnKey))
                holder.GroupId = cursor.getString(cursor.getColumnIndexOrThrow(GroupColumn.GROUP_ID.columnKey))
                holder.TitleTextView!!.text = title
                holder.UriTextView!!.visibility = View.GONE

            } else {
                // ID
                holder.GroupId = cursor.getString(cursor.getColumnIndexOrThrow(SiteColumn.GROUP_ID.columnKey))
                holder.SiteId = cursor.getString(cursor.getColumnIndexOrThrow(SiteColumn.SITE_ID.columnKey))

                // select
                holder.SelectRadioButton!!.visibility = View.VISIBLE
                //if (holder.SiteId == preference.getString(context.getString(R.string.prefkey_selected_site_id), "-1")) {
                if (holder.SiteId == prefs.getString(R.string.prefkey_selected_site_id, "-1")) {
                    holder.SelectRadioButton!!.isChecked = true
                } else {
                    holder.SelectRadioButton!!.isChecked = false
                }
                holder.SelectRadioButton!!.setOnClickListener {
                    //preference.edit().putString(context.getString(R.string.prefkey_selected_site_id), holder.SiteId).apply()
                    prefs.putValue(R.string.prefkey_selected_site_id, holder.SiteId)
                    notifyDataSetChanged()
                }

                // title
                val title = cursor.getString(cursor.getColumnIndexOrThrow(SiteColumn.SITE_NAME.columnKey))
                holder.TitleTextView!!.text = title
                holder.TitleTextView!!.setOnTouchListener { _, event -> view.onTouchEvent(event) }

                // uri
                val uri = cursor.getString(cursor.getColumnIndexOrThrow(SiteColumn.SITE_URI.columnKey))
                holder.UriTextView!!.visibility = View.VISIBLE
                holder.UriTextView!!.text = uri
                holder.LaunchImageButton!!.tag = uri
                holder.LaunchImageButton!!.visibility = View.VISIBLE
                if (!TextUtils.isEmpty(uri)) {
                    holder.LaunchImageButton!!.isEnabled = true
                    holder.LaunchImageButton!!.setOnClickListener { v ->
                        val sheetUri = Uri.parse(v.tag as String)
                        val browserIntent = Intent(Intent.ACTION_VIEW, sheetUri)
                        context.startActivity(browserIntent)
                    }
                } else {
                    holder.LaunchImageButton!!.isEnabled = false
                }
            }
        }

        /**
         * ViewHolder for QueueListView.
         */
        class ListViewHolder {
            internal var GroupId: String? = null
            internal var SiteId: String? = null

            internal var SelectRadioButton: RadioButton? = null
            internal var TitleTextView: TextView? = null
            internal var UriTextView: TextView? = null
            internal var LaunchImageButton: ImageButton? = null
        }
    }

    companion object {

        /** Cursor loader ID.  */
        private const val CURSOR_LOADER_ID = 1
        /** Cursor type.  */
        private const val CURSOR_TYPE = "cursor_type"
        /** Cursor type: site.  */
        private const val CURSOR_TYPE_SITE = 0
        /** Cursor type: group.  */
        private const val CURSOR_TYPE_GROUP = 1
        /** Group ID.  */
        private const val GROUP_ID = "group_id"
    }

}
