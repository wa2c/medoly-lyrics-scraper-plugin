package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.wa2c.android.medoly.utils.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;



/**
 * Spread sheet reading task class.
 */
public class SpreadSheetReadTask extends AsyncTask<String, Void, Boolean> {
    private static final String SITE_SHEET_NAME = "SITE";
    private static final String GROUP_SHEET_NAME = "GROUP";

    private Context context;
    private SpreadsheetService service;
    private ContentResolver resolver;

    public SpreadSheetReadTask(Context context) {
        this.context = context;
        service =  new SpreadsheetService(context.getString(R.string.app_name));
        resolver = context.getContentResolver();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {

            String sheetId = context.getString(R.string.sheet_id);
            URL feedURL = FeedURLFactory.getDefault().getWorksheetFeedUrl(sheetId, "public", "values");
            WorksheetFeed feed = service.getFeed(feedURL, WorksheetFeed.class);
            List<WorksheetEntry> worksheetList = feed.getEntries();

            int result = 1;
            for (WorksheetEntry entry : worksheetList) {
                ListQuery query = new ListQuery(entry.getListFeedUrl());
                ListFeed listFeed = service.query(query, ListFeed.class);

                String title = entry.getTitle().getPlainText();
                if (title.equals(SITE_SHEET_NAME)) {
                    result *= writeSiteTable(listFeed);
                } else if (title.equals(GROUP_SHEET_NAME)) {
                    result *= writeGroupTable(listFeed);
                }
            }

            if (result > 0)
                return true;
            else
                return false;
        } catch (Exception e) {
            Logger.e(e);
            return false;
        }
    }



    private int writeSiteTable(ListFeed listFeed) {
        try {
			ArrayList<ContentProviderOperation> operationList = new ArrayList<>();

            // delete
			operationList.add(ContentProviderOperation
                    .newDelete(SiteProvider.SITE_URI)
                    .build());

            // insert
            List<ListEntry> list = listFeed.getEntries();
            for (ListEntry row : list) {
                ContentValues values = new ContentValues();

                for (SiteColumn col : SiteColumn.values()) {
                    String val = row.getCustomElements().getValue(col.getColumnKey());
                    values.put(col.getColumnKey(), val);
                }
                operationList.add(ContentProviderOperation
                        .newInsert(SiteProvider.SITE_URI)
                        .withValues(values)
                        .build());
            }

            // apply
            ContentProviderResult[] results = resolver.applyBatch(SiteProvider.SITE_URI.getAuthority(), operationList);
            return results.length;
        } catch (Exception e) {
            Logger.e(e);
            return -1;
        }
    }

    private int writeGroupTable(ListFeed listFeed) {
        try {
            ArrayList<ContentProviderOperation> operationList = new ArrayList<>();

            // delete
            operationList.add(ContentProviderOperation
                    .newDelete(SiteProvider.GROUP_URI)
                    .build());

            // insert
            List<ListEntry> list = listFeed.getEntries();
            for (ListEntry row : list) {
                ContentValues values = new ContentValues();

                for (GroupColumn col : GroupColumn.values()) {
                    String val = row.getCustomElements().getValue(col.getColumnKey());
                    values.put(col.getColumnKey(), val);
                }
                operationList.add(ContentProviderOperation
                        .newInsert(SiteProvider.GROUP_URI)
                        .withValues(values)
                        .build());
            }

            // apply
            ContentProviderResult[] results = resolver.applyBatch(SiteProvider.GROUP_URI.getAuthority(), operationList);
            return results.length;
        } catch (Exception e) {
            Logger.e(e);
            return -1;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (actionListener != null) {
            actionListener.onListUpdated(result);
        }
    }

    // Event Listener

    /**
     * Event listener class.
     */
    public interface SiteUpdateListener extends EventListener {
        void onListUpdated(boolean isSucceeded);
    }

    /** Event listener. */
    private SiteUpdateListener actionListener;

    /**
     * Set event listener.
     * @param listener event listener.
     */
    public void setOnPropertyActionListener(SiteUpdateListener listener) {
        this.actionListener = listener;
    }
}
