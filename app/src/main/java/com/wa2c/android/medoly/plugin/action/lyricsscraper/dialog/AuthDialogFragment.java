package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;


/**
 * 確認ダイアログを表示する。
 */
public class AuthDialogFragment extends AbstractDialogFragment {
	/**
	 * ダイアログのインスタンスを作成する。
	 * @return ダイアログのインスタンス。
	 */
	static public AuthDialogFragment newInstance() {
		AuthDialogFragment fragment = new AuthDialogFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);

		return fragment;
	}

	private EditText dialogAuthUsernameEditText;
	private EditText dialogAuthPasswordEditText;


	/**
	 * onCreateDialogイベント処理。
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View content = View.inflate(getActivity(), R.layout.dialog_auth, null);

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String username = pref.getString(getString(R.string.prefkey_auth_username), "");

		// default
		dialogAuthUsernameEditText = ((EditText)content.findViewById(R.id.dialogAuthUsernameEditText));
		dialogAuthPasswordEditText = ((EditText)content.findViewById(R.id.dialogAuthPasswordEditText));
		dialogAuthUsernameEditText.setText(username);

		// Dialog build
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(content);
		builder.setTitle(R.string.title_dialog_auth);

		// Auth
		builder.setPositiveButton(R.string.label_dialog_auth_auth, clickListener);
		// Clear
		builder.setNeutralButton(R.string.label_dialog_auth_clear, clickListener);
		// Cancel
		builder.setNegativeButton(android.R.string.cancel, clickListener);


		return  builder.create();
	}


	/**
	 * 入力ユーザ名を取得する。
	 * @return ユーザ名。
	 */
	public String getUsername() {
		return dialogAuthUsernameEditText.getText().toString();
	}

	/**
	 * 入力パスワードを取得する。
	 * @return パスワード。
	 */
	public String getPassword() {
		return dialogAuthPasswordEditText.getText().toString();
	}

}
