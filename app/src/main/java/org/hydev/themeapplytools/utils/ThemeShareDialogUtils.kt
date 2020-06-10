package org.hydev.themeapplytools.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import org.hydev.themeapplytools.databinding.DialogThemeShareBinding

object ThemeShareDialogUtils {
    private const val THEME_OFFICIAL_URL = "http://zhuti.xiaomi.com/"
    private const val THEMES_TK_URL = "https://miuithemes.tk/"
    private const val TECH_RUCHI_URL = "http://www.techrushi.com/"
    private const val THEME_XIAOMIS_URL = "https://miuithemesxiaomis.blogspot.com/"
    private const val XIAOMI_THEMEZ_URL = "https://www.miuithemez.com/"

    fun setOnClickListener(activity: Activity, dialogThemeShareBinding: DialogThemeShareBinding) {
        dialogThemeShareBinding.btOfficialStore.setOnClickListener { openBrowser(activity, THEME_OFFICIAL_URL) }
        dialogThemeShareBinding.btThemesTK.setOnClickListener { openBrowser(activity, THEMES_TK_URL) }
        dialogThemeShareBinding.btTechruchi.setOnClickListener { openBrowser(activity, TECH_RUCHI_URL) }
        dialogThemeShareBinding.btThemeXiaomis.setOnClickListener { openBrowser(activity, THEME_XIAOMIS_URL) }
        dialogThemeShareBinding.btThemez.setOnClickListener { openBrowser(activity, XIAOMI_THEMEZ_URL) }
    }

    fun openBrowser(activity: Activity, URL: String?) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL))
        activity.startActivity(intent)
    }
}
