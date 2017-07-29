package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog.AboutDialogFragment;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;


/**
 * Settings activity.
 */
public class SettingsActivity extends PreferenceActivity {

    /**
     * onCreate event.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

        // Action bar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.title_activity_settings);
        }
    }

    /**
     * onOptionsItemSelected event.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * Settings fragment.
     */
    public static class SettingsFragment extends PreferenceFragment {

        /** Summary length. */
        private static final HashMap<Preference, Integer> summaryLengthMap = new LinkedHashMap<>();

        /**
         * onCreate event.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);

            // App info
            findPreference(getString(R.string.prefkey_application_details)).setOnPreferenceClickListener(applicationDetailsPreferenceClickListener);
            // About
            findPreference(getString(R.string.prefkey_about)).setOnPreferenceClickListener(aboutPreferenceClickListener);

            initSummary(getPreferenceScreen());
        }
        /**
         * onResume event.
         */
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        }

        /**
         * onPause event.
         */
        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        }



        /**
         * App info.
         */
        private Preference.OnPreferenceClickListener applicationDetailsPreferenceClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                startActivity(intent);
                return true;
            }
        };

        /**
         * About.
         */
        private Preference.OnPreferenceClickListener aboutPreferenceClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AboutDialogFragment.newInstance().show(getActivity());
                return true;
            }
        };

        /**
         * Initialize summary.
         * @param p Target preference.
         */
        private void initSummary(Preference p) {
            if (p == null) return;

            // Get summary length
            CharSequence summary = p.getSummary();
            if (summary != null && summary.length() > 0) {
                if (summary.toString().lastIndexOf("\n") != 0) p.setSummary(summary + "\n"); // 改行追加
                summaryLengthMap.put(p, p.getSummary().length());
            } else {
                summaryLengthMap.put(p, 0);
            }

            // Update summary
            if (p instanceof PreferenceCategory) {
                PreferenceCategory pCat = (PreferenceCategory) p;
                for (int i = 0; i < pCat.getPreferenceCount(); i++) {
                    initSummary(pCat.getPreference(i));
                }
            } else if (p instanceof PreferenceScreen) {
                PreferenceScreen ps = (PreferenceScreen) p;
                for (int i = 0; i < ps.getPreferenceCount(); i++) {
                    initSummary(ps.getPreference(i));
                }
            } else {
                updatePrefSummary(p);
            }
        }

        /**
         * Update summary.
         * @param p Target preference.
         */
        private void updatePrefSummary(Preference p) {
            if (p == null) return;

            String key = p.getKey();
            CharSequence summary = p.getSummary();
            if (TextUtils.isEmpty(key)) return;
            if (TextUtils.isEmpty(summary)) summary = "";

            // preference type
            if (p instanceof ListPreference) {
                // ListPreference
                ListPreference pref = (ListPreference) p;
                pref.setValue(p.getSharedPreferences().getString(pref.getKey(), "")); // set value at once
                p.setSummary(summary.subSequence(0, summaryLengthMap.get(p)) + getString(R.string.settings_summary_current_value, pref.getEntry()));
            } else if (p instanceof MultiSelectListPreference) {
                // MultiSelectListPreference
                MultiSelectListPreference pref = (MultiSelectListPreference) p;
                Set<String> stringSet = pref.getSharedPreferences().getStringSet(pref.getKey(), null);
                String text = "";
                if (stringSet != null && stringSet.size() > 0) {
                    pref.setValues(stringSet); // set value at once
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < pref.getEntries().length; i++) {
                        if (stringSet.contains(pref.getEntryValues()[i])) {
                            builder.append(pref.getEntries()[i]).append(",");
                        }
                    }
                    if (builder.length() > 0) {
                        text = builder.substring(0, builder.length() - 1); // delete end comma
                    }
                }
                p.setSummary(summary.subSequence(0, summaryLengthMap.get(p)) + getString(R.string.settings_summary_current_value, text));
            } else if (p instanceof EditTextPreference) {
                // EditTextPreference
                EditTextPreference pref = (EditTextPreference) p;
                String text = p.getSharedPreferences().getString(pref.getKey(), ""); // don't use pref.getText() because of update failed.

                // adjust number
                int inputType = pref.getEditText().getInputType();
                try {
                    if ( (inputType & InputType.TYPE_CLASS_NUMBER) > 0) {
                        if ((inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL) > 0) {
                            // real number
                            float val = Float.valueOf(text);
                            if ((inputType & InputType.TYPE_NUMBER_FLAG_SIGNED) == 0 && val < 0) {
                                val = 0;
                            }
                            text = String.valueOf(val);
                        } else {
                            // digit number
                            int val = Integer.valueOf(text);
                            if ((inputType & InputType.TYPE_NUMBER_FLAG_SIGNED) == 0 && val < 0) {
                                val = 0;
                            }
                            text = String.valueOf(val);
                        }
                    }
                } catch (NumberFormatException e) {
                    text = "0";
                }
                pref.setText(text); // set value at once
                p.setSummary(summary.subSequence(0, summaryLengthMap.get(p)) + getString(R.string.settings_summary_current_value, text));
            }
        }

        /**
         * On Shared Preference Change Listener.
         */
        private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                // Update summary
                updatePrefSummary(findPreference(key));
            }
        };

    }

}
