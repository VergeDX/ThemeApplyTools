package org.hydev.themeapplytools.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration.Companion.Stable
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.hydev.themeapplytools.utils.FileUtils.alert
import org.hydev.themeapplytools.utils.FileUtils.alertInfo
import java.net.URLDecoder
import java.util.*

object ThemeUtils {
    private const val THEME_API_URL = "https://thm.market.xiaomi.com/thm/download/v2/"

    /**
     * Apply a theme by send intent to system theme manager with theme file path,
     * and also set applied flag to true.
     *
     * @param filePath mtz theme file absolute path.
     * @return true if successful.
     */
    fun applyTheme(activity: Activity, filePath: String?): Boolean {
        // If theme manager exists.
        val applicationInfo: ApplicationInfo = try {
            activity.packageManager.getApplicationInfo("com.android.thememanager", 0)
        }
        catch (e: PackageManager.NameNotFoundException) {
            alertInfo(activity, "错误", "没有找到 MIUI 主题商店\n您或许卸载了 MIUI 主题商店")
            return false
        }

        // If theme manager is not enabled.
        if (!applicationInfo.enabled) {
            alert(activity, "警告", "MIUI 主题商店被禁用\n请手动启用 MIUI 主题商店")
                .negative("返回") {}
                .positive("启用") {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:com.android.thememanager")
                    activity.startActivity(intent)
                    Toast.makeText(activity, "请点击下方的 “启用”", Toast.LENGTH_LONG).show()
                }.show(activity)
            return false
        }

        val intent = Intent("android.intent.action.MAIN")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.component = ComponentName("com.android.thememanager", "com.android.thememanager.ApplyThemeForScreenshot")

        val bundle = Bundle()
        bundle.putString("theme_file_path", filePath)
        bundle.putString("api_called_from", "test")
        intent.putExtras(bundle)
        activity.startActivity(intent)
        return true
    }

    /**
     * Make a async get call to get theme info,
     * if theme share link does not match,
     * it will be show a dialog and return.
     *
     * @param themeShareLink MIUI theme share link.
     * @param callback       operation when after get HTTP request.
     */
    fun getThemeDownloadLinkAsync(themeShareLink: String, callback: Callback) {
        val themeToken = themeShareLink.split("/detail/".toRegex())[1].substring(0, 36)
        val request = Request.Builder().url("$THEME_API_URL$themeToken?miuiUIVersion=V11").build()
        OkHttpClient().newCall(request).enqueue(callback)
    }

    /**
     * Data classes for MIUI theme api
     */
    @Serializable
    data class MiuiTheme(val apiCode: Int, val apiData: MiuiThemeData)

    /**
     * Actual data class
     */
    @Serializable
    data class MiuiThemeData(private var downloadUrl: String, private var fileHash: String, private var fileSize: Int) {
        fun getDownloadUrl(): String = URLDecoder.decode(downloadUrl, "UTF-8")
        fun getFileHash() = (if (fileHash.isEmpty()) "暂无" else fileHash).toUpperCase(Locale.ROOT)
        fun getFileSize() = String.format(Locale.CHINESE, "%.2f", fileSize / 10e5) + " MB"
        fun getFileName() = getDownloadUrl().split("/").last()
    }

    /**
     * Parse MIUI theme API response, generate a theme info Set.
     * example JSON can get here:
     * https://thm.market.xiaomi.com/thm/download/v2/d555981b-e6af-4ea9-9eb2-e47cfbc3edfa?miuiUIVersion=V11
     *
     * @param responseBody HTTP response result.
     * @return theme info Set(downloadUrl, fileHash, fileSize, fileName).
     */
    fun getThemeInfo(responseBody: ResponseBody?): MiuiThemeData? {
        return try {
            Json(Stable).parse(MiuiTheme.serializer(), responseBody!!.string()).apiData
        }
        catch (e: Exception) {
            null
        }
    }

    /**
     * Set status bar color if not dark mode.
     * https://developer.android.com/guide/topics/ui/look-and-feel/darktheme#%E9%85%8D%E7%BD%AE%E5%8F%98%E6%9B%B4
     *
     * @param activity to get current configuration.
     */
    fun darkMode(activity: Activity) {
        val configuration = activity.resources.configuration
        val currentNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        // If not in dark mode, set status bar color to black.
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}
