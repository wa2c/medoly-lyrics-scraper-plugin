package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.databinding.DialogNormalizeBinding
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.AppUtils


/**
 * Normalize dialog.
 */
class NormalizeDialogFragment : AbstractDialogFragment() {

    private lateinit var binding: DialogNormalizeBinding

    /** Initial text. */
    private var initialText: String? = null

    /** Input text. */
    var inputText: String? = null
        private set

    /** Check change listener */
    private val textCheckChangeListener = CompoundButton.OnCheckedChangeListener { _, _ -> setAfterText() }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_normalize, null, false)

        val args = arguments!!
        initialText = args.getString(ARG_INITIAL_TEXT)
        inputText = args.getString(ARG_INPUT_TEXT)
        binding.dialogNormalizeBeforeTextView.text = inputText
        binding.dialogNormalizeAfterTextView.text = inputText

        if (initialText.isNullOrEmpty()) {
            binding.dialogNormalizeResetButton.visibility = View.GONE
        }

        binding.dialogNormalizeCheckBox.setOnCheckedChangeListener(textCheckChangeListener)
        binding.dialogNormalizeParenthesesCheckBox.setOnCheckedChangeListener(textCheckChangeListener)
        binding.dialogNormalizeDashCheckBox.setOnCheckedChangeListener(textCheckChangeListener)
        binding.dialogNormalizeInfoCheckBox.setOnCheckedChangeListener(textCheckChangeListener)
        binding.dialogNormalizeResetButton.setOnClickListener {
            binding.dialogNormalizeBeforeTextView.text = initialText
            setAfterText()
        }



        // build dialog
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        builder.setTitle(R.string.title_dialog_normalize)
        builder.setNeutralButton(R.string.label_close, null)
        builder.setPositiveButton(R.string.label_edit, null)

        return builder.create()
    }

    private fun setAfterText() {
        var text = binding.dialogNormalizeBeforeTextView.text.toString()
        if (binding.dialogNormalizeCheckBox.isChecked) {
            text = AppUtils.normalizeText(text)
        }
        if (binding.dialogNormalizeParenthesesCheckBox.isChecked) {
            text = AppUtils.removeParentheses(text)
        }
        if (binding.dialogNormalizeDashCheckBox.isChecked) {
            text = AppUtils.removeDash(text)
        }
        if (binding.dialogNormalizeInfoCheckBox.isChecked) {
            text = AppUtils.removeTextInfo(text)
        }
        inputText = AppUtils.trimLines(text)
        binding.dialogNormalizeAfterTextView.text = text
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
