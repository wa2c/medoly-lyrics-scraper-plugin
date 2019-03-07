package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils
import kotlinx.android.synthetic.main.dialog_normalize.*
import kotlinx.android.synthetic.main.dialog_normalize.view.*


/**
 * Normalize dialog.
 */
class NormalizeDialogFragment : AbstractDialogFragment() {

    /** Initial text. */
    private var initialText: String? = null

    /** Input text. */
    var inputText: String? = null
        private set

    /** Check change listener */
    private val textCheckChangeListener = CompoundButton.OnCheckedChangeListener { _, _ -> setAfterText() }

    /**
     * onCreateDialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val contentView = View.inflate(activity, R.layout.dialog_normalize, null)

        initialText = arguments.getString(ARG_INITIAL_TEXT)
        inputText = arguments.getString(ARG_INPUT_TEXT)
        contentView.dialogNormalizeBeforeTextView.text = inputText
        contentView.dialogNormalizeAfterTextView.text = inputText

        if (initialText.isNullOrEmpty()) {
            contentView.dialogNormalizeResetButton.visibility = View.GONE
        }

        contentView.dialogNormalizeCheckBox.setOnCheckedChangeListener(textCheckChangeListener)
        contentView.dialogNormalizeParenthesesCheckBox.setOnCheckedChangeListener(textCheckChangeListener)
        contentView.dialogNormalizeDashCheckBox.setOnCheckedChangeListener(textCheckChangeListener)
        contentView.dialogNormalizeInfoCheckBox.setOnCheckedChangeListener(textCheckChangeListener)
        contentView.dialogNormalizeResetButton.setOnClickListener {
            contentView.dialogNormalizeBeforeTextView.text = initialText
            setAfterText()
        }



        // build dialog
        val builder = AlertDialog.Builder(activity)
        builder.setView(contentView)
        builder.setTitle(R.string.title_dialog_normalize)
        builder.setNeutralButton(R.string.label_close, null)
        builder.setPositiveButton(R.string.label_edit, null)

        return builder.create()
    }

    private fun setAfterText() {
        var text = dialog.dialogNormalizeBeforeTextView.text.toString()
        if (dialog.dialogNormalizeCheckBox.isChecked) {
            text = AppUtils.normalizeText(text)
        }
        if (dialog.dialogNormalizeParenthesesCheckBox.isChecked) {
            text = AppUtils.removeParentheses(text)
        }
        if (dialog.dialogNormalizeDashCheckBox.isChecked) {
            text = AppUtils.removeDash(text)
        }
        if (dialog.dialogNormalizeInfoCheckBox.isChecked) {
            text = AppUtils.removeTextInfo(text)
        }
        inputText = AppUtils.trimLines(text)
        dialog.dialogNormalizeAfterTextView.text = text
    }

    companion object {

        // argument keys
        private const val ARG_INITIAL_TEXT = "ARG_INITIAL_TEXT"
        private const val ARG_INPUT_TEXT = "ARG_INPUT_TEXT"

        /**
         * Create dialog instance.
         * @param text Text.
         * @param initialText Initial text.
         * @return Dialog instance.
         */
        fun newInstance(text: String?, initialText: String?): NormalizeDialogFragment {
            val fragment = NormalizeDialogFragment()
            val args = Bundle()
            args.putString(ARG_INITIAL_TEXT, initialText)
            args.putString(ARG_INPUT_TEXT, text)
            fragment.arguments = args
            return fragment
        }
    }

}
