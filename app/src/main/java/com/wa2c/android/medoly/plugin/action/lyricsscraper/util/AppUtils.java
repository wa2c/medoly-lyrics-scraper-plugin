package com.wa2c.android.medoly.plugin.action.lyricsscraper.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;
import com.wa2c.android.medoly.plugin.action.lyricsscraper.service.ProcessService;

import java.text.Normalizer;


/**
 * App utilities.
 */
public class AppUtils {

    /**
     * Show message.
     * @param context context.
     * @param text message.
     */
    public static void showToast(Context context, String text) {
        ToastReceiver.showToast(context, text);
    }

    /**
     * Show message.
     * @param context context
     * @param stringId resource id.
     */
    public static void showToast(Context context, int stringId) {
        ToastReceiver.showToast(context, stringId);
    }



    /**
     * Get first non-null object.
     * @param objects Objects.
     * @return First non-null object. null as all null.
     */
    public static <T> T coalesce(T... objects) {
        if (objects == null)
            return null;
        for (T obj : objects) {
            if (obj != null)
                return obj;
        }
        return null;
    }

    /**
     * Get first non-null text.
     * @param texts Texts.
     * @return First non-null object. empty text as all null.
     */
    public static String coalesce(String... texts) {
        if (texts == null)
            return "";
        for (String text : texts) {
            if (!TextUtils.isEmpty(text))
                return text;
        }
        return "";
    }



    /**
     * 歌詞を調整する。
     * @param text 歌詞テキスト。
     * @return 調整後の歌詞テキスト。
     */
    @SuppressWarnings("deprecation")
    public static String adjustLyrics(String text) {
        if (TextUtils.isEmpty(text))
            return null;

        // タグ除去
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            text = Html.fromHtml(text).toString();
        }

        // トリミング
        text = trimLines(text);

        return text;
    }

    /**
     * 全角を含めてトリミング。
     * @param text 元テキスト。
     * @return トリミングテキスト。
     */
    public static String trimLines(String text) {
        if (TextUtils.isEmpty(text))
            return "";

        return text.replaceAll("(?m)^[\\t 　]*", "").replaceAll("(?m)[\\t 　]*$", "").trim();
    }



    /**
     * 比較向けにテキストをノーマライズ。
     * @param text テキスト。
     * @return 変換後テキスト。
     */
    public static String normalizeText(String text) {
        if (TextUtils.isEmpty(text))
            return "";

        // 正規化
        String output = trimLines(removeParentheses(Normalizer.normalize(text, Normalizer.Form.NFKC)).toLowerCase());
        // 特殊文字正規化
        return output
                .replace("゠", "=")
                .replace("(“|”)", "\"")
                .replace("(‘|’)", "\'");
    }

    /**
     * 括弧で括られた文字等（補助文字）を取り除く
     * @param text テキスト。
     * @return 括弧を取り除いたテキスト。
     */
    public static String removeParentheses(String text) {
        if (TextUtils.isEmpty(text))
            return "";

        return text
                .replaceAll("(^[^\\(]+)\\(.*?\\)", "$1")
                .replaceAll("(^[^\\[]+)\\[.*?\\]", "$1")
                .replaceAll("(^[^\\{]+)\\{.*?\\}", "$1")
                .replaceAll("(^[^\\<]+)\\<.*?\\>", "$1")
                .replaceAll("(^[^\\（]+)\\（.*?\\）", "$1")
                .replaceAll("(^[^\\［]+)\\［.*?\\］", "$1")
                .replaceAll("(^[^\\｛]+)\\｛.*?\\｝", "$1")
                .replaceAll("(^[^\\＜]+)\\＜.*?\\＞", "$1")
                .replaceAll("(^[^\\【]+)\\【.*?\\】", "$1")
                .replaceAll("(^[^\\〔]+)\\〔.*?\\〕", "$1")
                .replaceAll("(^[^\\〈]+)\\〈.*?\\〉", "$1")
                .replaceAll("(^[^\\《]+)\\《.*?\\》", "$1")
                .replaceAll("(^[^\\「]+)\\「.*?\\」", "$1")
                .replaceAll("(^[^\\『]+)\\『.*?\\』", "$1")
                .replaceAll("(^[^\\〖]+)\\〖.*?\\〗", "$1")
                .replaceAll("(^[^\\-]+)-.*?-", "$1")
                .replaceAll("(^[^\\－]+)－.*?－", "$1")
                .replaceAll(" (~|～|〜|〰).*", "");

    }

    /**
     * Remove text after dash characters.
     * @param text text.
     * @return removed text.
     */
    public static String removeDash(String text) {
        if (TextUtils.isEmpty(text))
            return "";

        return text
                .replaceAll("\\s+(-|－|―|ー|ｰ|~|～|〜|〰|=|＝).*", "");
    }

    /**
     * Remove attached info.
     * @param text text.
     * @return removed text.
     */
    public static String removeTextInfo(String text) {
        if (TextUtils.isEmpty(text))
            return "";

        return text
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?off vocal.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?no vocal.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?less vocal.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?without.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?w/o.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?backtrack.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?backing track.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?karaoke.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?カラオケ.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?からおけ.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?歌無.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?vocal only.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?instrumental.*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?inst\\..*", "")
                .replaceAll("(?i)[\\(\\<\\[\\{\\s]?インスト.*", "")
                ;
    }


    /**
     * 2つのテキストを比較して、ほぼ同じ場合はtrue。
     * @param text1 比較テキスト1。
     * @param text2 比較テキスト2。
     * @return ほぼ一致する場合はtrue。
     */
    public static boolean similarText(String text1, String text2) {
        if (TextUtils.isEmpty(text1) || TextUtils.isEmpty(text2))
            return false;

        String it = removeWhitespace(normalizeText(text1), false);
        String ot = removeWhitespace(normalizeText(text2), false);
        return it.equals(ot);
    }

    /**
     * 空白を置換える
     * @param text テキスト。
     * @param insertSpace スペースに置換える場合はtrue。
     * @return 変換後テキスト。
     */
    private static String removeWhitespace(String text, boolean insertSpace) {
        if (TextUtils.isEmpty(text))
            return "";

        return text.replaceAll("(\\s|　)", insertSpace ? " " : "");
    }



    /** Request code */
    public static final int REQUEST_CODE_SAVE_FILE = 1;

    /**
     * Save file.
     * @param activity A activity.
     * @param title Title (searching text).
     * @param artist Artist (searching text).
     */
    public static void saveFile(@NonNull Activity activity, String title, String artist) {
        try {
            if (title == null)
                title = "";
            if (artist == null)
                artist = "";

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);

            String defaultNameKey = activity.getString(R.string.pref_file_name_default);
            String defaultName = pref.getString(defaultNameKey, activity.getString(R.string.file_name_default_default));

            String separatorKey = activity.getString(R.string.pref_file_name_separator);
            String separator = pref.getString(separatorKey, activity.getString(R.string.file_name_separator_default));

            String fileName;
            switch (defaultName) {
                case "TITLE_ARTIST":
                    fileName = title + separator + artist;
                    break;
                case "ARTIST_TITLE":
                    fileName = artist + separator + title;
                    break;
                default:
                    fileName = title;
                    break;
            }

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE, fileName + ".lrc");
            activity.startActivityForResult(intent, REQUEST_CODE_SAVE_FILE);
        } catch (Exception e) {
            Logger.e(e);
            showToast(activity, R.string.error_app);
        }
    }
}
