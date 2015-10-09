package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.wa2c.android.medoly.plugin.action.ActionPluginParam;


/**
 * メイン画面のアクティビティ。
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Site List
		findViewById(R.id.siteListButton).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

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
		final Intent launchIntent =  getPackageManager().getLaunchIntentForPackage(ActionPluginParam.MEDOLY_PACKAGE);
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
