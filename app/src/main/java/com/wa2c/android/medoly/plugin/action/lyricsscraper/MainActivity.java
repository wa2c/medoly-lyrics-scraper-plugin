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

//		// Account Auth
//		findViewById(R.id.twitterOAuthButton).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final AuthDialogFragment dialogFragment = AuthDialogFragment.newInstance();
//				dialogFragment.setClickListener(new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//						if (which == DialogInterface.BUTTON_POSITIVE) {
//							try {
//								// 認証
//								String username = dialogFragment.getUsername();
//								String password = dialogFragment.getPassword();
//								(new AsyncAuthTask(username, password)).execute();
//							} catch (Exception e) {
//								Logger.e(e);
//								// 失敗
////								preference.edit().remove(getString(R.string.prefkey_auth_username)).apply();
////								preference.edit().remove(getString(R.string.prefkey_auth_password)).apply();
////								AppUtils.showToast(MainActivity.this, R.string.message_auth_failure);
//							}
//						} else if (which == DialogInterface.BUTTON_NEUTRAL) {
//							// クリア
////							preference.edit().remove(getString(R.string.prefkey_auth_username)).apply();
////							preference.edit().remove(getString(R.string.prefkey_auth_password)).apply();
////							AppUtils.showToast(MainActivity.this, R.string.message_account_clear);
//						}
//
//						updateAuthMessage();
//					}
//				});
//				dialogFragment.show(MainActivity.this);
//			}
//		});

		// Open Last.fm
		findViewById(R.id.lastfmSiteButton).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//
//				String username = sharedPreferences.getString(getString(R.string.prefkey_auth_username), "");
//				Uri uri;
//				if (TextUtils.isEmpty(username)) {
//					// ユーザ未認証
//					uri = Uri.parse(getString(R.string.lastfm_url));
//				} else {
//					// ユーザ認証済
//					uri = Uri.parse(getString(R.string.lastfm_url_user, username));
//				}
//				Intent i = new Intent(Intent.ACTION_VIEW, uri);
//				startActivity(i);
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


		//updateAuthMessage();
	}

//	/**
//	 * 投稿タスク。
//	 */
//	private class AsyncAuthTask extends AsyncTask<String, Void, Boolean> {
//		private SharedPreferences preferences;
//		private Context context;
//		private String username;
//		private String password;
//
//		public AsyncAuthTask(String username, String password) {
//			this.preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//			this.context = MainActivity.this;
//			this.username = username;
//			this.password = StringUtilities.md5(password); // パスワードはMD5で処理;
//		}
//
//		@Override
//		protected Boolean doInBackground(String... params) {
//			try {
//				// フォルダ設定 (getMobileSessionの前に入れる必要あり)
//				Caller.getInstance().setCache(new FileSystemCache(new File(context.getExternalCacheDir().getPath() + File.separator + "last.fm")));
//
//				// 認証
//				String k = context.getString(R.string.base_app_name) + "__" + context.getString(R.string.domain_name);
//				Session session = Authenticator.getMobileSession(username, password, Token.getKey1(context), Token.getKey2(context));
//				return  (session != null);
//			} catch (Exception e) {
//				Logger.e(e);
//				return null;
//			}
//			return true;
//		}
//
//		@Override
//		protected void onPostExecute(Boolean result) {
//			if (result) {
//				preferences.edit().putString(getString(R.string.prefkey_auth_username), username).apply();
//				preferences.edit().putString(getString(R.string.prefkey_auth_password), password).apply();
//				AppUtils.showToast(context, R.string.message_auth_success); // Succeed
//			} else {
//				preferences.edit().remove(getString(R.string.prefkey_auth_username)).apply();
//				preferences.edit().remove(getString(R.string.prefkey_auth_password)).apply();
//				AppUtils.showToast(MainActivity.this, R.string.message_auth_failure); // Failed
//			}
//
//			updateAuthMessage();
//		}
//	}
//
//
//	/**
//	 * 認証状態のメッセージを更新する。
//	 */
//	private void updateAuthMessage() {
//		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//		String username = preference.getString(getString(R.string.prefkey_auth_username), "");
//		String password = preference.getString(getString(R.string.prefkey_auth_password), "");
//
//		if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
//			((TextView)findViewById(R.id.accountAuthTextView)).setText(getString(R.string.message_account_auth));
//		} else {
//			((TextView)findViewById(R.id.accountAuthTextView)).setText(getString(R.string.message_account_not_auth));
//		}
//	}

}
