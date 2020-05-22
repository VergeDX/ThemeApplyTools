package org.hydev.themeapplytools.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.hydev.themeapplytools.databinding.ActivityApplyThemeBinding;
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

        activityApplyThemeBinding.mbChooseFile.setOnClickListener(l -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            // It wiil call onActivityResult method, 7 is requestCode that distinguish request.
            startActivityForResult(intent, 7);
        });

        activityApplyThemeBinding.mbApplyTheme.setOnClickListener(l -> {
            if (filePath == null) {
                Snackbar.make(l, " ! 你还没有选择文件 ! ", Snackbar.LENGTH_LONG)
                        .show();
            } else if (!filePath.endsWith(".mtz")) {
                Snackbar.make(l, " ! 你选择的不是主题（mtz）文件 ! ", Snackbar.LENGTH_LONG)
                        .show();
            } else {
                boolean result = ThemeUtils.applyTheme(this, filePath);
                if (result) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("完成")
                            .setMessage("主题已应用完毕. \n" +
                                    "若主题商店版本太老, \n" +
                                    "可能会没有效果. ")
                            .setPositiveButton("OK", null)
                            .show();
                }

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

        if (requestCode == 7 && resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();

            // Build the absolutely path of theme file.
            String path = fileUri.getPath();
            String[] filePathSpiltArray = path.split(":");
            String filePathSpilt = path.substring(filePathSpiltArray[0].length() + 1);
            filePath = Environment.getExternalStorageDirectory().getPath() + "/" + filePathSpilt;

            if (!fileUri.getPath().endsWith(".mtz")) {
                activityApplyThemeBinding.tvFilePath.setText("你选择的文件是：\n\n" + filePath + "\n\n ! 但它不是主题（mtz）文件 ! ");
            } else {
                activityApplyThemeBinding.tvFilePath.setText("你选择的文件是：\n\n" + filePath);
            }
        }
    }
}
