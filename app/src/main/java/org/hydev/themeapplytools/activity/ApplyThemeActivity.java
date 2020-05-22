package org.hydev.themeapplytools.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.hydev.themeapplytools.databinding.ActivityApplyThemeBinding;
import org.hydev.themeapplytools.utils.FileUtils;
import org.hydev.themeapplytools.utils.ThemeUtils;

public class ApplyThemeActivity extends AppCompatActivity {
    private static String filePath = null;
    private ActivityApplyThemeBinding activityApplyThemeBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityApplyThemeBinding = ActivityApplyThemeBinding.inflate(getLayoutInflater());
        setContentView(activityApplyThemeBinding.getRoot());

        ThemeUtils.darkMode(this);

        activityApplyThemeBinding.mbChooseFile.setOnClickListener(l -> FileUtils.chooseFile(this));

        activityApplyThemeBinding.mbApplyTheme.setOnClickListener(l -> {
            if (filePath == null) {
                Snackbar.make(l, "未选择有效文件，请先选择文件", Snackbar.LENGTH_LONG)
                        .show();
            } else {
                ThemeUtils.applyTheme(this, filePath);
                filePath = null;
                activityApplyThemeBinding.tvFilePath.setText("");
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
                        Snackbar.make(activityApplyThemeBinding.getRoot(), "你选择的不是主题（mtz）文件", Snackbar.LENGTH_LONG)
                                .show();
                        fileName = null;
                    }

                    if (fileName == null) {
                        filePath = null;
                    } else {
                        // MIUI theme manager needs absolute path.
                        String path = fileUri.getPath();
                        String[] filePathSpiltArray = path.split(":");
                        String filePathSpilt = path.substring(filePathSpiltArray[0].length() + 1);
                        filePath = Environment.getExternalStorageDirectory().getPath() + "/" + filePathSpilt;

                        activityApplyThemeBinding.tvFilePath.setText("你选择的文件是：\n" + filePath);
                    }
                }
            }
        }
    }
}
