package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site);




    }


    /**
     *
     * @param param
     * @return リスト項目ビュー。
     */
    private ViewGroup createParamView(final LyricsObtainParam param) {
        // レイアウト
        final ViewGroup siteParamView = (ViewGroup) View.inflate(this, R.layout.layout_site, null);
        final ListView siteParamListView = (ListView)siteParamView.findViewById(R.id.siteParamListView);


        siteParamListView.setAdapter(new ArrayAdapter<Pair<Integer, String>>(this, android.R.layout.simple_list_item_1, param.getList(this)) {
            // 項目のビュー内容を作成
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                convertView = View.inflate(getContext(), android.R.layout.simple_list_item_1, null);
                TextView titleText = (TextView)convertView.findViewById(android.R.id.text1);
                TextView valueText = (TextView)convertView.findViewById(android.R.id.text2);

                Pair<Integer, String> pair = getItem(position);

                titleText.setText(getString(pair.first));
                valueText.setText(pair.second);

                return convertView;
            }
        });

        return siteParamView;

//        /** 実行アクションIDプレフィックス。 */
//        final String EXECUTE_ID_PREFIX = "execute_id_";
//        /** 実行アクションラベルプレフィックス。 */
//        final String EXECUTE_LABEL_PREFIX = "execute_label_";
//        /** 実行アクションアイコンプレフィックス。 */
//        final String EXECUTE_ICON_PREFIX = "execute_icon_";
//        /** 実行アクションカテゴリ。 */
//        final String EXECUTE_CATEGORY_PREFIX = "execute_type_";
//
//        final PackageManager packageManager = context.getPackageManager();
//        final ComponentInfo componentInfo = info.activityInfo == null ? info.serviceInfo : info.activityInfo;
//        final Bundle metaData = componentInfo.metaData;
//        Resources appResource = null;
//        try {
//            appResource = packageManager.getResourcesForApplication(componentInfo.applicationInfo);
//        } catch (PackageManager.NameNotFoundException e) {
//            Logger.e(e);
//        }
//
//
//
//
//        // レイアウト
//        final ViewGroup classView = (ViewGroup) View.inflate(this, R.layout.layout_site, null);
//
//        // アイコン
//        ImageView iconView = (ImageView)classView.findViewById(R.id.pluginClassIconImageView);
//        Drawable icon = info.loadIcon(packageManager);
//        if (icon != null) iconView.setImageDrawable(icon);
//        else iconView.setVisibility(View.GONE);
//
//        // ラベル
//        TextView labelView = (TextView)classView.findViewById(R.id.pluginClassLabelTextView);
//        CharSequence label = info.loadLabel(packageManager);
//        if (!TextUtils.isEmpty(label)) labelView.setText(label);
//        else labelView.setVisibility(View.INVISIBLE);
//
//
//
//        // 実行アクション
//        boolean existsEvents = false;
//
//        ViewGroup executeLayout = (ViewGroup) classView.findViewById(R.id.pluginExecuteLayout);
//        if (metaData != null) {
//
//            // 実行ID順にソート
//            final Map<String, Integer> orderMap = new HashMap<>();
//            for (final String metaKey : metaData.keySet()) {
//                if (TextUtils.isEmpty(metaKey))
//                    continue;
//                if (metaKey.indexOf(EXECUTE_ID_PREFIX) != 0)
//                    continue;
//                orderMap.put(metaKey, metaData.getInt(metaKey));
//            }
//            final List<Map.Entry<String,Integer>> entries = new ArrayList<>(orderMap.entrySet());
//            Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
//                @Override
//                public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
//                    return entry1.getValue().compareTo(entry2.getValue());
//                }
//            });
//
//            // ボタンを表示
//            for (final Map.Entry<String,Integer> map : entries) {
//                final String executeIdName = map.getKey();
//
//                try {
//                    // アクション名取得
//                    String actionName = executeIdName.replaceAll("^" + EXECUTE_ID_PREFIX, "");
//                    if (TextUtils.isEmpty(actionName))
//                        continue;
//
//                    Button executeButton = new Button(context, null, android.R.attr.buttonStyleSmall);
//
//                    // ラベル取得
//                    int labelId;
//                    if (appResource != null && (labelId = metaData.getInt(EXECUTE_LABEL_PREFIX + actionName)) > 0) {
//                        executeButton.setText(appResource.getString(labelId));
//                    }
//
//                    // アイコン取得
//                    int iconId;
//                    if (appResource != null && (iconId = metaData.getInt(EXECUTE_ICON_PREFIX + actionName)) > 0) {
//                        executeButton.setCompoundDrawablesWithIntrinsicBounds(appResource.getDrawable(iconId), null, null, null);
//                    }
//
//                    // 種別
//                    PluginTypeCategory tempType = null;
//                    String type;
//                    if (appResource != null && !TextUtils.isEmpty(type = metaData.getString(EXECUTE_CATEGORY_PREFIX + actionName))) {
//                        try {
//                            tempType = PluginTypeCategory.valueOf(type);
//                        } catch (Exception e) {
//                            Logger.e(e);
//                        }
//                    }
//                    final PluginTypeCategory pluginTypeCategory = tempType;
//
//                    DisplayMetrics metrics = getResources().getDisplayMetrics();
//                    executeButton.setTextSize(getResources().getDimension(R.dimen.dialog_plugin_button_font_size) / metrics.scaledDensity);
//                    executeButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//                    executeButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Intent intent = new Intent(PluginAction.ACTION_MEDIA.getActionValue());
//                            intent.setClassName(componentInfo.packageName, componentInfo.name);
//                            intent.addCategory(PluginOperationCategory.OPERATION_EXECUTE.getCategoryValue());
//                            if (pluginTypeCategory != null) {
//                                intent.addCategory(pluginTypeCategory.getCategoryValue());
//                            }
//                            intent.putExtra(executeIdName, map.getValue());
//                            resultIntent = intent;
//                            onClickButton(PluginDialogFragment.this.getDialog(), BUTTON_EXECUTE);
//                        }
//                    });
//
//                    executeLayout.addView(executeButton);
//                } catch (Exception e) {
//                    Logger.e(e);
//                }
//            }
//        }
//
//        if (executeLayout.getChildCount() <= 1) {
//            executeLayout.setVisibility(View.GONE);
//        }
//
//        // イベントアクション
//
//        // メディア開始
//        Button pluginEventMediaOpenButton = (Button)classView.findViewById(R.id.pluginEventMediaOpenButton);
//        if (info.filter != null && info.filter.hasCategory(PluginOperationCategory.OPERATION_MEDIA_OPEN.getCategoryValue())) {
//            pluginEventMediaOpenButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(PluginAction.ACTION_MEDIA.getActionValue());
//                    intent.setClassName(componentInfo.packageName, componentInfo.name);
//                    intent.addCategory(PluginOperationCategory.OPERATION_MEDIA_OPEN.getCategoryValue());
//                    resultIntent = intent;
//
//                    onClickButton(PluginDialogFragment.this.getDialog(), BUTTON_MEDIA_OPEN);
//                }
//            });
//            existsEvents = true;
//        } else {
//            pluginEventMediaOpenButton.setEnabled(false);
//        }
//        // メディア終了
//        Button pluginEventMediaCloseButton = (Button)classView.findViewById(R.id.pluginEventMediaCloseButton);
//        if (info.filter != null && info.filter.hasCategory(PluginOperationCategory.OPERATION_MEDIA_CLOSE.getCategoryValue())) {
//            pluginEventMediaCloseButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(PluginAction.ACTION_MEDIA.getActionValue());
//                    intent.setClassName(componentInfo.packageName, componentInfo.name);
//                    intent.addCategory(PluginOperationCategory.OPERATION_MEDIA_CLOSE.getCategoryValue());
//                    resultIntent = intent;
//
//                    onClickButton(PluginDialogFragment.this.getDialog(), BUTTON_MEDIA_CLOSE);
//                }
//            });
//            existsEvents = true;
//        } else {
//            pluginEventMediaCloseButton.setEnabled(false);
//        }
//        // 再生開始
//        Button pluginEventPlayStartButton = (Button)classView.findViewById(R.id.pluginEventPlayStartButton);
//        if (info.filter != null && info.filter.hasCategory(PluginOperationCategory.OPERATION_PLAY_START.getCategoryValue())) {
//            pluginEventPlayStartButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(PluginAction.ACTION_MEDIA.getActionValue());
//                    intent.setClassName(componentInfo.packageName, componentInfo.name);
//                    intent.addCategory(PluginOperationCategory.OPERATION_PLAY_START.getCategoryValue());
//                    resultIntent = intent;
//
//                    onClickButton(PluginDialogFragment.this.getDialog(), BUTTON_PLAY_START);
//                }
//            });
//            existsEvents = true;
//        } else {
//            pluginEventPlayStartButton.setEnabled(false);
//        }
//        // 再生停止
//        Button pluginEventPlayStopButton = (Button)classView.findViewById(R.id.pluginEventPlayStopButton);
//        if (info.filter != null && info.filter.hasCategory(PluginOperationCategory.OPERATION_PLAY_STOP.getCategoryValue())) {
//            pluginEventPlayStopButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(PluginAction.ACTION_MEDIA.getActionValue());
//                    intent.setClassName(componentInfo.packageName, componentInfo.name);
//                    intent.addCategory(PluginOperationCategory.OPERATION_PLAY_STOP.getCategoryValue());
//                    resultIntent = intent;
//
//                    onClickButton(PluginDialogFragment.this.getDialog(), BUTTON_PLAY_STOP);
//                }
//            });
//            existsEvents = true;
//        } else {
//            pluginEventPlayStopButton.setEnabled(false);
//        }
//        // 再生中
//        Button pluginEventPlayNowButton = (Button)classView.findViewById(R.id.pluginEventPlayNowButton);
//        if (info.filter != null && info.filter.hasCategory(PluginOperationCategory.OPERATION_PLAY_NOW.getCategoryValue())) {
//            pluginEventPlayNowButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(PluginAction.ACTION_MEDIA.getActionValue());
//                    intent.setClassName(componentInfo.packageName, componentInfo.name);
//                    intent.addCategory(PluginOperationCategory.OPERATION_PLAY_NOW.getCategoryValue());
//                    resultIntent = intent;
//
//                    onClickButton(PluginDialogFragment.this.getDialog(), BUTTON_PLAY_NOW);
//                }
//            });
//            existsEvents = true;
//        } else {
//            pluginEventPlayNowButton.setEnabled(false);
//        }
//        // 再生完了
//        Button pluginEventPlayCompleteButton = (Button)classView.findViewById(R.id.pluginEventPlayCompleteButton);
//        if (info.filter != null && info.filter.hasCategory(PluginOperationCategory.OPERATION_PLAY_COMPLETE.getCategoryValue())) {
//            pluginEventPlayNowButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(PluginAction.ACTION_MEDIA.getActionValue());
//                    intent.setClassName(componentInfo.packageName, componentInfo.name);
//                    intent.addCategory(PluginOperationCategory.OPERATION_PLAY_COMPLETE.getCategoryValue());
//                    resultIntent = intent;
//
//                    onClickButton(PluginDialogFragment.this.getDialog(), BUTTON_PLAY_COMPLETE);
//                }
//            });
//            existsEvents = true;
//        } else {
//            pluginEventPlayCompleteButton.setEnabled(false);
//        }
//
//
//        // イベントが存在しない
//        if (!existsEvents) {
//            classView.findViewById(R.id.pluginEventLayout).setVisibility(View.GONE);
//        }
//
//        classView.setTag(existsEvents);
//        return classView;
    }

}
