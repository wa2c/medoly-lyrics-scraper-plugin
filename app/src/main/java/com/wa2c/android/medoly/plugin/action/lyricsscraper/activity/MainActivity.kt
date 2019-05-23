package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wa2c.android.medoly.library.MedolyEnvironment
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.databinding.ActivityMainBinding
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils

/**
 * Main activity.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayShowTitleEnabled(true)
        }

        // Search
        binding.openSearchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // Cache
        binding.openCacheButton.setOnClickListener {
            startActivity(Intent(this, CacheActivity::class.java))
        }

        // Site
        binding.openSitesButton.setOnClickListener {
            startActivity(Intent(this, SiteActivity::class.java))
        }

        // Settings
        binding.openSettingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Launch Medoly
        binding.launchMedolyButton.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage(MedolyEnvironment.MEDOLY_PACKAGE)
            if (intent == null) {
                AppUtils.showToast(this, R.string.message_no_medoly)
                return@setOnClickListener
            }
            startActivity(intent)
        }
    }

}
