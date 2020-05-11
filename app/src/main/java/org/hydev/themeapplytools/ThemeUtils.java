package org.hydev.themeapplytools;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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

class ThemeUtils {
    private static final String THEME_API_URL = "https://thm.market.xiaomi.com/thm/download/v2/";

    /**
     * Apply a theme by send intent to system theme manager with theme file path,
     * and also set applied flag to true.
     *
     * @param filePath mtz theme file absolute path.
     */
    static void applyTheme(Activity activity, String filePath) {
        ApplicationInfo applicationInfo;

        try {
            // If theme manager not exist.
            applicationInfo = activity.getPackageManager().getApplicationInfo("com.android.thememanager", 0);
        } catch (PackageManager.NameNotFoundException e) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle("错误")
                    .setMessage("没有找到 MIUI 主题商店 \n" +
                            "您可能不是 MIUI 系统 \n" +
                            "或 MIUI 主题商店被卸载 \n" +
                            "非 MIUI 系统无法使用本 app \n")
                    .setNegativeButton("退出", (dialog, which) -> activity.finish())
                    .show();

            return;
        }

        // If theme manager not enable.
        if (!applicationInfo.enabled) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle("警告")
                    .setMessage("MIUI 主题商店被禁用 \n" +
                            "请启用 MIUI 主题商店 \n" +
                            "以便继续应用主题 \n")
                    .setNegativeButton("返回", null)
                    .setPositiveButton("启用", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:com.android.thememanager"));
                        activity.startActivity(intent);

                        Toast.makeText(activity, "请点击下方的 “启用”", Toast.LENGTH_LONG).show();
                    })
                    .show();

            return;
        }

        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName("com.android.thememanager", "com.android.thememanager.ApplyThemeForScreenshot"));

        Bundle bundle = new Bundle();
        bundle.putString("theme_file_path", filePath);
        bundle.putString("api_called_from", "test");

        intent.putExtras(bundle);
        activity.startActivity(intent);

        MainActivity.applied = true;
    }

    /**
     * Make a async get call to get theme info,
     * if theme share link does not match,
     * it will be show a dialog and return.
     *
     * @param themeShareLink MIUI theme share link.
     * @param callback       operation when after get HTTP request.
     */
    static void getThemeDownloadLinkAsync(Activity activity, String themeShareLink, Callback callback) {
        String[] themeLinkSplit = themeShareLink.split("/detail/");
        if (themeLinkSplit.length != 2) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle("错误")
                    .setMessage("请输入主题的分享链接，例如：\n" +
                            "http://zhuti.xiaomi.com/detail/f02025cb-8f0e-44e0-b39a-653e46d84d42 \n")
                    .setNegativeButton("OK", null)
                    .show();

            return;
        }

        String themeToken = themeLinkSplit[1].substring(0, 36);

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(THEME_API_URL + themeToken + "?miuiUIVersion=V11").build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    /**
     * Parse MIUI theme API response, generate a theme info Set.
     *
     * @param responseBody HTTP response result.
     * @return theme info Set(downloadUrl, fileHash, fileSize, fileName).
     */
    static Map<String, String> getThemeInfo(ResponseBody responseBody) {
        try {
            JsonObject jsonObject = new Gson().fromJson(responseBody.string(), JsonObject.class);
            int apiCode = jsonObject.get("apiCode").getAsInt();

            // 0 is OK, -1 is error.
            if (apiCode == 0) {
                JsonObject apiDataJsonObject = jsonObject.getAsJsonObject("apiData");

                String downloadUrl = URLDecoder.decode(apiDataJsonObject.get("downloadUrl").getAsString(), "UTF-8");
                String fileHash = apiDataJsonObject.get("fileHash").getAsString().toUpperCase();
                String fileSize = String.format(Locale.CHINESE, "%.2f", apiDataJsonObject.get("fileSize").getAsInt() / 10e5) + " MB";

                String[] downloadUrlSpilt = downloadUrl.split("/");
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
}
