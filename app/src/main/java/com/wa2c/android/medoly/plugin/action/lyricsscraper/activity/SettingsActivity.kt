package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity

import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.MenuItem
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R


/**
 * Settings activity.
 */
class SettingsActivity : PreferenceActivity() {

    /**
     * onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Action bar
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowTitleEnabled(true)
        actionBar.setTitle(R.string.title_activity_settings)
    }

    /**
     * onOptionsItemSelected
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


//    /**
//     * onCreate event.
//     */
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        fragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()
//
//        // Action bar
//        val actionBar = actionBar
//        if (actionBar != null) {
//            actionBar.setDisplayShowHomeEnabled(true)
//            actionBar.setDisplayHomeAsUpEnabled(true)
//            actionBar.setDisplayShowTitleEnabled(true)
//            actionBar.setTitle(R.string.title_activity_settings)
//        }
//    }
//
//    /**
//     * onOptionsItemSelected event.
//     */
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            android.R.id.home -> {
//                finish()
//                return true
//            }
//        }
//
//        return super.onOptionsItemSelected(item)
//    }
//
//
//    /**
//     * Settings fragment.
//     */
//    class SettingsFragment : PreferenceFragment() {
//
//
//        /**
//         * App info.
//         */
//        private val applicationDetailsPreferenceClickListener = Preference.OnPreferenceClickListener {
//            val intent = Intent()
//            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//            intent.data = Uri.parse("package:" + activity.packageName)
//            startActivity(intent)
//            true
//        }
//
//        /**
//         * About.
//         */
//        private val aboutPreferenceClickListener = Preference.OnPreferenceClickListener {
//            AboutDialogFragment.newInstance().show(activity)
//            true
//        }
//
//        /**
//         * On Shared Preference Change Listener.
//         */
//        private val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
//            // Update summary
//            updatePrefSummary(findPreference(key))
//        }
//
//        /**
//         * onCreate event.
//         */
//        override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//            addPreferencesFromResource(R.xml.pref_settings)
//
//            // App info
//            findPreference(getString(R.string.prefkey_application_details)).onPreferenceClickListener = applicationDetailsPreferenceClickListener
//            // About
//            findPreference(getString(R.string.prefkey_about)).onPreferenceClickListener = aboutPreferenceClickListener
//
//            initSummary(preferenceScreen)
//        }
//
//        /**
//         * onResume event.
//         */
//        override fun onResume() {
//            super.onResume()
//            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
//        }
//
//        /**
//         * onPause event.
//         */
//        override fun onPause() {
//            super.onPause()
//            preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
//        }
//
//        /**
//         * Initialize summary.
//         * @param p Target preference.
//         */
//        private fun initSummary(p: Preference?) {
//            if (p == null) return
//
//            // Get summary length
//            val summary = p.summary
//            if (summary != null && summary.length > 0) {
//                if (summary.toString().lastIndexOf("\n") != 0) p.summary = summary.toString() + "\n" // 改行追加
//                summaryLengthMap[p] = p.summary.length
//            } else {
//                summaryLengthMap[p] = 0
//            }
//
//            // Update summary
//            if (p is PreferenceCategory) {
//                val pCat = p as PreferenceCategory?
//                for (i in 0 until pCat!!.preferenceCount) {
//                    initSummary(pCat!!.getPreference(i))
//                }
//            } else if (p is PreferenceScreen) {
//                val ps = p as PreferenceScreen?
//                for (i in 0 until ps!!.preferenceCount) {
//                    initSummary(ps!!.getPreference(i))
//                }
//            } else {
//                updatePrefSummary(p)
//            }
//        }
//
//        /**
//         * Update summary.
//         * @param p Target preference.
//         */
//        private fun updatePrefSummary(p: Preference?) {
//            if (p == null) return
//
//            val key = p.key
//            var summary = p.summary
//            if (TextUtils.isEmpty(key)) return
//            if (TextUtils.isEmpty(summary)) summary = ""
//
//            // preference type
//            if (p is ListPreference) {
//                // ListPreference
//                val pref = p as ListPreference?
//                pref!!.value = p.sharedPreferences.getString(pref.key, "") // set value at once
//                p.setSummary(summary.subSequence(0, summaryLengthMap[p]).toString() + getString(R.string.settings_summary_current_value, pref.entry))
//            } else if (p is MultiSelectListPreference) {
//                // MultiSelectListPreference
//                val pref = p as MultiSelectListPreference?
//                val stringSet = pref!!.sharedPreferences.getStringSet(pref.key, null)
//                var text = ""
//                if (stringSet != null && stringSet.size > 0) {
//                    pref.values = stringSet // set value at once
//                    val builder = StringBuilder()
//                    for (i in 0 until pref.entries.size) {
//                        if (stringSet.contains(pref.entryValues[i])) {
//                            builder.append(pref.entries[i]).append(",")
//                        }
//                    }
//                    if (builder.length > 0) {
//                        text = builder.substring(0, builder.length - 1) // delete end comma
//                    }
//                }
//                p.setSummary(summary.subSequence(0, summaryLengthMap[p]).toString() + getString(R.string.settings_summary_current_value, text))
//            } else if (p is EditTextPreference) {
//                // EditTextPreference
//                val pref = p as EditTextPreference?
//                var text = p.sharedPreferences.getString(pref!!.key, "") // don't use pref.getText() because of update failed.
//
//                // adjust number
//                val inputType = pref.editText.inputType
//                try {
//                    if (inputType and InputType.TYPE_CLASS_NUMBER > 0) {
//                        if (inputType and InputType.TYPE_NUMBER_FLAG_DECIMAL > 0) {
//                            // real number
//                            var `val` = java.lang.Float.valueOf(text)!!
//                            if (inputType and InputType.TYPE_NUMBER_FLAG_SIGNED == 0 && `val` < 0) {
//                                `val` = 0f
//                            }
//                            text = `val`.toString()
//                        } else {
//                            // digit number
//                            var `val` = Integer.valueOf(text)!!
//                            if (inputType and InputType.TYPE_NUMBER_FLAG_SIGNED == 0 && `val` < 0) {
//                                `val` = 0
//                            }
//                            text = `val`.toString()
//                        }
//                    }
//                } catch (e: NumberFormatException) {
//                    text = "0"
//                }
//
//                pref.text = text // set value at once
//                p.setSummary(summary.subSequence(0, summaryLengthMap[p]).toString() + getString(R.string.settings_summary_current_value, text))
//            }
//        }
//
//        companion object {
//
//            /** Summary length.  */
//            private val summaryLengthMap = LinkedHashMap<Preference, Int>()
//        }
//
//    }

}
