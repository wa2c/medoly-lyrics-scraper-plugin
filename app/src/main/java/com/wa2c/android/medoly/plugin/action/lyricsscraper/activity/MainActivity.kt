package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity

import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button

import com.wa2c.android.medoly.library.MedolyEnvironment
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils

import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Prefs
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Main activity.
 */
class MainActivity : Activity() {

    /** Preferences.  */
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = Prefs(this)

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

//    @BindView(R.id.openSearchButton)
//    internal var openSearchButton: Button? = null
//    @BindView(R.id.openCacheButton)
//    internal var openCacheButton: Button? = null
//    @BindView(R.id.openSitesButton)
//    internal var openSitesButton: Button? = null
//    @BindView(R.id.openSettingsButton)
//    internal var openSettingsButton: Button? = null
//    @BindView(R.id.launchMedolyButton)
//    internal var launchMedolyButton: Button? = null
//
//    @OnClick(R.id.openSearchButton)
//    internal fun openSearchButtonClick(view: View) {
//        startActivity(Intent(this@MainActivity, SearchActivity::class.java))
//    }
//
//    @OnClick(R.id.openCacheButton)
//    internal fun openCacheButtonClick(view: View) {
//        startActivity(Intent(this@MainActivity, CacheActivity::class.java))
//    }
//
//    @OnClick(R.id.openSitesButton)
//    internal fun openSitesButtonClickListener(view: View) {
//        startActivity(Intent(this@MainActivity, SiteActivity::class.java))
//    }
//
//    @OnClick(R.id.openSettingsButton)
//    internal fun openSettingsButtonClick(view: View) {
//        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
//    }
//
//    @OnClick(R.id.launchMedolyButton)
//    internal fun launchMedolyButtonClick(view: View) {
//        val launchIntent = packageManager.getLaunchIntentForPackage(MedolyEnvironment.MEDOLY_PACKAGE)
//        if (launchIntent != null)
//            startActivity(launchIntent)
//        else
//            AppUtils.showToast(applicationContext, R.string.message_no_medoly)
//    }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        ButterKnife.bind(this)
//
//        // ActionBar
//        val actionBar = actionBar
//        if (actionBar != null) {
//            actionBar.setDisplayShowHomeEnabled(true)
//            actionBar.setDisplayShowTitleEnabled(true)
//        }
//    }

}
