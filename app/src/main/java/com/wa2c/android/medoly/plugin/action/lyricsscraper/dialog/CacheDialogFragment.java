package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;


import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCache;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.db.SearchCacheHelper;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.search.ResultItem;

import java.util.Arrays;


/**
 * Confirm dialog.
 */
public class CacheDialogFragment extends AbstractDialogFragment {

    /** Dialog result on delete lyrics. */
    public final static int DIALOG_RESULT_DELETE_LYRICS = -10;
    /** Dialog result on delete cache. */
    public final static int DIALOG_RESULT_DELETE_CACHE = -20;

    /** Cache key. */
    private final static String ARG_CACHE = "ARG_CACHE";

    /**
     * Create dialog instance.
     * @param cache Search cache.
     * @return Dialog instance.
     */
    static public CacheDialogFragment newInstance(SearchCache cache) {
        CacheDialogFragment fragment = new CacheDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CACHE, cache);
        fragment.setArguments(args);

        return fragment;
    }



    /**
     * onCreateDialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // data
        final SearchCache cache = (SearchCache)getArguments().getSerializable(ARG_CACHE);
        ResultItem result = cache.makeResultItem();

        // view
        final View content = View.inflate(getActivity(), R.layout.dialog_cache, null);
        TextView textView = (TextView)content.findViewById(R.id.dialogCacheLyricsTextView);
        textView.setText((cache.has_lyrics != null && cache.has_lyrics) ? result.getLyrics() : getString(R.string.message_dialog_cache_none));

//        // delete lyrics button
//        content.findViewById(R.id.dialogCacheDeleteLyricsButton).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                (new AsyncTask<Void, Void, Void>() {
//                    @Override
//                    protected Void doInBackground(Void... params) {
//                        SearchCacheHelper searchCacheHelper = new SearchCacheHelper(getActivity());
//                        searchCacheHelper.insertOrUpdate(cache.title, cache.artist, null);
//                        return null;
//                    }
//                    @Override
//                    protected void onPostExecute(Void result) {
//                        onClickButton(getDialog(), DIALOG_RESULT_DELETE_LYRICS);
//                    }
//                }).execute();
//             }
//        });
//        // delete cache button
//        content.findViewById(R.id.dialogCacheDeleteCacheButton).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                (new AsyncTask<Void, Void, Void>() {
//                    @Override
//                    protected Void doInBackground(Void... params) {
//                        SearchCacheHelper searchCacheHelper = new SearchCacheHelper(getActivity());
//                        searchCacheHelper.delete(Arrays.asList(cache));
//                        return null;
//                    }
//                    @Override
//                    protected void onPostExecute(Void result) {
//                        onClickButton(getDialog(), DIALOG_RESULT_DELETE_CACHE);
//                    }
//                }).execute();
//            }
//        });

        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.title_activity_cache);
        builder.setView(content);
        builder.setNeutralButton(R.string.label_close, null);
        builder.setNegativeButton(R.string.label_dialog_cache_research, clickListener);
        if (result != null && !TextUtils.isEmpty(result.getLyrics())) {
            builder.setPositiveButton(R.string.menu_search_save_file, clickListener);
        }
        return  builder.create();
    }

}
