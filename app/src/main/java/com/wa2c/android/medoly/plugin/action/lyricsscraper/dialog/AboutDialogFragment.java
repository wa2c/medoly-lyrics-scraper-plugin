package com.wa2c.android.medoly.plugin.action.lyricsscraper.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * About dialog.
 */
public class AboutDialogFragment extends AbstractDialogFragment {

    /**
     * Create dialog instance.
     * @return Dialog instance.
     */
    static public AboutDialogFragment newInstance() {
        return new AboutDialogFragment();
    }



    /**
     * onCreateDialog
     */
    @SuppressWarnings("deprecation")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        final View layoutView = View.inflate(getActivity(), R.layout.dialog_about, null);

        // Version
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo( getActivity().getPackageName(), PackageManager.GET_ACTIVITIES);
            ((TextView)layoutView.findViewById(R.id.dialogAboutAppVersionTextView)).setText(getString(R.string.label_dialog_about_ver, packageInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(e);
        }

        // Email
        ((TextView)layoutView.findViewById(R.id.dialogAboutEmailTextView)).setText(getString(R.string.app_mail_name) + "@" + getString(R.string.app_mail_domain));

        // License
        Linkify.addLinks(
                (TextView)layoutView.findViewById(R.id.dialogAboutLicenseTextView),
                Pattern.compile(getString(R.string.app_license)),
                getString(R.string.app_license_url),
                null,
                new Linkify.TransformFilter() {
                    @Override
                    public String transformUrl(Matcher match, String url) {
                        return getString(R.string.app_license_url);
                    }
                });

        // Privacy Policy
        Linkify.addLinks(
                (TextView)layoutView.findViewById(R.id.dialogAboutPrivacyPolicyTextView),
                Pattern.compile(getString(R.string.label_dialog_about_link)),
                getString(R.string.app_privacy_policy_url),
                null,
                new Linkify.TransformFilter() {
                    @Override
                    public String transformUrl(Matcher match, String url) {
                        return getString(R.string.app_privacy_policy_url);
                    }
                });

        // Google Play
        Linkify.addLinks(
                (TextView)layoutView.findViewById(R.id.dialogAboutGooglePlayTextView),
                Pattern.compile(getString(R.string.label_dialog_about_link)),
                getString(R.string.app_market_web),
                null,
                new Linkify.TransformFilter() {
                    @Override
                    public String transformUrl(Matcher match, String url) {
                        return getString(R.string.app_market_web);
                    }
                });


        // Library
        String[] libraryNames = getResources().getStringArray(R.array.about_library_names);
        String[] libraryUrls = getResources().getStringArray(R.array.about_library_urls);
        LinearLayout libraryLayout = (LinearLayout) layoutView.findViewById(R.id.dialogAboutLibraryLayout);
        for (int i = 0; i < libraryNames.length; i++) {
            TextView libTextView;
            libTextView = new TextView(getActivity());
            libTextView.setMovementMethod(LinkMovementMethod.getInstance());
            if (Build.VERSION.SDK_INT >= 24) {
                libTextView.setText(Html.fromHtml("<a href=\"" + libraryUrls[i] + "\">" +  libraryNames[i] + "</a>", Html.FROM_HTML_MODE_COMPACT));
            } else {
                libTextView.setText(Html.fromHtml("<a href=\"" + libraryUrls[i] + "\">" +  libraryNames[i] + "</a>"));
            }
            libTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            libTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            libraryLayout.setPadding(2, 2, 2, 2);
            libraryLayout.addView(libTextView);
        }

        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pref_title_about);
        builder.setView(layoutView);
        builder.setPositiveButton(android.R.string.ok, null);
        return  builder.create();
    }

}
