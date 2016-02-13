package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.gson.Gson;
import com.wa2c.android.medoly.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wa2c on 2015/12/15.
 */
public class ParamManager {


    private static final String PREFKEY_GROUP_PARAM = "group_param";

    private static final String PREFKEY_SITE_PARAM = "site_param";

    private SpreadsheetService client;


    private SharedPreferences sharedPreferences;

    private Context context;

    public ParamManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    }


    public void start(Context context) {


//        try {
//            final String APPLICATION_NAME = context.getString(R.string.app_name);
//            client = new SpreadsheetService(APPLICATION_NAME);
//
//            URL url = new URL("https://docs.google.com/spreadsheets/d/1nT2imod7-aMaygeCOlFsODCz4VyqpJRPvWjjzA0vmTM/edit?pli=1#gid=0");
//            SpreadsheetQuery query = new SpreadsheetQuery(url);
//            query.setTitleQuery("日本ファルコム ボーカル曲一覧");
//            SpreadsheetFeed feed = client.query(query, SpreadsheetFeed.class);
//            List<SpreadsheetEntry> spreadsheetEntryList = feed.getEntries();
//            for (SpreadsheetEntry spreadsheetEntry : spreadsheetEntryList) {
//                System.out.println(spreadsheetEntry.getTitle().getPlainText());
//            }
//        } catch (AuthenticationException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ServiceException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        AsyncAuthTask task = new AsyncAuthTask(context);
        task.execute();

    }


    /**
     * 投稿タスク。
     */
    private class AsyncAuthTask extends AsyncTask<String, Void, Boolean> {
        private Context context;

        private HashMap<String, GroupParam> groupParamMap = new HashMap<>();
        private HashMap<String, SiteParam> siteParamMap = new HashMap<>();


        public AsyncAuthTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                final String APPLICATION_NAME = context.getString(R.string.app_name);
                client = new SpreadsheetService(APPLICATION_NAME);

                String applicationName = "AppName";

                SpreadsheetService service = new SpreadsheetService(applicationName);


                URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl("1rAtZzpOmwkAigUJ4ENOCglaXPK5klmGfAhGasQrVg6U", "public", "values");

                WorksheetFeed feed = service.getFeed(url, WorksheetFeed.class);
                List<WorksheetEntry> worksheetList = feed.getEntries();


                for (WorksheetEntry entry : worksheetList) {
                    ListQuery query = new ListQuery(entry.getListFeedUrl());
                    ListFeed listFeed = service.query(query, ListFeed.class);

                    String title = entry.getTitle().getPlainText();
                    if (title.equals(GroupParam.SHEET_NAME)) {
                        groupParamMap = getGroupParamMap(listFeed);
                    } else if (title.equals(SiteParam.SHEET_NAME)) {
                        siteParamMap = getSiteParamMap(listFeed);
                    }
                }




                WorksheetEntry worksheetEntry = worksheetList.get(0);



                String sss = worksheetEntry.getTitle().getPlainText();
                Logger.i(sss);


                ListQuery listQuery = new ListQuery(worksheetEntry.getListFeedUrl());




                //listQuery.setStartIndex(1);
                //listQuery.setMaxResults(2);
                //listQuery.setSpreadsheetQuery("SITE_ID = 1");
                ListFeed listFeed = service.query(listQuery, ListFeed.class);

                List<ListEntry> list = listFeed.getEntries();
                for (ListEntry row : list) {
                    Set<String> abc = row.getCustomElements().getTags();
                    System.out.println(row.getTitle().getPlainText() + "\t"
                            + row.getCustomElements().getValue("siteid"));
                    System.out.println(row.getTitle().getPlainText() + "\t"
                            + row.getCustomElements().getValue("siteuri"));
                                                        //+ row.getPlainTextContent());
                }


                ListEntry listEntry = listFeed.getEntries().get(0);
                CustomElementCollection elements = listEntry.getCustomElements();
                System.out.println("SITE_NAME：" + elements.getValue("SITE_NAME"));
                System.out.println("SITE_URI：" + elements.getValue("SITE_URI"));
                System.out.println("SEARCH_URI：" + elements.getValue("SEARCH_URI"));


//                ListQuery listQuery = new ListQuery(worksheetEntry.getListFeedUrl());
//                listQuery.setSpreadsheetQuery( query );
//
//                ListFeed listFeed = service.query( listQuery, ListFeed.class );
//                List<ListEntry> list = listFeed.getEntries();
//                for( ListEntry listEntry : list )
//                {
//                    System.out.println( "content=[" + listEntry.getPlainTextContent() + "]");
//                    CustomElementCollection elements = listEntry.getCustomElements();
//                    System.out.println(
//                            " name=" + elements.getValue("name") +
//                                    " age="  + elements.getValue("age") );
//                }

//                SpreadsheetService service = new SpreadsheetService(APPLICATION_NAME);
//
//                String key = "1nT2imod7-aMaygeCOlFsODCz4VyqpJRPvWjjzA0vmTM";
//                URL entryUrl = new URL("http://spreadsheets.google.com/feeds/spreadsheets/" + key);
//                SpreadsheetEntry spreadsheetEntry = service.getEntry(entryUrl, SpreadsheetEntry.class);
//
//                WorksheetEntry worksheetEntry = spreadsheetEntry.getDefaultWorksheet();
//                Logger.i(worksheetEntry.getTitle());


//                //URL url = new URL("https://docs.google.com/spreadsheets/d/1nT2imod7-aMaygeCOlFsODCz4VyqpJRPvWjjzA0vmTM/edit?usp=sharing");
//                //SpreadsheetQuery query = new SpreadsheetQuery(url);
//                query.setTitleQuery("日本ファルコム ボーカル曲一覧");
//                SpreadsheetFeed feed = client.query(query, SpreadsheetFeed.class);
//                List<SpreadsheetEntry> spreadsheetEntryList = feed.getEntries();
//                for (SpreadsheetEntry spreadsheetEntry : spreadsheetEntryList) {
//                    System.out.println(spreadsheetEntry.getTitle().getPlainText());
//                }
                return true;
            } catch (Exception e) {
                Logger.e(e);
                return false;
            }
        }


        private HashMap<String, GroupParam> getGroupParamMap(ListFeed listFeed) {
            HashMap<String, GroupParam> map = new HashMap<>();
            List<ListEntry> list = listFeed.getEntries();
            for (ListEntry row : list) {
                GroupParam param = new GroupParam(row);
                map.put(param.getValue(GroupParam.GroupColumn.GROUP_ID), param);
            }
            return map;
        }

        private HashMap<String, SiteParam> getSiteParamMap(ListFeed listFeed) {
            HashMap<String, SiteParam> map = new HashMap<>();
            List<ListEntry> list = listFeed.getEntries();
            for (ListEntry row : list) {
                SiteParam param = new SiteParam(row);
                map.put(param.getValue(SiteParam.SiteColumn.SITE_ID), param);
            }
            return map;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            Gson gson = new Gson();
            String groupParamMapJson = gson.toJson(groupParamMap);
            sharedPreferences.edit().putString(PREFKEY_GROUP_PARAM, groupParamMapJson).apply();
            String siteParamMapJson = gson.toJson(siteParamMap);
            sharedPreferences.edit().putString(PREFKEY_SITE_PARAM, siteParamMapJson).apply();


            ToastReceiver.showToast(context, "かんりょー");

            return;
        }
    }


    public HashMap<String, GroupParam> getGroupParamMap() {
        Gson gson = new Gson();

        HashMap<String, GroupParam> groupParamMap = new HashMap<>();
        String groupParamMapJson = sharedPreferences.getString(PREFKEY_GROUP_PARAM, null);
        if (groupParamMapJson != null) {
            groupParamMap = gson.fromJson(groupParamMapJson, HashMap.class);
        }
        return groupParamMap;
    }

}
