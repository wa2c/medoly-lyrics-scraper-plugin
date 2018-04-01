package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.app.Fragment
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Prefs
import java.util.*

/**
 * Abstract dialog class.
 */
abstract class AbstractDialogFragment : DialogFragment() {

    companion object {
        private val shownDialogMap = HashMap<String, DialogFragment>()
    }

    /** Preferences controller.  */
    protected lateinit var prefs: Prefs

    /** Click listener  */
    var clickListener: DialogInterface.OnClickListener? = null



    /***
     * Show dialog.
     * @param activity A activity.
     */
    fun show(activity: Activity) {
        val key = this.javaClass.name
        shownDialogMap[key]?.dismiss()

        super.show(activity.fragmentManager, key)
        shownDialogMap.put(key, this)
    }

    /**
     * Show dialog.
     * @param fragment A fragment.
     */
    fun show(fragment: Fragment) {
        val key = this.javaClass.name
        shownDialogMap[key]?.dismiss()

        super.show(fragment.fragmentManager, key)
        shownDialogMap.put(key, this)
    }


    /**
     * On create dialog event.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        prefs = Prefs(activity)
        return super.onCreateDialog(savedInstanceState)
    }

    /**
     * On configuration changed event.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dialog?.cancel() // close on rotation
    }

    /**
     * On start event.
     */
    override fun onStart() {
        super.onStart()
    }

    /**
     * On stop event.
     */
    override fun onStop() {
        super.onStop()
        dialog?.cancel()
    }

    /**
     * On dismiss event.
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        shownDialogMap.remove(this.javaClass.name)
    }

    /**
     * On click action.
     * @param dialog A dialog.
     * @param which A clicked button.
     * @param close True if dialog closing.
     */
    fun onClickButton(dialog: DialogInterface?, which: Int, close: Boolean = true) {
        clickListener?.onClick(dialog, which)
        if ( close)
            dialog?.cancel()
    }

}

