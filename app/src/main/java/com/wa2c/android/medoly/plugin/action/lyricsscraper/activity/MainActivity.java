package com.wa2c.android.medoly.plugin.action.lyricsscraper.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wa2c.android.medoly.library.MedolyEnvironment;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Main activity.
 */
public class MainActivity extends Activity {

    @BindView(R.id.openSearchButton)
    Button openSearchButton;
    @BindView(R.id.openCacheButton)
    Button openCacheButton;
    @BindView(R.id.openSitesButton)
    Button openSitesButton;
    @BindView(R.id.openSettingsButton)
    Button openSettingsButton;
    @BindView(R.id.launchMedolyButton)
    Button launchMedolyButton;

    @OnClick(R.id.openSearchButton)
    void openSearchButtonClick(View view) {
        startActivity(new Intent(MainActivity.this, SearchActivity.class));
    }
    @OnClick(R.id.openCacheButton)
    void openCacheButtonClick(View view) {
        startActivity(new Intent(MainActivity.this, CacheActivity.class));
    }
    @OnClick(R.id.openSitesButton)
    void openSitesButtonClickListener(View view) {
        startActivity(new Intent(MainActivity.this, SiteActivity.class));
    }
    @OnClick(R.id.openSettingsButton)
    void openSettingsButtonClick(View view) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }
    @OnClick(R.id.launchMedolyButton)
    void launchMedolyButtonClick(View view) {
        final Intent launchIntent = getPackageManager().getLaunchIntentForPackage(MedolyEnvironment.MEDOLY_PACKAGE);
        if (launchIntent != null)
            startActivity(launchIntent);
        else
            AppUtils.showToast(getApplicationContext(), R.string.message_no_medoly);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // ActionBar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

}
