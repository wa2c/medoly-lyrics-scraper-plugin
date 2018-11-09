package com.wa2c.android.medoly.plugin.action.lyricsscraper.service

import android.content.Intent
import com.wa2c.android.medoly.library.MediaProperty
import com.wa2c.android.medoly.plugin.action.lyricsscraper.activity.SearchActivity
import timber.log.Timber


/**
 * Download intent service.
 */
class PluginRunService : AbstractPluginService(PluginRunService::class.java.simpleName) {

    override fun onHandleIntent(intent: Intent?) {
        super.onHandleIntent(intent)
        try {
            if (receivedClassName == PluginReceivers.ExecuteSearchLyricsReceiver::class.java.name) {
                openSearchScreen()
            }
        } catch (e: Exception) {
            Timber.e(e)
            //AppUtils.showToast(this, R.string.error_app);
        }
    }

    /**
     * Open search lyrics screen.
     */
    private fun openSearchScreen() {
        val searchIntent = Intent(this, SearchActivity::class.java)
        searchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        searchIntent.putExtra(SearchActivity.INTENT_SEARCH_TITLE, propertyData.getFirst(MediaProperty.TITLE))
        searchIntent.putExtra(SearchActivity.INTENT_SEARCH_ARTIST, propertyData.getFirst(MediaProperty.ARTIST))
        startActivity(searchIntent)
    }

}
