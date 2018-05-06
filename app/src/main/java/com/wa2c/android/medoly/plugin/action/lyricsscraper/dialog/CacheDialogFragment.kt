package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCache
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCacheHelper
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import kotlinx.android.synthetic.main.dialog_cache.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

/**
 * Cache dialog.
 */
class CacheDialogFragment : AbstractDialogFragment() {

    /**
     * onCreateDialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        // data
        val cache = arguments.getSerializable(ARG_CACHE) as SearchCache
        val result = cache.makeResultItem()

        // view
        val contentView = View.inflate(activity, R.layout.dialog_cache, null)
        contentView.dialogCacheLyricsTextView.text = if (cache.has_lyrics != null && cache.has_lyrics!!) result!!.lyrics else getString(R.string.message_dialog_cache_none)

//        // deleteCache lyrics button
//        contentView.dialogCacheDeleteLyricsButton.setOnClickListener { LyricsDeleteAsyncTask(this@CacheDialogFragment, cache).execute() }
//
//        // deleteCache cache button
//        contentView.dialogCacheDeleteCacheButton.setOnClickListener { CacheDeleteAsyncTask(this@CacheDialogFragment, cache).execute() }
        // deleteCache lyrics button
        contentView.dialogCacheDeleteLyricsButton.setOnClickListener {
            deleteLyrics(cache)
        }

        // deleteCache cache button
        contentView.dialogCacheDeleteCacheButton.setOnClickListener {
            deleteCache(cache)
        }

        // build dialog
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.title_activity_cache)
        builder.setView(contentView)
        builder.setNeutralButton(R.string.label_close, null)
        builder.setNegativeButton(R.string.label_dialog_cache_research, clickListener)
        if (result != null && !result.lyrics.isNullOrEmpty()) {
            builder.setPositiveButton(R.string.menu_search_save_file, clickListener)
        }
        return builder.create()
    }

//    /**
//     * Delete lyrics task
//     */
//    private class LyricsDeleteAsyncTask internal constructor(dialog: AbstractDialogFragment, private val cache: SearchCache) : AsyncTask<Void, Void, Void>() {
//
//        private val dialogReference: WeakReference<AbstractDialogFragment> = WeakReference(dialog)
//
//        override fun doInBackground(vararg params: Void): Void? {
//            val c = dialogReference.get()?.activity ?: return null
//            val searchCacheHelper = SearchCacheHelper(c)
//            searchCacheHelper.insertOrUpdateCache(cache.title, cache.artist, null)
//            return null
//        }
//
//        override fun onPostExecute(result: Void) {
//            dialogReference.get()?.onClickButton(dialogReference.get()?.dialog, DIALOG_RESULT_DELETE_LYRICS)
//        }
//    }

//    /**
//     * Delete lyrics task
//     */
//    private class CacheDeleteAsyncTask internal constructor(dialog: AbstractDialogFragment, private val cache: SearchCache) : AsyncTask<Void, Void, Void>() {
//
//        private val dialogReference: WeakReference<AbstractDialogFragment> = WeakReference(dialog)
//
//        override fun doInBackground(vararg params: Void): Void? {
//            val c = dialogReference.get()?.activity ?: return null
//            val searchCacheHelper = SearchCacheHelper(c)
//            searchCacheHelper.deleteCache(listOf(cache))
//            return null
//        }
//
//        override fun onPostExecute(result: Void) {
//            dialogReference.get()?.onClickButton(dialogReference.get()?.dialog, DIALOG_RESULT_DELETE_CACHE)
//        }
//    }


    /**
     * Delete cache lyrics.
     */
    private fun deleteLyrics(cache: SearchCache) {
        val searchCacheHelper = SearchCacheHelper(this@CacheDialogFragment.activity)
        launch(UI) {
            val deleteResult = async {
                try {
                    searchCacheHelper.insertOrUpdateCache(cache.title, cache.artist, null)
                } catch (e: Exception) {
                    return@async null
                }
            }
            val r = deleteResult.await()
            if (r != true)
                AppUtils.showToast(this@CacheDialogFragment.activity, R.string.message_dialog_cache_delete_error)
            onClickButton(dialog, DIALOG_RESULT_DELETE_LYRICS)
        }
    }

