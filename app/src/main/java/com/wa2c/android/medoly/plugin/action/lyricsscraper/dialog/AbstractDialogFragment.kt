package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import com.wa2c.android.prefs.Prefs

/**
 * Abstract dialog class.
 */
abstract class AbstractDialogFragment : DialogFragment() {

    /** Prefs */
    protected lateinit var prefs: Prefs
    /** Click listener. */
    var clickListener: ((dialog: DialogInterface?, which: Int, bundle: Bundle?) -> Unit)? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        prefs = Prefs(activity)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dialog?.cancel() // close on rotation
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as AlertDialog?
        if (dialog != null) {
            if (dialog.getButton(DialogInterface.BUTTON_POSITIVE) != null)
                setPositiveButton(dialog, dialog.getButton(DialogInterface.BUTTON_POSITIVE))
            if (dialog.getButton(DialogInterface.BUTTON_NEGATIVE) != null)
                setNegativeButton(dialog, dialog.getButton(DialogInterface.BUTTON_NEGATIVE))
            if (dialog.getButton(DialogInterface.BUTTON_NEUTRAL) != null)
                setNeutralButton(dialog, dialog.getButton(DialogInterface.BUTTON_NEUTRAL))
        }
    }

    override fun onStop() {
        super.onStop()
        dialog?.cancel()
    }

    /**
     * Set positive button
     */
    protected open fun setPositiveButton(dialog: AlertDialog, button: Button) {
        button.setOnClickListener {
            clickListener?.invoke(dialog, DialogInterface.BUTTON_POSITIVE, null)
            dialog.dismiss()
        }
    }

    /**
     * Set negative button
     */
    protected open fun setNegativeButton(dialog: AlertDialog, button: Button) {
        button.setOnClickListener {
            clickListener?.invoke(dialog, DialogInterface.BUTTON_NEGATIVE, null)
            dialog.dismiss()
        }
    }

    /**
     * Set neutral button
     */
    protected open fun setNeutralButton(dialog: AlertDialog, button: Button) {
        button.setOnClickListener {
            clickListener?.invoke(dialog, DialogInterface.BUTTON_NEUTRAL, null)
            dialog.dismiss()
        }
    }

    /**
     * Fragment tag.
     */
    private val fragmentTag: String by lazy { this.javaClass.name }

    /***
     * Show dialog.
     * @param activity A activity.
     */
    fun show(activity: Activity) {
        val manager = activity.fragmentManager
        val fragment = manager.findFragmentByTag(fragmentTag) as? AbstractDialogFragment
        fragment?.dismiss()
        super.show(activity.fragmentManager, fragmentTag)
    }

}
