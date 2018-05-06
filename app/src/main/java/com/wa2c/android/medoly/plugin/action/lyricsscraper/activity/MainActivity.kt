package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.wa2c.android.medoly.library.MedolyEnvironment
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Main activity.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayShowTitleEnabled(true)

        // Search
        openSearchButton.setOnClickListener({
            startActivity(Intent(this, SearchActivity::class.java))
        })

        // Cache
        openCacheButton.setOnClickListener({
            startActivity(Intent(this, CacheActivity::class.java))
        })

        // Site
        openSitesButton.setOnClickListener({
            startActivity(Intent(this@MainActivity, SiteActivity::class.java))
        })

        // Settings
        openSettingsButton.setOnClickListener({
            startActivity(Intent(this, SettingsActivity::class.java))
        })

        // Launch Medoly
        launchMedolyButton.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage(MedolyEnvironment.MEDOLY_PACKAGE)
            if (intent == null) {
                AppUtils.showToast(this, R.string.message_no_medoly)
                return@setOnClickListener
            }
            startActivity(intent)
        }
    }

}
