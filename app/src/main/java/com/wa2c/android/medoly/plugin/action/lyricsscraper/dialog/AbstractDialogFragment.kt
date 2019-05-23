package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.wa2c.android.prefs.Prefs

/**
 * Abstract dialog class.
 */
abstract class AbstractDialogFragment : DialogFragment() {

    /** The called activity.  */
    protected lateinit var context: FragmentActivity
    /** Prefs */
    protected lateinit var prefs: Prefs
    /** Click listener. */
    var clickListener: ((dialog: DialogInterface?, which: Int, bundle: Bundle?) -> Unit)? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        context = activity!!
        prefs = Prefs(context)
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
    fun show(activity: FragmentActivity?) {
        if (activity == null)
            return
        val manager = activity.supportFragmentManager
        val fragment = manager.findFragmentByTag(fragmentTag) as? AbstractDialogFragment
        fragment?.dismiss()
        super.show(manager, fragmentTag)
    }

}
