package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;


/**
 * Confirm dialog.
 */
public class ConfirmDialogFragment extends AbstractDialogFragment {

    /** Title key. */
    private final static String ARG_TITLE = "TITLE";
    /** Message key. */
    private final static String ARG_MESSAGE = "MESSAGE";


    private final static String ARG_IS_BUTTON_DEFAULT = "IS_BUTTON_DEFAULT";
    /** Positive button key. */
    private final static String ARG_POSITIVE_BUTTON = "POSITIVE_BUTTON";
    /** Neutral button key. */
    private final static String ARG_NEUTRAL_BUTTON = "NEUTRAL_BUTTON";
    /** Negative button key. */
    private final static String ARG_NEGATIVE_BUTTON = "NEGATIVE_BUTTON";


    /**
     * Create dialog instance. (OK/Cancel button)
     * @param message Message.
     * @param title Title.
     * @return Dialog instance.
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
     * Create dialog instance. (Custom button)
     * @param message Message.
     * @param title Title.
     * @param positiveButton Positive button label.
     * @param neutralButton  Neutral button label.
     * @param negativeButton Negative button label.
     * @return Dialog instance.
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
     * onCreateDialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Bundle args = getArguments();

        // dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(args.getCharSequence(ARG_TITLE));
        builder.setMessage(args.getCharSequence(ARG_MESSAGE));

        // button.
        if (args.getBoolean(ARG_IS_BUTTON_DEFAULT)) {
            builder.setPositiveButton(android.R.string.ok, clickListener);
            builder.setNeutralButton(android.R.string.cancel, clickListener);
        } else {
            // Positive button
            String positive = args.getString(ARG_POSITIVE_BUTTON);
            if (!TextUtils.isEmpty(positive)) {
                builder.setPositiveButton(positive, clickListener);
            }
            // Neutral button
            String neutral = args.getString(ARG_NEUTRAL_BUTTON);
            if (!TextUtils.isEmpty(neutral)) {
                builder.setNeutralButton(neutral, clickListener);
            }
            // Negative button
            String negative = args.getString(ARG_NEGATIVE_BUTTON);
            if (!TextUtils.isEmpty(negative)) {
                builder.setNegativeButton(negative, clickListener);
            }
        }

        return  builder.create();
    }

}
