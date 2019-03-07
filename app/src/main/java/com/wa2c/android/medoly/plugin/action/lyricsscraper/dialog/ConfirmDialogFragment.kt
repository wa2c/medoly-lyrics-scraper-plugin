package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle

/**
 * Confirmation dialog
 */
class ConfirmDialogFragment : AbstractDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val args = arguments

        // dialog build
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(args.getCharSequence(ARG_TITLE))
        builder.setMessage(args.getCharSequence(ARG_MESSAGE))


        // button
        if (args.getBoolean(ARG_IS_BUTTON_DEFAULT)) {
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setNeutralButton(android.R.string.cancel, null)
        } else {
            // Positive button
            val positive = args.getString(ARG_POSITIVE_BUTTON)
            if (!positive.isNullOrEmpty()) {
                builder.setPositiveButton(positive, null)
            }
            // Neutral button
            val neutral = args.getString(ARG_NEUTRAL_BUTTON)
            if (!neutral.isNullOrEmpty()) {
                builder.setNeutralButton(neutral, null)
            }
            // Negative button
            val negative = args.getString(ARG_NEGATIVE_BUTTON)
            if (!negative.isNullOrEmpty()) {
                builder.setNegativeButton(negative, null)
            }
        }

        return builder.create()
    }

    companion object {

        /** Title key.  */
        private const val ARG_TITLE = "TITLE"
        /** Message key. */
        private const val ARG_MESSAGE = "MESSAGE"


        private const val ARG_IS_BUTTON_DEFAULT = "IS_BUTTON_DEFAULT"
        /** Positive button key.  */
        private const val ARG_POSITIVE_BUTTON = "POSITIVE_BUTTON"
        /** Neutral button key.  */
        private const val ARG_NEUTRAL_BUTTON = "NEUTRAL_BUTTON"
        /** Negative button key.  */
        private const val ARG_NEGATIVE_BUTTON = "NEGATIVE_BUTTON"

        /**
         * Create dialog instance. (OK/Cancel)
         * @param message A message.
         * @param title A title.
         * @return New dialog instance.
         */
        fun newInstance(message: CharSequence, title: CharSequence?): ConfirmDialogFragment {
            val args = Bundle()
            args.putCharSequence(ARG_MESSAGE, message)
            args.putCharSequence(ARG_TITLE, title)
            args.putBoolean(ARG_IS_BUTTON_DEFAULT, true)

            val fragment = ConfirmDialogFragment()
            fragment.arguments = args
            return fragment
        }

        /**
         * Create dialog instance. (Customized button)
         * @param message A message.
         * @param title A title.
         * @param positiveButton Positive button. None if null.
         * @param neutralButton  Neutral button. None if null.
         * @param negativeButton Negative button. None if null.
         * @return New dialog instance.
         */
        fun newInstance(message: CharSequence, title: CharSequence?, positiveButton: String?, neutralButton: String?, negativeButton: String?): ConfirmDialogFragment {
            val args = Bundle()
            args.putCharSequence(ARG_MESSAGE, message)
            args.putCharSequence(ARG_TITLE, title)
            args.putString(ARG_POSITIVE_BUTTON, positiveButton)
            args.putString(ARG_NEUTRAL_BUTTON, neutralButton)
            args.putString(ARG_NEGATIVE_BUTTON, negativeButton)
            args.putBoolean(ARG_IS_BUTTON_DEFAULT, false)

            val fragment = ConfirmDialogFragment()
            fragment.arguments = args
            return fragment
        }

    }

}
