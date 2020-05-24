package org.hydev.themeapplytools.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.hydev.themeapplytools.databinding.ActivityGetDirectLinkBinding;
import org.hydev.themeapplytools.databinding.DialogThemeShareBinding;
import org.hydev.themeapplytools.utils.FileUtils;
import org.hydev.themeapplytools.utils.ThemeShareDialogUtils;
import org.hydev.themeapplytools.utils.ThemeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GetDirectLinkActivity extends AppCompatActivity {
    private static final String EXAMPLE_THEME_LINK = "http://zhuti.xiaomi.com/detail/d555981b-e6af-4ea9-9eb2-e47cfbc3edfa";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityGetDirectLinkBinding activityGetDirectLinkBinding = ActivityGetDirectLinkBinding.inflate(getLayoutInflater());
        setContentView(activityGetDirectLinkBinding.getRoot());

        ThemeUtils.darkMode(this);

        // Get theme share link and get theme info to show.
        activityGetDirectLinkBinding.mbGetDirectLink.setOnClickListener(v -> {
            String inputShareLink = activityGetDirectLinkBinding.tilInputThemeLink.getEditText().getText().toString();
            String[] themeLinkSplit = inputShareLink.split("/detail/");
            if (themeLinkSplit.length != 2) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("错误")
                        .setMessage("请输入主题的分享链接，例如：\n" + EXAMPLE_THEME_LINK)
                        .setNegativeButton("返回", null)
                        .setPositiveButton("复制", (dialog, which) -> {
                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("ExampleUrl", EXAMPLE_THEME_LINK);
                            clipboardManager.setPrimaryClip(clipData);

                            Toast.makeText(this, "已复制示例链接", Toast.LENGTH_SHORT).show();
                        })
                        .show();

                return;
            }

            ThemeUtils.getThemeDownloadLinkAsync(this, inputShareLink, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    runOnUiThread(() -> new MaterialAlertDialogBuilder(GetDirectLinkActivity.this)
                            .setTitle("错误")
                            .setMessage("获取直链失败, \n" +
                                    "请检查网络连接后重试. ")
                            .setNegativeButton("OK", null)
                            .show());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    ResponseBody body = response.body();
                    Map<String, String> themeInfo = ThemeUtils.getThemeInfo(body);

                    // Cannot get theme info, maybe link is wrong.
                    if (themeInfo == null) {
                        runOnUiThread(() -> new MaterialAlertDialogBuilder(GetDirectLinkActivity.this)
                                .setTitle("失败")
                                .setMessage("获取主题信息失败 \n" +
                                        "可能是链接输入错误 ")
                                .setNegativeButton("OK", null)
                                .show());
                    } else {
                        String downloadUrl = themeInfo.get("downloadUrl");
                        String fileName = themeInfo.get("fileName");
                        String fileHash = "\n" + themeInfo.get("fileHash");

                        // File hash may not exist.
                        if (fileHash.equals("\n")) {
                            fileHash = "暂无";
                        }

                        // Show theme info, set copy and download button.
                        String finalFileHash = fileHash;
                        runOnUiThread(() -> new MaterialAlertDialogBuilder(GetDirectLinkActivity.this)
                                .setTitle(fileName)
                                .setMessage("文件大小：" + themeInfo.get("fileSize") + "\n\n" +
                                        "下载链接：\n" + downloadUrl + "\n\n" +
                                        "哈希值：" + finalFileHash + "\n")
                                .setNegativeButton("复制链接", (dialog, which) -> FileUtils.copyLink(GetDirectLinkActivity.this, downloadUrl))
                                .setPositiveButton("直接下载", (dialog, which) -> FileUtils.systemDownload(GetDirectLinkActivity.this, themeInfo))
                                .show());
                    }
                }
            });
        });

        activityGetDirectLinkBinding.mcvThemeShareSite.setOnClickListener(l -> {
            DialogThemeShareBinding dialogThemeShareBinding = DialogThemeShareBinding.inflate(getLayoutInflater());
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);

            materialAlertDialogBuilder.setView(dialogThemeShareBinding.getRoot())
                    .setPositiveButton("OK", null);

            ThemeShareDialogUtils.setOnClickListener(this, dialogThemeShareBinding);
            materialAlertDialogBuilder.show();
        });
    }
}
