package org.hydev.themeapplytools.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.hydev.themeapplytools.databinding.DialogThemeShareBinding

@Suppress("SpellCheckingInspection")
object ThemeShareDialogUtils {
    private const val THEME_OFFICIAL_URL = "http://zhuti.xiaomi.com/"
    private const val THEMES_TK_URL = "https://miuithemes.tk/"
    private const val TECH_RUCHI_URL = "http://www.techrushi.com/"
    private const val THEME_XIAOMIS_URL = "https://miuithemesxiaomis.blogspot.com/"
    private const val XIAOMI_THEMEZ_URL = "https://www.miuithemez.com/"

    /**
     * Register click event of theme
     *
     * @param context need to open browser.
     * @param dialogThemeShareBinding to find button.
     * @see DialogThemeShareBinding
     */
    fun setOnClickListener(context: Context, dialogThemeShareBinding: DialogThemeShareBinding) {
        dialogThemeShareBinding.btOfficialStore.setOnClickListener { openBrowser(context, THEME_OFFICIAL_URL) }
        dialogThemeShareBinding.btThemesTK.setOnClickListener { openBrowser(context, THEMES_TK_URL) }
        dialogThemeShareBinding.btTechruchi.setOnClickListener { openBrowser(context, TECH_RUCHI_URL) }
        dialogThemeShareBinding.btThemeXiaomis.setOnClickListener { openBrowser(context, THEME_XIAOMIS_URL) }
        dialogThemeShareBinding.btThemez.setOnClickListener { openBrowser(context, XIAOMI_THEMEZ_URL) }
    }

    /**
     * Open browser of given link.
     *
     * @param context need to start activity.
     * @param link to browse
     */
    fun openBrowser(context: Context, link: String?) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        context.startActivity(intent)
    }
}
