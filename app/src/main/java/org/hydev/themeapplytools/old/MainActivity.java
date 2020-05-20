package org.hydev.themeapplytools.old;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.hydev.themeapplytools.R;
import org.hydev.themeapplytools.utils.FileUtils;
import org.hydev.themeapplytools.utils.ThemeShareDialogUtils;
import org.hydev.themeapplytools.utils.ThemeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

import eu.darken.rxshell.cmd.Cmd;
import eu.darken.rxshell.cmd.RxCmdShell;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    private static final String GITHUB_URL = "https://github.com/VergeDX/ThemeApplyTools";
    private static final String ME_COOLAPK_URL = "https://coolapk.com/u/506843";
    private static final String ICEBOX_COOLAPK_URL = "https://coolapk.com/apk/com.catchingnow.icebox";

    public static boolean applied = false;
    private static String filePath = null;

    /**
     * After apply theme, make a dialog.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (applied) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("完成")
                    .setMessage("主题已应用 \n" +
                            "若没有效果，可能是该主题不在根目录下 \n" +
                            "请仔细阅读说明 \n")
                    .setPositiveButton("OK", null)
                    .show();

            applied = false;
        }
    }

    /**
     * After user choose file, check the file and set filePath.
     *
     * @param requestCode is always 7.
     * @param data        contains user chosen file Uri.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 7 && resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();

            // try-with-source
            try (Cursor cursor = getContentResolver().query(fileUri, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    if (!fileName.endsWith(".mtz")) {
                        Snackbar.make(findViewById(R.id.rootView), R.string.not_mtz_file, Snackbar.LENGTH_LONG)
                                .show();
                        fileName = null;
                    } else {
                        Snackbar.make(findViewById(R.id.rootView), R.string.ensure_mtz, Snackbar.LENGTH_LONG)
                                .show();
                    }

                    if (fileName == null) {
                        filePath = null;
                    } else {
                        // MIUI theme manager needs absolute path.
                        filePath = Environment.getExternalStorageDirectory().getPath() + "/" + fileName;
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get system MIUI version.
        Cmd.Result result = Cmd.builder("getprop ro.miui.ui.version.name").execute(RxCmdShell.builder().build());
        String MIUIVersion = result.getOutput().get(0);
        if (MIUIVersion.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("警告")
                    .setCancelable(false)
                    .setMessage("你似乎不是 MIUI 系统 \n" +
                            "本 app 只在 MIUI 11 上测试过 \n" +
                            "你确定要继续使用吗？ \n")
                    .setNegativeButton("退出", (dialog, which) -> finish())
                    .setPositiveButton("继续", null)
                    .show();
        }

        // Open system file manager app.
        Button openFileManagerButton = findViewById(R.id.bt_openFileManager);
        openFileManagerButton.setOnClickListener(v -> {
            ApplicationInfo applicationInfo;

            try {
                // Check if file manager is exist.
                applicationInfo = getPackageManager().getApplicationInfo("com.android.fileexplorer", 0);
            } catch (PackageManager.NameNotFoundException e) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("错误")
                        .setMessage("没有找到 MIUI 文件管理器 \n" +
                                "您可能需要手动进行步骤 [1.] \n" +
                                "请确保你在使用 MIUI 系统 \n")
                        .setNegativeButton("返回", null)
                        .show();

                return;
            }

            // Check if file manager is enable.
            if (!applicationInfo.enabled) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("警告")
                        .setMessage("MIUI 文件管理器被冻结（禁用） \n" +
                                "您需要手动进行步骤 [1.] 以继续 \n")
                        .setNegativeButton("OK", null)
                        .show();

                return;
            }

            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName("com.android.fileexplorer", "com.android.fileexplorer.activity.FileActivity"));
            startActivity(intent);
        });

        // Choose a file and return to onActivityResult.
        Button chooseFileButton = findViewById(R.id.bt_chooseFile);
        chooseFileButton.setOnClickListener(v -> FileUtils.chooseFile(this));

        // Apply theme, should call after choose file.
        Button applyThemeButton = findViewById(R.id.bt_applyTheme);
        applyThemeButton.setOnClickListener(v -> {
            if (filePath == null) {
                Snackbar.make(v, R.string.no_Choose_File, Snackbar.LENGTH_LONG)
                        .show();
            } else {
                ThemeUtils.applyTheme(this, filePath);
                filePath = null;
            }
        });

        TextInputLayout themeShareLinkTextInputLayout = findViewById(R.id.til_themeShareLink);

        // Get theme share link and get theme info to show.
        Button getThemeDownloadLinkButton = findViewById(R.id.bt_getThemeDownloadLink);
        getThemeDownloadLinkButton.setOnClickListener(v -> {
            String inputShareLink = themeShareLinkTextInputLayout.getEditText().getText().toString();
            if (!inputShareLink.isEmpty()) {
                ThemeUtils.getThemeDownloadLinkAsync(this, inputShareLink, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        runOnUiThread(() -> new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("错误")
                                .setMessage("获取直链失败 \n" +
                                        "请检查网络连接后重试 \n")
                                .setNegativeButton("OK", null)
                                .show());
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) {
                        ResponseBody body = response.body();
                        Map<String, String> themeInfo = ThemeUtils.getThemeInfo(body);

                        // Cannot get theme info, maybe link is wrong.
                        if (themeInfo == null) {
                            runOnUiThread(() -> new MaterialAlertDialogBuilder(MainActivity.this)
                                    .setTitle("失败")
                                    .setMessage("获取主题信息失败 \n" +
                                            "可能是链接输入错误 \n")
                                    .setNegativeButton("OK", null)
                                    .show());
                        } else {
                            String downloadUrl = themeInfo.get("downloadUrl");
                            String fileName = themeInfo.get("fileName");

                            // Show theme info, set copy and download button.
                            runOnUiThread(() -> new MaterialAlertDialogBuilder(MainActivity.this)
                                    .setTitle(fileName)
                                    .setMessage("文件大小：" + themeInfo.get("fileSize") + "\n\n" +
                                            "下载链接：\n" + downloadUrl + "\n\n" +
                                            "哈希值：\n" + themeInfo.get("fileHash") + "\n")
                                    .setNegativeButton("复制链接", (dialog, which) -> FileUtils.copyLink(MainActivity.this, downloadUrl))
                                    .setPositiveButton("直接下载", (dialog, which) -> FileUtils.systemDownload(MainActivity.this, themeInfo))
                                    .show());
                        }
                    }
                });
            }
        });

        // Theme share website.
        Button themeShareButton = findViewById(R.id.bt_themeShare);
        themeShareButton.setOnClickListener(v -> {
            View view = getLayoutInflater().inflate(R.layout.dialog_theme_share, null);
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
            materialAlertDialogBuilder.setView(view);

            ThemeShareDialogUtils.init(this, view);

            materialAlertDialogBuilder.setTitle("第三方主题资源分享站");
            materialAlertDialogBuilder.setPositiveButton("OK", null);
            materialAlertDialogBuilder.show();
        });

        // Go to IceBox Coolapk download page.
        Button iceBoxCoolapkButton = findViewById(R.id.bt_iceboxCoolapk);
        iceBoxCoolapkButton.setOnClickListener(v -> ThemeShareDialogUtils.openBrowser(this, ICEBOX_COOLAPK_URL));

        // Default example path.
        TextInputLayout themeFilePathTextInputLayout = findViewById(R.id.til_path);
        themeFilePathTextInputLayout.getEditText().setText("/sdcard/test.mtz");

        // Get user input path and apply theme.
        Button advApplyThemeButton = findViewById(R.id.bt_advAppleTheme);
        advApplyThemeButton.setOnClickListener(v -> {
            String input = themeFilePathTextInputLayout.getEditText().getText().toString();

            if (input.equals("") || !input.endsWith(".mtz")) {
                Snackbar.make(v, R.string.input_isEmpty, Snackbar.LENGTH_LONG)
                        .show();
            } else {
                ThemeUtils.applyTheme(this, input);
            }
        });

        // Go to GitHub open source page.
        Button githubButton = findViewById(R.id.bt_github);
        githubButton.setOnClickListener(v -> ThemeShareDialogUtils.openBrowser(this, GITHUB_URL));

        // Show how this app works.
        Button howItWorkButton = findViewById(R.id.bt_howItWork);
        howItWorkButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("[原理] 应用主题")
                .setMessage("向 MIUI 主题商店发送 Intent 请求 \n" +
                        "在请求中指定特定的参数 \n" +
                        "\"theme_file_path\" -> 对应主题路径 \n" +
                        "\"api_called_from\" -> \"test\" \n" +
                        "之后发送请求即可 \n\n" +
                        "由于是测试接口 \n" +
                        "所以可能会恢复默认，请见谅 \n")
                .setPositiveButton("OK", (dialog, which) ->
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("[原理] 获取直链")
                                .setMessage("向 MIUI api 发送 get 请求 \n" +
                                        "在其中包含主题 Token \n" +
                                        "返回的 Json 中有下载链接 \n\n" +
                                        "例如： \n" +
                                        "http://zhuti.xiaomi.com/detail/f02025cb-8f0e-44e0-b39a-653e46d84d42 \n" +
                                        "的主题 Token 即为 \n" +
                                        "f02025cb-8f0e-44e0-b39a-653e46d84d42 \n")
                                .setPositiveButton("OK", null)
                                .show())
                .show());

        // Go to my page in Coolapk.
        Button meCoolapkButton = findViewById(R.id.bt_meCoolapk);
        meCoolapkButton.setOnClickListener(v -> ThemeShareDialogUtils.openBrowser(this, ME_COOLAPK_URL));
    }
}
