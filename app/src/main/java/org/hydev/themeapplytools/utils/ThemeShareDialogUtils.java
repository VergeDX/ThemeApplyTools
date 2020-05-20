package org.hydev.themeapplytools.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import org.hydev.themeapplytools.R;

public class ThemeShareDialogUtils {
    private static final String THEME_OFFICIAL_URL = "http://zhuti.xiaomi.com/";
    private static final String THEMES_TK_URL = "https://miuithemes.tk/";
    private static final String TECH_RUCHI_URL = "http://www.techrushi.com/";
    private static final String THEME_XIAOMIS_URL = "https://miuithemesxiaomis.blogspot.com/";
    private static final String XIAOMI_THEMEZ_URL = "https://www.miuithemez.com/";

    public static void init(Activity activity, View view) {
        Button officialStoreButton = view.findViewById(R.id.bt_officialStore);
        Button themesTKButton = view.findViewById(R.id.bt_themesTK);
        Button techruchiButton = view.findViewById(R.id.bt_techruchi);
        Button themeXiaomisButton = view.findViewById(R.id.bt_themeXiaomis);
        Button themezButton = view.findViewById(R.id.bt_themez);

        officialStoreButton.setOnClickListener(v -> openBrowser(activity, THEME_OFFICIAL_URL));
        themesTKButton.setOnClickListener(v -> openBrowser(activity, THEMES_TK_URL));
        techruchiButton.setOnClickListener(v -> openBrowser(activity, TECH_RUCHI_URL));
        themeXiaomisButton.setOnClickListener(v -> openBrowser(activity, THEME_XIAOMIS_URL));
        themezButton.setOnClickListener(v -> openBrowser(activity, XIAOMI_THEMEZ_URL));
    }

    public static void openBrowser(Activity activity, final String URL) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
        activity.startActivity(intent);
    }
}
