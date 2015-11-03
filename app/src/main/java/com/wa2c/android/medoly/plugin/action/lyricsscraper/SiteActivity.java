package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;


import com.wa2c.android.medoly.utils.Logger;

import org.maripo.android.widget.DeepRadioGroup;

import java.util.List;
import java.util.Map;

public class SiteActivity extends Activity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        // ラジオボタン
        DeepRadioGroup layout = (DeepRadioGroup)findViewById(R.id.siteParamGroupLayout);
        layout.setOnCheckedChangeListener(new DeepRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(DeepRadioGroup group, int checkedId) {
                try {
                    View v = findViewById(group.getCheckedRadioButtonId());
                    if (v != null) {
                        Integer id = (Integer) v.getTag();
                        if (id != null) {
                            preferences.edit().putInt(getString(R.string.prefkey_selected_id), id).apply();
                        }
                    }
                } catch (Exception e) {
                    Logger.e(e);
                }
            }
        });

        // 選択ID
        int selectedId = preferences.getInt(getString(R.string.prefkey_selected_id), 0);

        // ビュー初期化
        Map<Integer, LyricsObtainParam> map = LyricsObtainParam.getParamMap();
        for (LyricsObtainParam param : map.values()) {
            layout.addView(createParamView(param, (selectedId == param.Id)));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }



    /**
     * 取得パラメータビュー作成。
     * @param param パラメータ。
     * @return パラメータビュー。
     */
    private ViewGroup createParamView(final LyricsObtainParam param, boolean isSelected) {
        // レイアウト
        final ViewGroup siteLayout = (ViewGroup) View.inflate(this, R.layout.layout_site, null);
        final ViewGroup siteParamContentLayout = (ViewGroup) siteLayout.findViewById(R.id.siteParamContentLayout);
        final RadioButton siteParamRadioButton = (RadioButton) siteLayout.findViewById(R.id.siteParamRadioButton);

        // チェック
        siteParamRadioButton.setTag(param.Id);
        siteParamRadioButton.setChecked(isSelected);
        siteParamRadioButton.setId(View.generateViewId());
        siteParamRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    preferences.edit().putInt(getString(R.string.prefkey_selected_id), param.Id).apply();
                }
            }
        });

        List<Pair<Integer, String>> pairList = param.getList(this);
        for (final Pair<Integer, String> pair : pairList) {
            ViewGroup itemLayout = (ViewGroup) View.inflate(this, android.R.layout.simple_list_item_2, null);
            TextView titleText = (TextView)itemLayout.findViewById(android.R.id.text1);
            TextView valueText = (TextView)itemLayout.findViewById(android.R.id.text2);

            // 値
            titleText.setText(getString(pair.first));
            valueText.setText(pair.second);

            // クリック
            TypedValue outValue = new TypedValue();
            this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            itemLayout.setBackgroundResource(outValue.resourceId);

            if (pair.first == R.string.site_name) {
                itemLayout.setClickable(true);
                itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        siteParamRadioButton.setChecked(true);
                    }
                });
            } else if (pair.first == R.string.site_uri) {
                itemLayout.setClickable(true);
                itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(pair.second));
                            SiteActivity.this.startActivity(intent);
                        } catch (Exception e) {
                            Logger.e(e);
                        }
                    }
                });
            }

            siteParamContentLayout.addView(itemLayout);
        }

        return siteLayout;
    }

}
