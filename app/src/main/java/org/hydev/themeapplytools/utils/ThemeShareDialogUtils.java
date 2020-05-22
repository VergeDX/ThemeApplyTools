package org.hydev.themeapplytools.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import org.hydev.themeapplytools.databinding.DialogThemeShareBinding;

public class ThemeShareDialogUtils {
    private static final String THEME_OFFICIAL_URL = "http://zhuti.xiaomi.com/";
    private static final String THEMES_TK_URL = "https://miuithemes.tk/";
    private static final String TECH_RUCHI_URL = "http://www.techrushi.com/";
    private static final String THEME_XIAOMIS_URL = "https://miuithemesxiaomis.blogspot.com/";
    private static final String XIAOMI_THEMEZ_URL = "https://www.miuithemez.com/";

    public static void init(Activity activity, DialogThemeShareBinding dialogThemeShareBinding) {
        dialogThemeShareBinding.btOfficialStore.setOnClickListener(v -> openBrowser(activity, THEME_OFFICIAL_URL));
        dialogThemeShareBinding.btThemesTK.setOnClickListener(v -> openBrowser(activity, THEMES_TK_URL));
        dialogThemeShareBinding.btTechruchi.setOnClickListener(v -> openBrowser(activity, TECH_RUCHI_URL));
        dialogThemeShareBinding.btThemeXiaomis.setOnClickListener(v -> openBrowser(activity, THEME_XIAOMIS_URL));
        dialogThemeShareBinding.btThemez.setOnClickListener(v -> openBrowser(activity, XIAOMI_THEMEZ_URL));
    }

    public static void openBrowser(Activity activity, final String URL) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
        activity.startActivity(intent);
    }
}
