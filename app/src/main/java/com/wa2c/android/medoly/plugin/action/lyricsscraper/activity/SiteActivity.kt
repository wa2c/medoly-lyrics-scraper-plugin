package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity

import android.app.Activity
import android.app.LoaderManager
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.GroupColumn
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SiteColumn
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SiteProvider
import com.wa2c.android.medoly.plugin.action.lyricsscraper.service.SpreadSheetReadTask
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Prefs
import kotlinx.android.synthetic.main.activity_group.*
import kotlinx.android.synthetic.main.layout_site_item.view.*
import java.util.*


class SiteActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var cursorAdapter: SheetCursorAdapter

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
        prefs = Prefs(this)

        setContentView(R.layout.activity_group)
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
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
            if (cursorAdapter.cursorType == CURSOR_TYPE_GROUP) {
                openSiteList(view.tag as String)
            } else if (cursorAdapter.cursorType == CURSOR_TYPE_SITE) {
                prefs.putValue(R.string.prefkey_selected_site_id, view.tag as String)
                cursorAdapter.notifyDataSetChanged()
            }
        }
        listView.choiceMode = AbsListView.CHOICE_MODE_SINGLE
        listView.adapter = cursorAdapter

        // create load manager
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
        loaderManager.restartLoader(CURSOR_LOADER_ID, bundle, cursorLoaderCallbacks)
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

        private val prefs = Prefs(context)
        private val localeGroupNameCol: GroupColumn = GroupColumn.findLocaleColumn(Locale.getDefault())

        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            return View.inflate(context, R.layout.layout_site_item, null)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            if (cursorType == CURSOR_TYPE_GROUP) {
                view.siteSelectRadioButton.visibility = View.GONE
                view.siteLaunchImageButton.visibility = View.GONE

                val title = cursor.getString(cursor.getColumnIndexOrThrow(localeGroupNameCol.columnKey))
                view.tag = cursor.getString(cursor.getColumnIndexOrThrow(GroupColumn.GROUP_ID.columnKey))
                view.siteParamTitleTextView.text = title
                view.siteParamUriTextView.visibility = View.GONE
            } else {
                // ID
                val siteId = cursor.getString(cursor.getColumnIndexOrThrow(SiteColumn.SITE_ID.columnKey))
                view.tag = siteId

                // select
                view.siteSelectRadioButton.visibility = View.VISIBLE
                view.siteSelectRadioButton.isChecked = (siteId == prefs.getString(R.string.prefkey_selected_site_id, "-1"))
                view.siteSelectRadioButton.setOnClickListener {
                    prefs.putValue(R.string.prefkey_selected_site_id, siteId)
                    notifyDataSetChanged()
                }

                // title
                val title = cursor.getString(cursor.getColumnIndexOrThrow(SiteColumn.SITE_NAME.columnKey))
                view.siteParamTitleTextView.text = title
                view.siteParamTitleTextView.setOnTouchListener { _, event -> view.onTouchEvent(event) }

                // uri
                val uri = cursor.getString(cursor.getColumnIndexOrThrow(SiteColumn.SITE_URI.columnKey))
                view.siteParamUriTextView.visibility = View.VISIBLE
                view.siteParamUriTextView.text = uri
                view.siteLaunchImageButton.tag = uri
                view.siteLaunchImageButton.visibility = View.VISIBLE
                if (!uri.isNullOrEmpty()) {
                    view.siteLaunchImageButton.isEnabled = true
                    view.siteLaunchImageButton.setOnClickListener { v ->
                        val sheetUri = Uri.parse(v.tag as String)
                        val browserIntent = Intent(Intent.ACTION_VIEW, sheetUri)
                        context.startActivity(browserIntent)
                    }
                } else {
                    view.siteLaunchImageButton.isEnabled = false
                }
            }
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
