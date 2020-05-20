package org.hydev.themeapplytools.activity;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.hydev.themeapplytools.R;
import org.hydev.themeapplytools.utils.FileUtils;
import org.hydev.themeapplytools.utils.ThemeUtils;

public class ApplyThemeActivity extends AppCompatActivity {
    private static String filePath = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_theme);

        ThemeUtils.darkMode(this);

        MaterialButton mb_openFileManager = findViewById(R.id.mb_openFileManager);
        mb_openFileManager.setOnClickListener(v -> {
            ApplicationInfo applicationInfo;

            try {
                // File manager is exist.
                applicationInfo = getPackageManager().getApplicationInfo("com.android.fileexplorer", 0);
            } catch (PackageManager.NameNotFoundException e) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("错误")
                        .setMessage("没有找到 MIUI 文件管理器 \n" +
                                "您可能需要手动进行步骤 (1.) \n" +
                                "同时，请确保你在使用 MIUI 系统")
                        .setNegativeButton("返回", null)
                        .show();

                return;
            }

            // File manager is enable.
            if (!applicationInfo.enabled) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("警告")
                        .setMessage("MIUI 文件管理器被冻结（禁用） \n" +
                                "您需要手动进行步骤 (1.) 以继续")
                        .setNegativeButton("OK", null)
                        .show();

                return;
            }

            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName("com.android.fileexplorer", "com.android.fileexplorer.activity.FileActivity"));
            startActivity(intent);
        });

        MaterialButton mb_chooseFile = findViewById(R.id.mb_chooseFile);
        mb_chooseFile.setOnClickListener(l -> FileUtils.chooseFile(this));

        MaterialButton mb_applyTheme = findViewById(R.id.mb_applyTheme);
        mb_applyTheme.setOnClickListener(l -> {
            if (filePath == null) {
                Snackbar.make(l, R.string.no_Choose_File, Snackbar.LENGTH_LONG)
                        .show();
            } else {
                ThemeUtils.applyTheme(this, filePath);
                filePath = null;
            }
        });
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
                        Snackbar.make(findViewById(R.id.ll_applyTheme), R.string.not_mtz_file, Snackbar.LENGTH_LONG)
                                .show();
                        fileName = null;
                    } else {
                        Snackbar.make(findViewById(R.id.ll_applyTheme), R.string.ensure_mtz, Snackbar.LENGTH_LONG)
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
}
