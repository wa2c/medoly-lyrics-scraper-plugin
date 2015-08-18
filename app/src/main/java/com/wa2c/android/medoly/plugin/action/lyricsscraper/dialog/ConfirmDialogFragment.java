package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;


/**
 * 確認ダイアログを表示する。
 */
public class ConfirmDialogFragment extends AbstractDialogFragment {

	/** タイトルキー。 */
	private final static String ARG_TITLE = "TITLE";
	/** メッセージキー。 */
	private final static String ARG_MESSAGE = "MESSAGE";

	/** 標準ボタン判定。 */
	private final static String ARG_IS_BUTTON_DEFAULT = "IS_BUTTON_DEFAULT";
	/** Positiveボタンキー。 */
	private final static String ARG_POSITIVE_BUTTON = "POISITIVE_BUTTON";
	/** Neutralボタンキー。 */
	private final static String ARG_NEUTRAL_BUTTON = "NEUTRAL_BUTTON";
	/** Negativeボタンキー。 */
	private final static String ARG_NEGATIVE_BUTTON = "NEGATIVE_BUTTON";


	/**
	 * ダイアログのインスタンスを作成する。(OK/Cancelボタン。)
	 * @param message メッセージ。
	 * @param title タイトル。
	 * @return ダイアログのインスタンス。
	 */
	static public ConfirmDialogFragment newInstance(CharSequence message, CharSequence title) {
		ConfirmDialogFragment fragment = new ConfirmDialogFragment();
		Bundle args = new Bundle();
		args.putCharSequence(ARG_MESSAGE, message);
		args.putCharSequence(ARG_TITLE, title);
		args.putBoolean(ARG_IS_BUTTON_DEFAULT, true);
		fragment.setArguments(args);

		return fragment;
	}

	/**
	 * ダイアログのインスタンスを作成する。(ボタン文字指定。null指定の場合はボタン無し。)
	 * @param message メッセージ。
	 * @param title タイトル。
	 * @param positiveButton Positiveボタン。
	 * @param neutralButton  Neutralボタン。
	 * @param negativeButton Negativeボタン。
	 * @return ダイアログのインスタンス。
	 */
	static public ConfirmDialogFragment newInstance(String message, String title, String positiveButton, String neutralButton, String negativeButton) {
		ConfirmDialogFragment fragment = new ConfirmDialogFragment();
		Bundle args = new Bundle();
		args.putCharSequence(ARG_MESSAGE, message);
		args.putCharSequence(ARG_TITLE, title);
		args.putString(ARG_POSITIVE_BUTTON, positiveButton);
		args.putString(ARG_NEUTRAL_BUTTON,  neutralButton);
		args.putString(ARG_NEGATIVE_BUTTON, negativeButton);
		args.putBoolean(ARG_IS_BUTTON_DEFAULT, false);
		fragment.setArguments(args);

		return fragment;
	}



	/**
	 * onCreateDialogイベント処理。
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();

		// ダイアログビルダ
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(args.getCharSequence(ARG_TITLE));
		builder.setMessage(args.getCharSequence(ARG_MESSAGE));

		// ボタン
		if (args.getBoolean(ARG_IS_BUTTON_DEFAULT)) {
			builder.setPositiveButton(android.R.string.ok, clickListener);
			builder.setNegativeButton(android.R.string.cancel, clickListener);
		} else {
			// Positiveボタン
			String positive = args.getString(ARG_POSITIVE_BUTTON);
			if (!TextUtils.isEmpty(positive)) {
				builder.setPositiveButton(positive, clickListener);
			}
			// Neutralボタン
			String neutral = args.getString(ARG_NEUTRAL_BUTTON);
			if (!TextUtils.isEmpty(neutral)) {
				builder.setNeutralButton(neutral, clickListener);

			}
			// Negativeボタン
			String negative = args.getString(ARG_NEGATIVE_BUTTON);
			if (!TextUtils.isEmpty(negative)) {
				builder.setNegativeButton(negative, clickListener);
			}
		}

		return  builder.create();
	}

}
