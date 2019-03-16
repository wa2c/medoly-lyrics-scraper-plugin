package com.wa2c.android.medoly.plugin.action.lyricsscraper.db

import android.content.Context

import com.google.gson.Gson
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.ResultItem
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils

/**
 * Search cache adapter.
 */

class DbHelper(private val context: Context) {
    /** Gson.  */
    private val gson: Gson = Gson()

    /**
     * Get ResultItem object from title and artist.
     * @param title Search title.
     * @param artist Search artist.
     * @return Exists item, null if not exists.
     */
    fun selectCache(title: String?, artist: String?): SearchCache? {
        val od = provideOrmaDatabase(context)
        return od.selectFromSearchCache()
                .titleAndArtistEq(AppUtils.coalesce(title), AppUtils.coalesce(artist))
                .valueOrNull()
    }

    /**
     * Get Search Cache object matched on title and artist.
     * @param title Search title.
     * @param artist Search artist.
     * @return Exists item, null if not exists.
     */
    fun searchCache(title: String?, artist: String?): List<SearchCache> {
        val od = provideOrmaDatabase(context)
        val selector = od.selectFromSearchCache()
        // title
        if (!title.isNullOrEmpty())
            selector.where("title like ?", "%$title%")
        // artist
        if (!artist.isNullOrEmpty())
            selector.where("artist like ?", "%$artist%")

        return selector.orderBytitleAndArtistAsc()
                .toList()
    }

    /**
     * Insert or update cache.
     * @param searchTitle Search title.
     * @param searchArtist Search artist.
     * @param resultItem Result item.
     * @return true as succeeded.
     */
    fun insertOrUpdateCache(searchTitle: String?, searchArtist: String?, resultItem: ResultItem?): Boolean {
        val od = provideOrmaDatabase(context)

        var hasLyrics: Boolean? = false
        val result = gson.toJson(resultItem)
        if (resultItem != null) {
            hasLyrics = resultItem.lyrics != null
        }

        val title = AppUtils.coalesce(searchTitle)
        val artist = AppUtils.coalesce(searchArtist)

        var cache = selectCache(title, artist)
        if (cache != null) {
            val count = od.updateSearchCache()._idEq(cache._id)
                    .has_lyrics(hasLyrics)
                    .result(result)
                    .execute()
            return count > 0
        } else {
            // Insert model
            cache = SearchCache(
                    0,
                    title,
                    artist,
                    null,
                    null,
                    null,
                    hasLyrics,
                    result,
                    null,
                    null
            )
//            cache.title = title
//            cache.artist = artist
//            cache.has_lyrics = hasLyrics
//            cache.result = result
            val id = od.insertIntoSearchCache(cache)
            return id >= 0
        }
    }

    /**
     * Delete cache.
     * @param caches Cache.
     * @return true as succeeded.
     */
    fun deleteCache(caches: Collection<SearchCache>?): Boolean {
        if (caches == null || caches.isEmpty())
            return false

        val od = provideOrmaDatabase(context)

        od.transactionSync {
            for (c in caches) {
                od.deleteFromSearchCache()
                        ._idEq(c._id)
                        .execute()
            }
        }

        return true
    }


    // Site / Group

    /**
     * Select a site by id.
     * @param siteId A site id.
     * @return A site.
     */
    fun selectSite(siteId: Long?): Site? {
        val od = provideOrmaDatabase(context)
        return od.selectFromSite().site_idEq(siteId!!).valueOrNull()
    }

    /**
     * Select all sites.
     * @return All sites.
     */
    fun selectSiteList(): List<Site> {
        val od = provideOrmaDatabase(context)
        return od.selectFromSite().orderBySite_idAsc().toList()
    }

    /**
     * Select sites by group id
     * @param groupId A group id.
     * @return Selected sits.
     */
    fun selectSiteListByGroupId(groupId: Long?): List<Site> {
        val od = provideOrmaDatabase(context)
        return od.selectFromSite().where("group_id = ?", groupId!!).orderBySite_idAsc().toList()
    }

    /**
     * Select all site groups.
     * @return All site groups.
     */
    fun selectSiteGroupList(): List<SiteGroup> {
        val od = provideOrmaDatabase(context)
        return od.selectFromSiteGroup().orderByGroup_idAsc().toList()
    }

    /**
     * Renew sites.
     * @param collection Insert sits.
     */
    fun renewSite(collection: Collection<Site>) {
        val od = provideOrmaDatabase(context)
        od.deleteFromSite().execute()
        od.prepareInsertIntoSite().executeAll(collection)
    }

    /**
     * Renew site groups.
     * @param collection Insert site groups.
     */
    fun renewSiteGroup(collection: Collection<SiteGroup>) {
        val od = provideOrmaDatabase(context)
        od.deleteFromSiteGroup().execute()
        od.prepareInsertIntoSiteGroup().executeAll(collection)
    }

    companion object {

        /** Singleton object.  */
        private var ormaDatabase: OrmaDatabase? = null

        /**
         * Provide OrmaDatabase.
         * @param context context.
         * @return OrmaDatabase object.
         */
        @Synchronized
        private fun provideOrmaDatabase(context: Context): OrmaDatabase {
            if (ormaDatabase == null) {
                ormaDatabase = OrmaDatabase.builder(context).build()
            }
            return ormaDatabase!!
        }
    }

}
