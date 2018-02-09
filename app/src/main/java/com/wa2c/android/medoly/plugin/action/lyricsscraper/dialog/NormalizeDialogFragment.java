package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils;


/**
 * Normalize dialog.
 */
public class NormalizeDialogFragment extends AbstractDialogFragment {

    // argument keys
    private final static String ARG_INITIAL_TEXT = "ARG_INITIAL_TEXT";
    private final static String ARG_INPUT_TEXT = "ARG_INPUT_TEXT";

    /**
     * Create dialog instance.
     * @param text Text.
     * @param initialText Initial text.
     * @return Dialog instance.
     */
    static public NormalizeDialogFragment newInstance(String text,  String initialText) {
        NormalizeDialogFragment fragment = new NormalizeDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_TEXT, initialText);
        args.putString(ARG_INPUT_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }





    private TextView dialogNormalizeBeforeTextView;
    private TextView dialogNormalizeAfterTextView;
    private CheckBox dialogNormalizeCheckBox;
    private CheckBox dialogNormalizeParenthesesCheckBox;
    private CheckBox dialogNormalizeDashCheckBox;
    private CheckBox dialogNormalizeInfoCheckBox;
    private Button dialogNormalizeResetButton;

    private String initialText;
    private String inputText;

    public String getInputText() {
        return inputText;
    }

    /**
     * onCreateDialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.context = getActivity();

        View baseView = View.inflate(context, R.layout.dialog_normalize, null);
        dialogNormalizeBeforeTextView = (TextView)baseView.findViewById(R.id.dialogNormalizeBeforeTextView);
        dialogNormalizeAfterTextView = (TextView)baseView.findViewById(R.id.dialogNormalizeAfterTextView);
        dialogNormalizeCheckBox = (CheckBox)baseView.findViewById(R.id.dialogNormalizeCheckBox);
        dialogNormalizeParenthesesCheckBox = (CheckBox)baseView.findViewById(R.id.dialogNormalizeParenthesesCheckBox);
        dialogNormalizeDashCheckBox = (CheckBox)baseView.findViewById(R.id.dialogNormalizeDashCheckBox);
        dialogNormalizeInfoCheckBox = (CheckBox)baseView.findViewById(R.id.dialogNormalizeInfoCheckBox);
        dialogNormalizeResetButton = (Button)baseView.findViewById(R.id.dialogNormalizeResetButton);

        initialText = getArguments().getString(ARG_INITIAL_TEXT);
        inputText = getArguments().getString(ARG_INPUT_TEXT);
        dialogNormalizeBeforeTextView.setText(inputText);
        dialogNormalizeAfterTextView.setText(inputText);

        if (TextUtils.isEmpty(initialText)) {
            dialogNormalizeResetButton.setVisibility(View.GONE);
        }

        dialogNormalizeCheckBox.setOnCheckedChangeListener(textCheckChangeListener);
        dialogNormalizeParenthesesCheckBox.setOnCheckedChangeListener(textCheckChangeListener);
        dialogNormalizeDashCheckBox.setOnCheckedChangeListener(textCheckChangeListener);
        dialogNormalizeInfoCheckBox.setOnCheckedChangeListener(textCheckChangeListener);
        dialogNormalizeResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogNormalizeBeforeTextView.setText(initialText);
                setAfterText();
            }
        });

        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(baseView);
        builder.setTitle(R.string.title_dialog_normalize);
        builder.setNeutralButton(R.string.label_close, null);
        builder.setPositiveButton(R.string.label_edit, clickListener);
        return  builder.create();
    }



    private CompoundButton.OnCheckedChangeListener textCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setAfterText();
        }
    };

    private void setAfterText() {
        String text = dialogNormalizeBeforeTextView.getText().toString();
        if (dialogNormalizeCheckBox.isChecked()) {
            text = AppUtils.normalizeText(text);
        }
        if (dialogNormalizeParenthesesCheckBox.isChecked()) {
            text = AppUtils.removeParentheses(text);
        }
        if (dialogNormalizeDashCheckBox.isChecked()) {
            text = AppUtils.removeDash(text);
        }
        if (dialogNormalizeInfoCheckBox.isChecked()) {
            text = AppUtils.removeTextInfo(text);
        }
        inputText =  AppUtils.trimLines(text);
        dialogNormalizeAfterTextView.setText(text);
    }

}
