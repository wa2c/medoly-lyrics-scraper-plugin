package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity

import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.MenuItem
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R

/**
 * Settings activity.
 */
class SettingsActivity : PreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Action bar
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowTitleEnabled(true)
        actionBar.setTitle(R.string.title_activity_settings)

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction().add(android.R.id.content, SettingsFragment()).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

}
