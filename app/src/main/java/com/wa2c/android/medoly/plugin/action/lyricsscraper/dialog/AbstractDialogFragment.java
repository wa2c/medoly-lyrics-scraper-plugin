package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * Abstract dialog.
 */
public abstract class AbstractDialogFragment extends DialogFragment {

    /**
     * Exists dialog map.
     */
    private static final Map<String, DialogFragment> shownDialogMap = new HashMap<>();

    /** Activity。 */
    protected Activity context;
    /** Preference. */
    protected SharedPreferences preferences;



    /***
     * Show dialog.
     * @param activity Activity.
     */
    public void show(Activity activity) {
        if (activity == null) {
            AppUtils.showToast(getActivity(), R.string.error_dialog_dismissed);
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
     * Show dialog.
     * @param fragment Fragment.
     */
    public void show(Fragment fragment) {
        if (fragment == null) {
            AppUtils.showToast(getActivity(), R.string.error_dialog_dismissed);
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
     * onCreateDialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        context = getActivity();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return super.onCreateDialog(savedInstanceState);
    }

    /**
     * onStart.
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * onConfigurationChanged.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Dialog d = getDialog();
        if (d != null) d.cancel(); // close
    }

    /**
     * onStop
     */
    @Override
    public void onStop() {
        super.onStop();
        Dialog d = getDialog();
        if (d != null) d.cancel();
    }

    /**
     * onDismiss
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        String key = this.getClass().getName();
        shownDialogMap.remove(key);
    }



    /** Click listener. */
    protected DialogInterface.OnClickListener clickListener = null;

    /**
     * Set click listener.
     * @param listener Click listener.
     */
    public void setClickListener(DialogInterface.OnClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Execute button click event.
     * @param dialog Dialog.
     * @param which Clicked button.
     */
    protected void onClickButton(DialogInterface dialog, int which) {
        onClickButton(dialog, which, true);
    }

    /**
     * Execute button click event.
     * @param dialog Dialog.
     * @param which Clicked button.
     * @param close true if closing dialog.
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
