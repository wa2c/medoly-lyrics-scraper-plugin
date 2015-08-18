package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wa2c.android.medoly.plugin.action.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * 設定画面のアクティビティ。
 */
public class SettingsActivity extends PreferenceActivity {

	/**
	 * onCreate event.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

		// アクションバー
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
	 * 設定画面フラグメント。
	 */
	public static class SettingsFragment extends PreferenceFragment {

		/** サマリの長さマップ。 */
		private static final HashMap<Preference, Integer> summaryLengthMap = new LinkedHashMap<>();

		/**
		 * onCreate event.
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_settings);

			// アプリ情報
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
		 * アプリ情報。
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
				RelativeLayout layoutView = (RelativeLayout) View.inflate(getActivity(), R.layout.layout_about, null);

				// Version
				try {
					PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo( getActivity().getPackageName(), PackageManager.GET_ACTIVITIES);
					((TextView)layoutView.findViewById(R.id.aboutAppVersionTextView)).setText("Ver. " + packageInfo.versionName);
				} catch (NameNotFoundException e) {
					Logger.e(e);
				}

				// Developer
				((TextView)layoutView.findViewById(R.id.aboutDeveloperNameTextView)).setText(getString(R.string.app_author));

				// Link
				Linkify.TransformFilter filter = new Linkify.TransformFilter() {
					@Override
					public String transformUrl(Matcher match, String url) {
						return getString(R.string.app_market_web);
					}
				};
				Linkify.addLinks(
						(TextView)layoutView.findViewById(R.id.aboutGooglePlayTextView),
						Pattern.compile("Google Play"),
						getString(R.string.app_market_web),
						null,
						filter);

				// Contact
				((TextView)layoutView.findViewById(R.id.aboutEmailTextView)).setText(getString(R.string.app_mail_name) + "@" + getString(R.string.app_mail_domain));

				// Library
				String[] libraryNames = getResources().getStringArray(R.array.about_library_names);
				String[] libraryUrls = getResources().getStringArray(R.array.about_library_urls);

				for (int i = 0; i < libraryNames.length; i++) {
					TextView libTextView;
					LinearLayout libraryLayout = (LinearLayout) layoutView.findViewById(R.id.abountLibraryLayout);
					libTextView = new TextView(getActivity());
					libTextView.setMovementMethod(LinkMovementMethod.getInstance());
					libTextView.setText(Html.fromHtml("<a href=\"" + libraryUrls[i] + "\">" +  libraryNames[i] + "</a>"));
					libTextView.setGravity(Gravity.CENTER_HORIZONTAL);
					libraryLayout.setPadding(2, 2, 2, 2);
					libraryLayout.addView(libTextView);
				}

				// ダイアログ作成
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.pref_title_about);
				builder.setView(layoutView);
				builder.setPositiveButton(android.R.string.ok, null);
				builder.create().show();

				return true;
			}
		};

		/**
		 * サマリを初期化する。
		 * @param p 対象項目。
		 */
		private void initSummary(Preference p) {
			if (p == null) return;

			// サマリの長さ取得
			CharSequence summary = p.getSummary();
			if (summary != null && summary.length() > 0) {
				if (summary.toString().lastIndexOf("\n") != 0) p.setSummary(summary + "\n"); // 改行追加
				summaryLengthMap.put(p, p.getSummary().length());
			} else {
				summaryLengthMap.put(p, 0);
			}

			// サマリ更新
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
		 * サマリを更新する。
		 * @param p 対象項目。
		 */
		private void updatePrefSummary(Preference p) {
			if (p == null) return;

			String key = p.getKey();
			CharSequence summary = p.getSummary();
			if (TextUtils.isEmpty(key)) return;
			if (TextUtils.isEmpty(summary)) summary = "";

			// 種別毎
			if (p instanceof ListPreference) {
				// ListPreference
				ListPreference pref = (ListPreference) p;
				pref.setValue(p.getSharedPreferences().getString(pref.getKey(), "")); // 一度値を更新
				p.setSummary(summary.subSequence(0, summaryLengthMap.get(p)) + getString(R.string.settings_summary_current_value, pref.getEntry()));
			} else if (p instanceof MultiSelectListPreference) {
				// MultiSelectListPreference
				MultiSelectListPreference pref = (MultiSelectListPreference) p;
				Set<String> stringSet = pref.getSharedPreferences().getStringSet(pref.getKey(), null);
				String text = "";
				if (stringSet != null && stringSet.size() > 0) {
					pref.setValues(stringSet); // 一度値を更新
					StringBuilder builder = new StringBuilder();
					for (int i = 0; i < pref.getEntries().length; i++) {
						if (stringSet.contains(pref.getEntryValues()[i])) {
							builder.append(pref.getEntries()[i]).append(",");
						}
					}
					if (builder.length() > 0) {
						text = builder.substring(0, builder.length() - 1); // 末尾のカンマ削除
					}
				}
				p.setSummary(summary.subSequence(0, summaryLengthMap.get(p)) + getString(R.string.settings_summary_current_value, text));
			} else if (p instanceof EditTextPreference) {
				// EditTextPreference
				EditTextPreference pref = (EditTextPreference) p;
				String text = p.getSharedPreferences().getString(pref.getKey(), ""); // 値が更新されない場合があるので、pref.getText() は使用しない

				// 数値型の補正
				int inputType = pref.getEditText().getInputType();
				try {
					if ( (inputType & InputType.TYPE_CLASS_NUMBER) > 0) {
						if ((inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL) > 0) {
							// 小数
							float val = Float.valueOf(text);
							if ((inputType & InputType.TYPE_NUMBER_FLAG_SIGNED) == 0 && val < 0) {
								val = 0;
							}
							text = String.valueOf(val);
						} else {
							// 整数
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
				pref.setText(text); // 一度値を更新
				p.setSummary(summary.subSequence(0, summaryLengthMap.get(p)) + getString(R.string.settings_summary_current_value, text));
			}
		}

		/**
		 * 設定更新時の処理。
		 */
		private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				// サマリ更新
				updatePrefSummary(findPreference(key));
			}
		};

	}

}
