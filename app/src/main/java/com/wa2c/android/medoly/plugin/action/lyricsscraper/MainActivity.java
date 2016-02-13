package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.wa2c.android.medoly.library.MedolyParam;
import com.wa2c.android.medoly.utils.Logger;

import java.util.HashSet;

/**
 * メイン画面のアクティビティ。
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Site List
        findViewById(R.id.siteListButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SiteActivity.class));
            }
        });

        // Update Site
        findViewById(R.id.updateSiteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GroupActivity.class));
            }
        });

        // Settings
        findViewById(R.id.settingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        // Launch Medoly
        final Intent launchIntent = getPackageManager().getLaunchIntentForPackage(MedolyParam.MEDOLY_PACKAGE);
        findViewById(R.id.launchMedolyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (launchIntent != null) startActivity(launchIntent);
            }
        });
        if (launchIntent == null) {
            findViewById(R.id.launchMedolyButton).setVisibility(View.GONE);
            findViewById(R.id.noMedolyTextView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.launchMedolyButton).setVisibility(View.VISIBLE);
            findViewById(R.id.noMedolyTextView).setVisibility(View.GONE);
        }


    }

}
