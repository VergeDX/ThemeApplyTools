package org.hydev.themeapplytools.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class ThemeUtils {
    private static final String THEME_API_URL = "https://thm.market.xiaomi.com/thm/download/v2/";

    /**
     * Apply a theme by send intent to system theme manager with theme file path,
     * and also set applied flag to true.
     *
     * @param filePath mtz theme file absolute path.
     * @return true if successful.
     */
    public static boolean applyTheme(Activity activity, String filePath) {
        ApplicationInfo applicationInfo;

        try {
            // If theme manager not exist.
            applicationInfo = activity.getPackageManager().getApplicationInfo("com.android.thememanager", 0);
        } catch (PackageManager.NameNotFoundException e) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle("错误 ")
                    .setMessage("没有找到 MIUI 主题商店 \n" +
                            "您或许卸载了 MIUI 主题商店 ")
                    .setNegativeButton("OK", null)
                    .show();

            return false;
        }

        // If theme manager not enable.
        if (!applicationInfo.enabled) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle("警告")
                    .setMessage("MIUI 主题商店被禁用 \n" +
                            "请手动启用 MIUI 主题商店 ")
                    .setNegativeButton("返回", null)
                    .setPositiveButton("启用", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:com.android.thememanager"));
                        activity.startActivity(intent);

                        Toast.makeText(activity, "请点击下方的 “启用”", Toast.LENGTH_LONG).show();
                    })
                    .show();

            return false;
        }

        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName("com.android.thememanager", "com.android.thememanager.ApplyThemeForScreenshot"));

        Bundle bundle = new Bundle();
        bundle.putString("theme_file_path", filePath);
        bundle.putString("api_called_from", "test");

        intent.putExtras(bundle);
        activity.startActivity(intent);

        return true;
    }

    /**
     * Make a async get call to get theme info,
     * if theme share link does not match,
     * it will be show a dialog and return.
     *
     * @param themeShareLink MIUI theme share link.
     * @param callback       operation when after get HTTP request.
     */
    public static void getThemeDownloadLinkAsync(Activity activity, String themeShareLink, Callback callback) {
        String[] themeLinkSplit = themeShareLink.split("/detail/");
        String themeToken = themeLinkSplit[1].substring(0, 36);

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(THEME_API_URL + themeToken + "?miuiUIVersion=V11").build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    /**
     * Parse MIUI theme API response, generate a theme info Set.
     * example JSON can get here: https://thm.market.xiaomi.com/thm/download/v2/d555981b-e6af-4ea9-9eb2-e47cfbc3edfa?miuiUIVersion=V11
     *
     * @param responseBody HTTP response result.
     * @return theme info Set(downloadUrl, fileHash, fileSize, fileName).
     */
    public static Map<String, String> getThemeInfo(ResponseBody responseBody) {
        try {
            JsonObject jsonObject = new Gson().fromJson(responseBody.string(), JsonObject.class);
            int apiCode = jsonObject.get("apiCode").getAsInt();

            // 0 is OK, -1 is error.
            if (apiCode == 0) {
                JsonObject apiDataJsonObject = jsonObject.getAsJsonObject("apiData");

                String rawDownloadUrl = apiDataJsonObject.get("downloadUrl").getAsString();
                String downloadUrl = URLDecoder.decode(rawDownloadUrl, "UTF-8");
                String fileHash = apiDataJsonObject.get("fileHash").getAsString().toUpperCase();
                String fileSize = String.format(Locale.CHINESE, "%.2f", apiDataJsonObject.get("fileSize").getAsInt() / 10e5) + " MB";

                String[] downloadUrlSpilt = rawDownloadUrl.split("/");
                String fileName = URLDecoder.decode(downloadUrlSpilt[downloadUrlSpilt.length - 1], "UTF-8");

                Map<String, String> themeInfo = new HashMap<>();
                themeInfo.put("downloadUrl", downloadUrl);
                themeInfo.put("fileHash", fileHash);
                themeInfo.put("fileSize", fileSize);
                themeInfo.put("fileName", fileName);

                return themeInfo;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
    }

    /**
     * Set status bar color if not dark mode.
     * https://developer.android.com/guide/topics/ui/look-and-feel/darktheme#%E9%85%8D%E7%BD%AE%E5%8F%98%E6%9B%B4
     *
     * @param activity to get current configuration.
     */
    public static void darkMode(Activity activity) {
        Configuration configuration = activity.getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;

        // If not in dark mode, set status bar color to black.
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}
