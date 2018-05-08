package com.wa2c.android.medoly.plugin.action.lyricsscraper.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.text.Html
import com.wa2c.android.medoly.library.ExtraData
import com.wa2c.android.medoly.library.MediaPluginIntent
import com.wa2c.android.medoly.library.PropertyData

import com.wa2c.android.medoly.plugin.action.lyricsscraper.R

import java.text.Normalizer


/**
 * App utilities.
 */
object AppUtils {


    /** Request code  */
    const val REQUEST_CODE_SAVE_FILE = 1

    /**
     * Show message.
     * @param context context.
     * @param text message.
     */
    fun showToast(context: Context, text: String) {
        ToastReceiver.showToast(context, text)
    }

    /**
     * Show message.
     * @param context context
     * @param stringId resource id.
     */
    fun showToast(context: Context, stringId: Int) {
        ToastReceiver.showToast(context, stringId)
    }



    /**
     * Get first non-null object.
     * @param objects Objects.
     * @return First non-null object. null as all null.
     */
    fun <T> coalesce(vararg objects: T): T? {
        return objects.firstOrNull { it != null }
    }

    /**
     * Get first non-empty text.
     * @param texts Texts.
     * @return First non-null object. empty text as all null.
     */
    fun coalesce(vararg texts: String?): String {
        for (text in texts) {
            if (!text.isNullOrEmpty())
                return text!!
        }
        return ""
    }


    /**
     * Adjust lyrics. (remove tags, trim lines)
     * @param inputText lyrics text.
     * @return adjusted lyrics text.
     */
    fun adjustLyrics(inputText: String?): String? {
        if (inputText.isNullOrEmpty() || inputText == "null") // "null" for  JOYSOUND
            return null

        // Remove tags
        var text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(inputText, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(inputText).toString()
        }

        // Trimming
        text = trimLines(text)

        return text
    }

    /**
     * Trim spaces including full-width characters.
     * @param text Text.
     * @return Trimmed text.
     */
    fun trimLines(text: String?): String {
        return if (text.isNullOrEmpty()) "" else text!!
                .replace("(?m)^[\\t 　]*".toRegex(), "")
                .replace("(?m)[\\t 　]*$".toRegex(), "")
                .trim { it <= ' ' }
    }

    /**
     * Normalize text.
     * @param text text.
     * @return Normalized text.
     */
    fun normalizeText(text: String?): String {
        if (text.isNullOrEmpty())
            return ""

        // normalize
        val output = trimLines(Normalizer.normalize(text, Normalizer.Form.NFKC)).toLowerCase()
        // change special characters
        return output
                .replace("゠", "=")
                .replace("(“|”)", "\"")
                .replace("(‘|’)", "\'")
    }

    /**
     * Remove parentheses.
     * @param text text.
     * @return removed text.
     */
    fun removeParentheses(text: String?): String {
        return if (text.isNullOrEmpty()) "" else text!!
                .replace("([^\\(]+)\\(.*?\\)".toRegex(), "$1")
                .replace("([^\\[]+)\\[.*?\\]".toRegex(), "$1")
                .replace("([^\\{]+)\\{.*?\\}".toRegex(), "$1")
                .replace("([^\\<]+)\\<.*?\\>".toRegex(), "$1")
                .replace("([^\\（]+)\\（.*?\\）".toRegex(), "$1")
                .replace("([^\\［]+)\\［.*?\\］".toRegex(), "$1")
                .replace("([^\\｛]+)\\｛.*?\\｝".toRegex(), "$1")
                .replace("([^\\＜]+)\\＜.*?\\＞".toRegex(), "$1")
                .replace("([^\\【]+)\\【.*?\\】".toRegex(), "$1")
                .replace("([^\\〔]+)\\〔.*?\\〕".toRegex(), "$1")
                .replace("([^\\〈]+)\\〈.*?\\〉".toRegex(), "$1")
                .replace("([^\\《]+)\\《.*?\\》".toRegex(), "$1")
                .replace("([^\\「]+)\\「.*?\\」".toRegex(), "$1")
                .replace("([^\\『]+)\\『.*?\\』".toRegex(), "$1")
                .replace("([^\\〖]+)\\〖.*?\\〗".toRegex(), "$1")
    }

    /**
     * Remove text after dash characters.
     * @param text text.
     * @return removed text.
     */
    fun removeDash(text: String?): String {
        return if (text.isNullOrEmpty()) "" else text!!
                .replace("\\s+(-|－|―|ー|ｰ|~|～|〜|〰|=|＝).*".toRegex(), "")
    }

    /**
     * Remove attached info.
     * @param text text.
     * @return removed text.
     */
    fun removeTextInfo(text: String?): String {
        return if (text.isNullOrEmpty()) "" else text!!
                .replace("(?i)[\\(\\<\\[\\{\\s]?off vocal.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?no vocal.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?less vocal.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?without.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?w/o.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?backtrack.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?backing track.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?karaoke.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?カラオケ.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?からおけ.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?歌無.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?vocal only.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?instrumental.*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?inst\\..*".toRegex(), "")
                .replace("(?i)[\\(\\<\\[\\{\\s]?インスト.*".toRegex(), "")
    }

    /**
     * Save file.
     * @param activity A activity.
     * @param inputTitle Title (searching text).
     * @param inputArtist Artist (searching text).
     */
    fun saveFile(activity: Activity, inputTitle: String?, inputArtist: String?) {
        var title = inputTitle
        var artist = inputArtist
        try {
            if (title == null)
                title = ""
            if (artist == null)
                artist = ""

            val pref = PreferenceManager.getDefaultSharedPreferences(activity)

            val defaultNameKey = activity.getString(R.string.pref_file_name_default)
            val defaultName = pref.getString(defaultNameKey, activity.getString(R.string.file_name_default_default))

            val separatorKey = activity.getString(R.string.pref_file_name_separator)
            val separator = pref.getString(separatorKey, activity.getString(R.string.file_name_separator_default))

            val fileName  = when (defaultName) {
                "TITLE_ARTIST" -> title + separator + artist
                "ARTIST_TITLE" -> artist + separator + title
                else -> title
            }

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_TITLE, "$fileName.txt")
            activity.startActivityForResult(intent, REQUEST_CODE_SAVE_FILE)
        } catch (e: Exception) {
            Logger.e(e)
            showToast(activity, R.string.error_app)
        }

    }

    /**
     * Send result.
     * @param context A context.
     * @param pluginIntent A plugin intent.
     * @param resultProperty A result property data.
     * @param resultExtra A result extra data.
     */
    fun sendResult(context: Context, pluginIntent: MediaPluginIntent, resultProperty: PropertyData? = null, resultExtra: ExtraData? = null) {
        context.sendBroadcast(pluginIntent.createResultIntent(resultProperty, resultExtra))
    }

}
