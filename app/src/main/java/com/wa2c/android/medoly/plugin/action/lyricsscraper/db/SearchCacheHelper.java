package com.wa2c.android.medoly.plugin.action.lyricsscraper.db;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.ResultItem;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils;

import java.util.Collection;
import java.util.List;

/**
 * Search cache adapter.
 */

public class SearchCacheHelper {

    /** Singleton object. */
    private static OrmaDatabase ormaDatabase = null;

    /**
     * Provide OrmaDatabase.
     * @param context context.
     * @return OrmaDatabase object.
     */
    static synchronized OrmaDatabase provideOrmaDatabase(Context context) {
        if (ormaDatabase == null) {
            ormaDatabase = OrmaDatabase.builder(context).build();
        }
        return ormaDatabase;
    }



    /** Context. */
    private Context context;
    /** Gson. */
    private Gson gson;

    /**
     * Constructor.
     * @param context Context.
     */
    public SearchCacheHelper(@NonNull Context context) {
        this.context = context;
        this.gson = new Gson();
    }


    /**
     * Get ResultItem object from title and artist.
     * @param title Search title.
     * @param artist Search artist.
     * @return Exists item, null if not exists.
     */
    public SearchCache selectCache(String title, String artist) {
        OrmaDatabase od = provideOrmaDatabase(context);
        return od.selectFromSearchCache()
                .titleAndArtistEq(AppUtils.INSTANCE.coalesce(title), AppUtils.INSTANCE.coalesce(artist))
                .valueOrNull();
    }

    /**
     * Get Search Cache object matched on title and artist.
     * @param title Search title.
     * @param artist Search artist.
     * @return Exists item, null if not exists.
     */
    public List<SearchCache> searchCache(String title, String artist) {
        OrmaDatabase od = provideOrmaDatabase(context);
        SearchCache_Selector selector = od.selectFromSearchCache();
        // title
        if (title != null && !title.isEmpty())
            selector.where("title like ?", "%" + title + "%");
        // artist
        if (artist != null && !artist.isEmpty())
            selector.where("artist like ?", "%" + artist + "%");

        return selector.orderBytitleAndArtistAsc()
                .toList();
    }

    /**
     * Insert or update cache.
     * @param title Search title.
     * @param artist Search artist.
     * @param resultItem Result item.
     * @return true as succeeded.
     */
    public boolean insertOrUpdateCache(@NonNull String title, String artist, @Nullable ResultItem resultItem) {
        OrmaDatabase od = provideOrmaDatabase(context);

        String language = null;
        String from = null;
        String file_name = null;
        Boolean has_lyrics = false;
        String result = gson.toJson(resultItem);
        if (resultItem != null) {
            //language = resultItem.getLanguage();
            //from = resultItem.getLyricUploader();
            //file_name = resultItem.getLyricURL().substring(resultItem.getLyricURL().lastIndexOf("/") + 1).replace(".lrc", "");
            has_lyrics = (resultItem.getLyrics() != null);
        }

        title = AppUtils.INSTANCE.coalesce(title);
        artist = AppUtils.INSTANCE.coalesce(artist);

        SearchCache cache = selectCache(title, artist);
        if (cache != null) {
            int count = od.updateSearchCache()._idEq(cache._id)
                    //.language(language)
                    //.from(from)
                    //.file_name(file_name)
                    .has_lyrics(has_lyrics)
                    .result(result)
                    .execute();
            return (count > 0);
        } else {
            // Insert model
            cache = new SearchCache();
            cache.title = title;
            cache.artist = artist;
            cache.language = language;
            cache.from = from;
            cache.file_name =  file_name;
            cache.has_lyrics = has_lyrics;
            cache.result = result;
            long id = od.insertIntoSearchCache(cache);
            return (id >= 0);
        }
    }

    /**
     * Delete cache.
     * @param caches Cache.
     * @return true as succeeded.
     */
    public boolean deleteCache(final Collection<SearchCache> caches) {
        if (caches == null || caches.size() == 0)
            return false;

        final OrmaDatabase od = provideOrmaDatabase(context);

        od.transactionSync(new Runnable() {
            @Override
            public void run() {
                for (SearchCache c : caches) {
                    od.deleteFromSearchCache()
                            ._idEq(c._id)
                            .execute();
                }
            }
        });

        return true;
    }


    // Site / Group

    /**
     * Select a site by id.
     * @param siteId A site id.
     * @return A site.
     */
    public Site selectSite(Long siteId) {
        final OrmaDatabase od = provideOrmaDatabase(context);
        return od.selectFromSite().site_idEq(siteId).valueOrNull();
    }

    /**
     * Select all sites.
     * @return All sites.
     */
    public List<Site> selectSiteList() {
        final OrmaDatabase od = provideOrmaDatabase(context);
        return od.selectFromSite().orderBySite_idAsc().toList();
    }

    /**
     * Select sites by group id
     * @param groupId A group id.
     * @return Selected sits.
     */
    public List<Site> selectSiteListByGroupId(Long groupId) {
        final OrmaDatabase od = provideOrmaDatabase(context);
        return od.selectFromSite().where("group_id = ?", groupId).orderBySite_idAsc().toList();
    }

    /**
     * Select all site groups.
     * @return All site groups.
     */
    public List<SiteGroup> selectSiteGroupList() {
        OrmaDatabase od = provideOrmaDatabase(context);
        return od.selectFromSiteGroup().orderByGroup_idAsc().toList();
    }

    /**
     * Renew sites.
     * @param collection Insert sits.
     */
    public void renewSite(Collection<Site> collection) {
        final OrmaDatabase od = provideOrmaDatabase(context);
        od.deleteFromSite().execute();
        od.prepareInsertIntoSite().executeAll(collection);
    }

    /**
     * Renew site groups.
     * @param collection Insert site groups.
     */
    public void renewSiteGroup(Collection<SiteGroup> collection) {
        final OrmaDatabase od = provideOrmaDatabase(context);
        od.deleteFromSiteGroup().execute();
        od.prepareInsertIntoSiteGroup().executeAll(collection);
    }

}
