//package com.wa2c.android.medoly.plugin.action.lyricsscraper.service
//
//import android.content.ContentProviderOperation
//import android.content.ContentProviderResult
//import android.content.ContentResolver
//import android.content.ContentValues
//import android.content.Context
//import android.os.AsyncTask
//
//import com.google.gdata.client.spreadsheet.FeedURLFactory
//import com.google.gdata.client.spreadsheet.ListQuery
//import com.google.gdata.client.spreadsheet.SpreadsheetService
//import com.google.gdata.data.spreadsheet.ListEntry
//import com.google.gdata.data.spreadsheet.ListFeed
//import com.google.gdata.data.spreadsheet.WorksheetEntry
//import com.google.gdata.data.spreadsheet.WorksheetFeed
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.BuildConfig
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.*
//import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger
//
//import java.net.URL
//import java.util.ArrayList
//import java.util.EventListener
//
//
///**
// * Spread sheet reading task class.
// */
//class SpreadSheetReadTask(private val context: Context) : AsyncTask<String, Void, Boolean>() {
//    private val service: SpreadsheetService = SpreadsheetService(context.getString(R.string.app_name))
//    private val resolver: ContentResolver = context.contentResolver
//
//    /** Event listener.  */
//    private var actionListener: SiteUpdateListener? = null
//
//    override fun doInBackground(vararg params: String): Boolean? {
//        try {
//
//            val sheetId = context.getString(R.string.sheet_id)
//            //if (BuildConfig.DEBUG)
//            //    sheetId = context.getString(R.string.sheet_id_debug);
//
//            val feedURL = FeedURLFactory.getDefault().getWorksheetFeedUrl(sheetId, "public", "values")
//            val feed = service.getFeed<WorksheetFeed>(feedURL, WorksheetFeed::class.java)
//            val worksheetList = feed.entries
//
//            var result = 1
//            for (entry in worksheetList) {
//                val query = ListQuery(entry.listFeedUrl)
//                val listFeed = service.query<ListFeed>(query, ListFeed::class.java)
//
//                val title = entry.title.plainText
//                if (title == SITE_SHEET_NAME) {
//                    result *= writeSiteTable(listFeed)
//                } else if (title == GROUP_SHEET_NAME) {
//                    result *= writeGroupTable(listFeed)
//                }
//            }
//
//            return result > 0
//        } catch (e: Exception) {
//            Logger.e(e)
//            return false
//        }
//
//    }
//
//
//    private fun writeSiteTable(listFeed: ListFeed): Int {
//        try {
//            val operationList = ArrayList<ContentProviderOperation>()
//
//            // delete
//            operationList.add(ContentProviderOperation
//                    .newDelete(SiteProvider.SITE_URI)
//                    .build())
//
//            val siteList = mutableListOf<Site>()
//
//            // insert
//            val list = listFeed.entries
//            for (row in list) {
//                val values = ContentValues()
//
//                for (col in SiteColumn.values()) {
//                    val `val` = row.customElements.getValue(col.columnKey)
//                    values.put(col.columnKey, `val`)
//                }
//                operationList.add(ContentProviderOperation
//                        .newInsert(SiteProvider.SITE_URI)
//                        .withValues(values)
//                        .build())
//
//                val site = Site()
//                site.site_id = row.customElements.getValue("siteid").toLong()
//                site.group_id = row.customElements.getValue("groupid").toLong()
//                site.site_name = row.customElements.getValue("sitename")
//                site.site_uri = row.customElements.getValue("siteuri")
//                site.search_uri = row.customElements.getValue("searchuri")
//                site.result_page_uri_encoding = row.customElements.getValue("resultpageuriencoding")
//                site.result_page_encoding = row.customElements.getValue("resultpageencoding")
//                site.result_page_parse_type = row.customElements.getValue("resultpageparsetype")
//                site.result_page_parse_text = row.customElements.getValue("resultpageparsetext")
//                site.lyrics_page_encoding = row.customElements.getValue("lyricspageencoding")
//                site.lyrics_page_parse_type = row.customElements.getValue("lyricspageparsetype")
//                site.lyrics_page_parse_text = row.customElements.getValue("lyricspageparsetext")
//                site.delay = row.customElements.getValue("delay").toLong()
//                site.timeout = row.customElements.getValue("timeout").toLong()
//                siteList.add(site)
//            }
//
//            SearchCacheHelper(context).renewSite(siteList)
//
//            // apply
//            val results = resolver.applyBatch(SiteProvider.SITE_URI.authority, operationList)
//            return results.size
//        } catch (e: Exception) {
//            Logger.e(e)
//            return -1
//        }
//
//    }
//
//    private fun writeGroupTable(listFeed: ListFeed): Int {
//        try {
//            val operationList = ArrayList<ContentProviderOperation>()
//
//            // delete
//            operationList.add(ContentProviderOperation
//                    .newDelete(SiteProvider.GROUP_URI)
//                    .build())
//
//            val lll = mutableListOf<SiteGroup>()
//
//            // insert
//            val list = listFeed.entries
//            for (row in list) {
//                val values = ContentValues()
//
//                for (col in GroupColumn.values()) {
//                    val `val` = row.customElements.getValue(col.columnKey)
//                    values.put(col.columnKey, `val`)
//                }
//                operationList.add(ContentProviderOperation
//                        .newInsert(SiteProvider.GROUP_URI)
//                        .withValues(values)
//                        .build())
//
//                val ggg = SiteGroup()
//                ggg.group_id = row.customElements.getValue("groupid").toLong()
//                ggg.name = row.customElements.getValue("name")
//                ggg.name_ja = row.customElements.getValue("nameja")
//                lll.add(ggg)
//            }
//
//            val v = SearchCacheHelper(context)
//            v.renewSiteGroup(lll)
//
//            // apply
//            val results = resolver.applyBatch(SiteProvider.GROUP_URI.authority, operationList)
//            return results.size
//        } catch (e: Exception) {
//            Logger.e(e)
//            return -1
//        }
//
//    }
//
//    override fun onPostExecute(result: Boolean?) {
//        if (actionListener != null) {
//            actionListener!!.onListUpdated(result!!)
//        }
//    }
//
//    // Event Listener
//
//    /**
//     * Event listener class.
//     */
//    interface SiteUpdateListener : EventListener {
//        fun onListUpdated(isSucceeded: Boolean)
//    }
//
//    /**
//     * Set event listener.
//     * @param listener event listener.
//     */
//    fun setOnPropertyActionListener(listener: SiteUpdateListener) {
//        this.actionListener = listener
//    }
//
//    companion object {
//        private val SITE_SHEET_NAME = "SITE"
//        private val GROUP_SHEET_NAME = "GROUP"
//    }
//}