    /**
     * Delete cache.
     */
    private fun deleteCache(cache: SearchCache) {
        val searchCacheHelper = SearchCacheHelper(this@CacheDialogFragment.activity)
        launch(UI) {
            val deleteResult = async {
                try {
                    searchCacheHelper.deleteCache(listOf(cache))
                } catch (e: Exception) {
                    return@async null
                }
            }
            val r = deleteResult.await()
            if (r != true)
                AppUtils.showToast(this@CacheDialogFragment.activity, R.string.message_dialog_cache_delete_error)
            onClickButton(dialog, DIALOG_RESULT_DELETE_CACHE)
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

//
///**
// * Confirm dialog.
// */
//class CacheDialogFragment : AbstractDialogFragment() {
//
//
//    /**
//     * onCreateDialog
//     */
//    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
//        super.onCreateDialog(savedInstanceState)
//
//        // data
//        val cache = arguments.getSerializable(ARG_CACHE) as SearchCache
//        val result = cache.makeResultItem()
//
//        // view
//        val content = View.inflate(activity, R.layout.dialog_cache, null)
//        val textView = content.findViewById<View>(R.id.dialogCacheLyricsTextView) as TextView
//        textView.text = if (cache.has_lyrics != null && cache.has_lyrics!!) result!!.lyrics else getString(R.string.message_dialog_cache_none)
//
//        //        // deleteCache lyrics button
//        //        content.findViewById(R.id.dialogCacheDeleteLyricsButton).setOnClickListener(new View.OnClickListener() {
//        //            @Override
//        //            public void onClick(View v) {
//        //                (new AsyncTask<Void, Void, Void>() {
//        //                    @Override
//        //                    protected Void doInBackground(Void... params) {
//        //                        SearchCacheHelper searchCacheHelper = new SearchCacheHelper(getActivity());
//        //                        searchCacheHelper.insertOrUpdateCache(cache.title, cache.artist, null);
//        //                        return null;
//        //                    }
//        //                    @Override
//        //                    protected void onPostExecute(Void result) {
//        //                        onClickButton(getDialog(), DIALOG_RESULT_DELETE_LYRICS);
//        //                    }
//        //                }).execute();
//        //             }
//        //        });
//        //        // deleteCache cache button
//        //        content.findViewById(R.id.dialogCacheDeleteCacheButton).setOnClickListener(new View.OnClickListener() {
//        //            @Override
//        //            public void onClick(View v) {
//        //                (new AsyncTask<Void, Void, Void>() {
//        //                    @Override
//        //                    protected Void doInBackground(Void... params) {
//        //                        SearchCacheHelper searchCacheHelper = new SearchCacheHelper(getActivity());
//        //                        searchCacheHelper.deleteCache(Arrays.asList(cache));
//        //                        return null;
//        //                    }
//        //                    @Override
//        //                    protected void onPostExecute(Void result) {
//        //                        onClickButton(getDialog(), DIALOG_RESULT_DELETE_CACHE);
//        //                    }
//        //                }).execute();
//        //            }
//        //        });
//
//        // build dialog
//        val builder = AlertDialog.Builder(context)
//        builder.setTitle(R.string.title_activity_cache)
//        builder.setView(content)
//        builder.setNeutralButton(R.string.label_close, null)
//        builder.setNegativeButton(R.string.label_dialog_cache_research, clickListener)
//        if (result != null && !TextUtils.isEmpty(result.lyrics)) {
//            builder.setPositiveButton(R.string.menu_search_save_file, clickListener)
//        }
//        return builder.create()
//    }
//
//    companion object {
//
//        /** Dialog result on deleteCache lyrics.  */
//        val DIALOG_RESULT_DELETE_LYRICS = -10
//        /** Dialog result on deleteCache cache.  */
//        val DIALOG_RESULT_DELETE_CACHE = -20
//
//        /** Cache key.  */
//        private val ARG_CACHE = "ARG_CACHE"
//
//        /**
//         * Create dialog instance.
//         * @param cache Search cache.
//         * @return Dialog instance.
//         */
//        fun newInstance(cache: SearchCache): CacheDialogFragment {
//            val fragment = CacheDialogFragment()
//            val args = Bundle()
//            args.putSerializable(ARG_CACHE, cache)
//            fragment.arguments = args
//
//            return fragment
//        }
//    }
//
//}
