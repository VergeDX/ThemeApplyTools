package org.hydev.themeapplytools.activity;

import android.os.Bundle;

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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityGetDirectLinkBinding activityGetDirectLinkBinding = ActivityGetDirectLinkBinding.inflate(getLayoutInflater());
        setContentView(activityGetDirectLinkBinding.getRoot());

        ThemeUtils.darkMode(this);

        // Get theme share link and get theme info to show.
        activityGetDirectLinkBinding.mbGetDirectLink.setOnClickListener(v -> {
            String inputShareLink = activityGetDirectLinkBinding.tilInputThemeLink.getEditText().getText().toString();
            ThemeUtils.getThemeDownloadLinkAsync(this, inputShareLink, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    runOnUiThread(() -> new MaterialAlertDialogBuilder(GetDirectLinkActivity.this)
                            .setTitle("错误")
                            .setMessage("获取直链失败 \n" +
                                    "请检查网络连接后重试 ")
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

            materialAlertDialogBuilder.setView(dialogThemeShareBinding.getRoot());
            ThemeShareDialogUtils.init(this, dialogThemeShareBinding);

            materialAlertDialogBuilder.setPositiveButton("OK", null);
            materialAlertDialogBuilder.show();
        });
    }
}
