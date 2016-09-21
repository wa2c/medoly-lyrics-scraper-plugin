package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.widget.Toast;

import java.text.Normalizer;


/**
 * アプリユーティリティ。
 */
public class AppUtils {

    public static void showToast(Context context, String text) {
        ToastReceiver.showToast(context, text);
    }

    public static void showToast(Context context, int stringId) {
        ToastReceiver.showToast(context, stringId);
    }

    /**
     * 歌詞を調整する。
     * @param text 歌詞テキスト。
     * @return 調整後の歌詞テキスト。
     */
    public static String adjustLyrics(String text) {
        if (TextUtils.isEmpty(text))
            return null;

        // タグ除去
        text = Html.fromHtml(text).toString();
        //text = text.replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "");

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
    private static String removeParentheses(String text) {
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
}
