package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger
import kotlinx.android.synthetic.main.dialog_about.view.*
import java.util.regex.Pattern

/**
 * About dialog.
 */
class AboutDialogFragment : AbstractDialogFragment() {

    /**
     * onCreateDialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        super.onCreateDialog(savedInstanceState)
        val content = View.inflate(activity, R.layout.dialog_about, null)

        // Version
        try {
            val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, PackageManager.GET_ACTIVITIES)
            content.dialogAboutAppVersionTextView.text = getString(R.string.label_dialog_about_ver, packageInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.e(e)
        }

        // Email
        content.dialogAboutEmailTextView.text = getString(R.string.app_mail_name) + "@" + getString(R.string.app_mail_domain)

        // License
        Linkify.addLinks(
                content.dialogAboutLicenseTextView,
                Pattern.compile(getString(R.string.app_license)),
                getString(R.string.app_license_url), null,
                Linkify.TransformFilter { _, _ -> getString(R.string.app_license_url) })

        // Privacy Policy
        Linkify.addLinks(
                content.dialogAboutPrivacyPolicyTextView,
                Pattern.compile(getString(R.string.label_dialog_about_link)),
                getString(R.string.app_privacy_policy_url), null,
                Linkify.TransformFilter { _, _ -> getString(R.string.app_privacy_policy_url) })

        // Google Play
        Linkify.addLinks(
                content.dialogAboutGooglePlayTextView,
                Pattern.compile(getString(R.string.label_dialog_about_link)),
                getString(R.string.app_market_web), null,
                Linkify.TransformFilter { _, _ -> getString(R.string.app_market_web) })


        // Library
        val libraryNames = resources.getStringArray(R.array.about_library_names)
        val libraryUrls = resources.getStringArray(R.array.about_library_urls)
        for (i in libraryNames.indices) {
            val libTextView = TextView(activity)
            libTextView.movementMethod = LinkMovementMethod.getInstance()
            if (Build.VERSION.SDK_INT >= 24) {
                libTextView.text = Html.fromHtml("<a href=\"" + libraryUrls[i] + "\">" + libraryNames[i] + "</a>", Html.FROM_HTML_MODE_COMPACT)
            } else {
                libTextView.text = Html.fromHtml("<a href=\"" + libraryUrls[i] + "\">" + libraryNames[i] + "</a>")
            }
            libTextView.gravity = Gravity.CENTER_HORIZONTAL
            libTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            content.dialogAboutLibraryLayout.setPadding(2, 2, 2, 2)
            content.dialogAboutLibraryLayout.addView(libTextView)
        }

        // Build
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.pref_title_about)
        builder.setView(content)
        builder.setNeutralButton(android.R.string.ok, null)
        return builder.create()
    }

    companion object {
        /**
         * Create dialog instance.
         * @return Dialog instance.
         */
        fun newInstance(): AboutDialogFragment {
            return AboutDialogFragment()
        }
    }

}
