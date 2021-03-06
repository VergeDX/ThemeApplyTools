package org.hydev.themeapplytools.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Proxy
import java.net.URLDecoder
import java.util.*

object ThemeUtils {
    private const val THEME_API_URL = "https://thm.market.xiaomi.com/thm/download/v2/"

    /**
     * After apply theme, there is operate result.
     */
    enum class ApplyThemeResult {
        // MIUI theme manager is not found.
        NO_THEME_MANAGER,

        // MIUI theme manager is disabled.
        THEME_MANAGER_DISABLED,

        // Sent intent to MIUI theme manager.
        SENT_INTENT
    }

    /**
     * Apply a theme by send intent to system theme manager with theme file path.
     *
     * @param activity need to start activity.
     * @param filePath mtz theme file's absolute path.
     * @return -1 if MIUI theme manager not exist.
     *         -2 if MIUI theme manager not enabled.
     *          0 if posted apply theme intent.
     */
    fun applyTheme(activity: Activity, filePath: String?): ApplyThemeResult {
        // The information of MIUI theme manager. If not exist, return code -1.
        val themeManagerAppInfo: ApplicationInfo = try {
            @Suppress("SpellCheckingInspection")
            activity.packageManager.getApplicationInfo("com.android.thememanager", 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return ApplyThemeResult.NO_THEME_MANAGER
        }

        // If MIUI theme manager disabled, return code -2.
        if (!themeManagerAppInfo.enabled) {
            return ApplyThemeResult.THEME_MANAGER_DISABLED
        }

        // Else, make intent and bundle to apply the theme.
        val intent = Intent().apply {
            action = Intent.ACTION_MAIN
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

            @Suppress("SpellCheckingInspection")
            component = ComponentName("com.android.thememanager", "com.android.thememanager.ApplyThemeForScreenshot")
        }
        val bundle = Bundle().apply {
            putString("theme_file_path", filePath)
            putString("api_called_from", "test")
        }

        intent.putExtras(bundle)
        activity.startActivity(intent)

        return ApplyThemeResult.SENT_INTENT
    }

    /**
     * Make an async get call to get theme info,
     * if theme share link does not match,
     * it will be show a dialog and return.
     *
     * @param themeToken MIUI theme token.
     * @param miuiVersion only can be V10, V11, V12
     * @param callback       operation when after get HTTP request.
     */
    fun getThemeDownloadLinkAsync(themeToken: String, miuiVersion: String, proxy: Proxy? = null, callback: Callback) {
        val okHttpClient = if (proxy == null) OkHttpClient()
        else OkHttpClient.Builder().proxy(proxy).build()

        val request = Request.Builder().url("$THEME_API_URL$themeToken?miuiUIVersion=$miuiVersion").build()
        okHttpClient.newCall(request).enqueue(callback)
    }

    /**
     * MIUI theme api json object from the official server.
     * https://thm.market.xiaomi.com/thm/download/v2/d555981b-e6af-4ea9-9eb2-e47cfbc3edfa?miuiUIVersion=V11
     *
     * @property apiCode from the official server, 0 is ok, and -1 is error.
     * @property apiData contains theme download url, file hash and file size.
     */
    @Serializable
    data class MiuiTheme(
            val apiCode: Int,
            val apiData: MiuiThemeData
    ) {
        /**
         * contains theme download url, file hash (maybe empty) and file size.
         *
         * @property downloadUrl theme direct download url, should be decoded.
         * @property fileHash theme file hash, are lower case from the official server.
         * @property fileSize theme file size, unit is B.
         */
        @Serializable
        data class MiuiThemeData(
                private val downloadUrl: String,
                private val fileHash: String,
                private val fileSize: Int
        ) {
            // Decoded theme download url.
            val themeDownloadUrl: String
                get() = URLDecoder.decode(downloadUrl, "UTF-8")

            // Upper case theme hash, or "暂无" if missing this field.
            val themeFileHash: String
                get() = if (fileHash.isEmpty()) "暂无" else fileHash.toUpperCase(Locale.ROOT)

            // Unit is MB.
            val themeFileSize: Double
                get() = fileSize / 10e5

            // Decoded file name from theme download url.
            val themeFileName: String
                get() = URLDecoder.decode(downloadUrl.split("/").last(), "UTF-8")
        }
    }

    /**
     * Parse theme object from json.
     * https://thm.market.xiaomi.com/thm/download/v2/d555981b-e6af-4ea9-9eb2-e47cfbc3edfa?miuiUIVersion=V11
     *
     * @param responseString json from MIUI official server.
     * @return MiuiThemeData, if apiCode is -1, return null.
     * @see MiuiTheme.apiCode
     */
    fun parseThemeInfo(responseString: String): Optional<MiuiTheme.MiuiThemeData> {
        return try {
            Optional.of(Json { allowStructuredMapKeys = true }
                    .decodeFromString(MiuiTheme.serializer(), responseString).apiData)
        } catch (e: Exception) {
            Optional.empty<MiuiTheme.MiuiThemeData>()
        }
    }

    /**
     * Set status bar color if not dark mode.
     * https://developer.android.com/guide/topics/ui/look-and-feel/darktheme#%E9%85%8D%E7%BD%AE%E5%8F%98%E6%9B%B4
     *
     * @param activity to get the current configuration.
     */
    fun darkMode(activity: Activity) {
        val configuration = activity.resources.configuration
        val currentNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        // If not in the dark mode, set status bar color to black.
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            // https://stackoverflow.com/questions/62577645/android-view-view-systemuivisibility-deprecated-what-is-the-replacement
            // activity.window.setDecorFitsSystemWindows(false)
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    /**
     * Get miui theme token from theme share link or text, the following text are also acceptable:
     *
     * "《Pure 轻雨》这个主题不错，推荐一下。下载地址: http://zhuti.xiaomi.com/detail/d555981b-e6af-4ea9-9eb2-e47cfbc3edfa ，来自 @MIUI"
     * "《Mine》这个主题不错，强烈推荐～下载地址: http://zhuti.xiaomi.com/detail/share/b797e0cf-e775-4824-a21c-44fa2728db84?miref=share&packId=def53eb6-daea-495e-8061-dbda0826ede8 ，来自@小米主题"
     *
     * @param themeShareText
     * @return theme token, if parse error, return empty string.
     */
    fun parseThemeToken(themeShareText: String): String {
        // Using themeLinkSplit[1] below, so default list length should be 2.
        var themeLinkSplit = listOf("", "")

        // 2020.10.2 update: theme share link now like this:
        // http://zhuti.xiaomi.com/detail/share/b797e0cf-e775-4824-a21c-44fa2728db84?miref=share&packId=def53eb6-daea-495e-8061-dbda0826ede8
        // def53eb6-daea-495e-8061-dbda0826ede8 is real theme token, length is also 36.
        if ("&packId=" in themeShareText) {
            // if contains, means new share link.
            themeLinkSplit = themeShareText.split("&packId=")
        } else if ("/detail/" in themeShareText) {
            // Old theme share link method, share link like this:
            // http://zhuti.xiaomi.com/detail/d555981b-e6af-4ea9-9eb2-e47cfbc3edfa
            // d555981b-e6af-4ea9-9eb2-e47cfbc3edfa is the theme token.
            themeLinkSplit = themeShareText.split("/detail/")
        }

        return if (themeLinkSplit[1].length < 36) ""
        else themeLinkSplit[1].substring(0, 36)
    }
}
