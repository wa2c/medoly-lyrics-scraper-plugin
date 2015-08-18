package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.widget.Toast;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;

import java.util.HashMap;
import java.util.Map;


/**
 * ダイアログの抽象クラス。
 */
public abstract class AbstractDialogFragment extends DialogFragment {

	/**
	 * ダイアログ状態管理。
	 */
	private static final Map<String, DialogFragment> shownDialogMap = new HashMap<>();



	/***
	 * ダイアログを表示する。
	 * @param activity アクティビティ。
	 */
	public void show(Activity activity) {
		if (activity == null) {
			Toast.makeText(getActivity(), R.string.error_dialog_dismissed, Toast.LENGTH_SHORT).show();
			return;
		}

		String key = this.getClass().getName();
		if (shownDialogMap.containsKey(key) && shownDialogMap.get(key) != null) {
			shownDialogMap.get(key).dismiss();
		}

		super.show(activity.getFragmentManager(), key);
		shownDialogMap.put(key, this);
	}

	/**
	 * ダイアログを表示する。
	 * @param fragment フラグメント。
	 */
	public void show(Fragment fragment) {
		if (fragment == null) {
			Toast.makeText(getActivity(), R.string.error_dialog_dismissed, Toast.LENGTH_SHORT).show();
			return;
		}

		String key = this.getClass().getName();
		if (shownDialogMap.containsKey(key) && shownDialogMap.get(key) != null) {
			shownDialogMap.get(key).dismiss();
		}

		super.show(fragment.getFragmentManager(), key);
		shownDialogMap.put(key, this);
	}



	/**
	 * onConfigurationChangedイベント処理。
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		Dialog d = getDialog();
		if (d != null) d.cancel(); // 回転等が発生した場合は閉じる
	}

	/**
	 * onStartイベント。
	 */
	@Override
	public void onStart() {
		super.onStart();
	}

	/**
	 * onStopイベント処理。 c
	 */
	@Override
	public void onStop() {
		super.onStop();
		Dialog d = getDialog();
		if (d != null) d.cancel(); // 回転等が発生した場合は閉じる
	}

	/**
	 * onDissmissイベント処理。 c
	 */
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);

		String key = this.getClass().getName();
		shownDialogMap.remove(key);
	}



	/** クリックのリスナ。 */
	protected DialogInterface.OnClickListener clickListener = null;

	/**
	 * クリックのリスナを設定する。
	 * @param listener 実行処理。
	 */
	public void setClickListener(DialogInterface.OnClickListener listener) {
		this.clickListener = listener;
	}

	/**
	 * クリックイベントを実行する。
	 * @param dialog ダイアログ。
	 * @param which クリックされたボタン。
	 */
	protected void onClickButton(DialogInterface dialog, int which) {
		onClickButton(dialog, which, true);
	}

	/**
	 * クリックイベントを実行する。
	 * @param dialog ダイアログ。
	 * @param which クリックされたボタン。
	 * @param close ダイアログを閉じる場合はtrue。
	 */
	protected void onClickButton(DialogInterface dialog, int which, boolean close) {
		if (dialog != null && clickListener != null) {
			clickListener.onClick(dialog, which);
			if (close) dialog.dismiss();
		} else {
			if (dialog != null && close) dialog.cancel();
		}
	}

}
