package org.hydev.themeapplytools.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.hydev.themeapplytools.R;
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
        setContentView(R.layout.activity_get_direct_link);

        ThemeUtils.darkMode(this);

        // Get theme share link and get theme info to show.
        Button mb_getDirectLink = findViewById(R.id.mb_getDirectLink);
        TextInputLayout til_inputThemeLink = findViewById(R.id.til_inputThemeLink);
        mb_getDirectLink.setOnClickListener(v -> {
            String inputShareLink = til_inputThemeLink.getEditText().getText().toString();
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
                                .setMessage("文件大小：" + themeInfo.get("fileSize") + "\n" +
                                        "下载链接：\n" + downloadUrl + "\n" +
                                        "哈希值：" + finalFileHash)
                                .setNegativeButton("复制链接", (dialog, which) -> FileUtils.copyLink(GetDirectLinkActivity.this, downloadUrl))
                                .setPositiveButton("直接下载", (dialog, which) -> FileUtils.systemDownload(GetDirectLinkActivity.this, themeInfo))
                                .show());
                    }
                }
            });
        });

        MaterialCardView mcv_themeShareSite = findViewById(R.id.mcv_themeShareSite);
        mcv_themeShareSite.setOnClickListener(l -> {
            View view = getLayoutInflater().inflate(R.layout.dialog_theme_share, null);
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);

            materialAlertDialogBuilder.setView(view);
            ThemeShareDialogUtils.init(this, view);

            materialAlertDialogBuilder.setPositiveButton("OK", null);
            materialAlertDialogBuilder.show();
        });
    }
}
