package com.wa2c.android.medoly.plugin.action.lyricsscraper;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        versionUp(this);
    }

    private void versionUp(Context context) {

    }
}
