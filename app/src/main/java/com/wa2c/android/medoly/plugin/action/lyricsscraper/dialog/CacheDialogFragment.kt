package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.databinding.DialogCacheBinding
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.DbHelper
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCache
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Cache dialog.
 */
class CacheDialogFragment : AbstractDialogFragment() {

    private lateinit var binding: DialogCacheBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        // data
        val cache = arguments.getSerializable(ARG_CACHE) as SearchCache
        val result = cache.makeResultItem()

        // view
        binding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.dialog_cache, null, false)
        binding.dialogCacheLyricsTextView.text = if (cache.has_lyrics != null && cache.has_lyrics!!) result!!.lyrics else getString(R.string.message_dialog_cache_none)

        // deleteCache lyrics button
        binding.dialogCacheDeleteLyricsButton.setOnClickListener {
            deleteLyrics(cache)
        }

        // deleteCache cache button
        binding.dialogCacheDeleteCacheButton.setOnClickListener {
            deleteCache(cache)
        }

        // build dialog
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.title_activity_cache)
        builder.setView(binding.root)
        builder.setNeutralButton(R.string.label_close, null)
        builder.setNegativeButton(R.string.label_dialog_cache_research, null)
        if (result != null && !result.lyrics.isNullOrEmpty()) {
            builder.setPositiveButton(R.string.menu_search_save_file, null)
        }
        return builder.create()
    }

    /**
     * Delete cache lyrics.
     */
    private fun deleteLyrics(cache: SearchCache) {
        val searchCacheHelper = DbHelper(this@CacheDialogFragment.activity)
        GlobalScope.launch(Dispatchers.Main) {
            val deleteResult = async(Dispatchers.Default) {
                try {
                    searchCacheHelper.insertOrUpdateCache(cache.title, cache.artist, null)
                } catch (e: Exception) {
                    return@async null
                }
            }
            val r = deleteResult.await()
            if (r != true)
                AppUtils.showToast(this@CacheDialogFragment.activity, R.string.message_dialog_cache_delete_error)
            clickListener?.invoke(dialog, DIALOG_RESULT_DELETE_LYRICS, null)
            dialog.dismiss()
        }
    }

    /**
     * Delete cache.
     */
    private fun deleteCache(cache: SearchCache) {
        val searchCacheHelper = DbHelper(this@CacheDialogFragment.activity)
        GlobalScope.launch(Dispatchers.Main) {
            val deleteResult = async(Dispatchers.Default) {
                try {
                    searchCacheHelper.deleteCache(listOf(cache))
                } catch (e: Exception) {
                    return@async null
                }
            }
            val r = deleteResult.await()
            if (r != true)
                AppUtils.showToast(this@CacheDialogFragment.activity, R.string.message_dialog_cache_delete_error)
            clickListener?.invoke(dialog, DIALOG_RESULT_DELETE_CACHE, null)
            dialog.dismiss()
        }
    }


    companion object {

        /** Dialog result on deleteCache lyrics.  */
        const val DIALOG_RESULT_DELETE_LYRICS = -10
        /** Dialog result on deleteCache cache.  */
        const val DIALOG_RESULT_DELETE_CACHE = -20

        /** Cache key.  */
        private const val ARG_CACHE = "ARG_CACHE"

        /**
         * Create dialog instance.
         * @param cache Search cache.
         * @return Dialog instance.
         */
        fun newInstance(cache: SearchCache): CacheDialogFragment {
            val fragment = CacheDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_CACHE, cache)
            fragment.arguments = args

            return fragment
        }
    }
}
