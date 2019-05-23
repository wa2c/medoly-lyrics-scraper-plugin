package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog

import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import com.wa2c.android.medoly.plugin.action.lyricsscraper.R
import com.wa2c.android.medoly.plugin.action.lyricsscraper.databinding.DialogAboutBinding
import timber.log.Timber
import java.util.regex.Pattern

/**
 * About dialog.
 */
class AboutDialogFragment : AbstractDialogFragment() {
    private lateinit var binding: DialogAboutBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_about, null, false)

        // Version
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
            binding.dialogAboutAppVersionTextView.text = getString(R.string.label_dialog_about_ver, packageInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }

        // Email
        val email = getString(R.string.app_mail_name) + "@" + getString(R.string.app_mail_domain)
        binding.dialogAboutEmailTextView.text = email

        // License
        Linkify.addLinks(
                binding.dialogAboutLicenseTextView,
                Pattern.compile(getString(R.string.app_license)),
                getString(R.string.app_license_url), null,
                Linkify.TransformFilter { _, _ -> getString(R.string.app_license_url) })

        // Privacy Policy
        Linkify.addLinks(
                binding.dialogAboutPrivacyPolicyTextView,
                Pattern.compile(getString(R.string.label_dialog_about_link)),
                getString(R.string.app_privacy_policy_url), null,
                Linkify.TransformFilter { _, _ -> getString(R.string.app_privacy_policy_url) })

        // Google Play
        Linkify.addLinks(
                binding.dialogAboutGooglePlayTextView,
                Pattern.compile(getString(R.string.label_dialog_about_link)),
                getString(R.string.app_market_web), null,
                Linkify.TransformFilter { _, _ -> getString(R.string.app_market_web) })


        // Library
        val libraryNames = resources.getStringArray(R.array.about_library_names)
        val libraryUrls = resources.getStringArray(R.array.about_library_urls)
        for (i in libraryNames.indices) {
            val libTextView = TextView(context)
            libTextView.movementMethod = LinkMovementMethod.getInstance()
            libTextView.text = HtmlCompat.fromHtml("<a href=\"" + libraryUrls[i] + "\">" + libraryNames[i] + "</a>", HtmlCompat.FROM_HTML_MODE_COMPACT)
            libTextView.gravity = Gravity.CENTER_HORIZONTAL
            libTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            binding.dialogAboutLibraryLayout.setPadding(2, 2, 2, 2)
            binding.dialogAboutLibraryLayout.addView(libTextView)
        }

        // Build
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.pref_title_about)
        builder.setView(binding.root)
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
