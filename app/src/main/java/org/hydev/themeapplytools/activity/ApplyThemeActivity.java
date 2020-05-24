package org.hydev.themeapplytools.activity;

import android.annotation.SuppressLint;
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
    private ActivityApplyThemeBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityApplyThemeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ThemeUtils.darkMode(this);

        binding.mbChooseFile.setOnClickListener(l -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            // It wiil call onActivityResult method, 7 is requestCode that distinguish request.
            startActivityForResult(intent, 7);
        });

        binding.mbApplyTheme.setOnClickListener(l -> {
            String path = binding.mbManualEntry.getEditText().getText().toString();
            if (path == null) {
                Snackbar.make(l, " ! 你还没有选择文件 ! ", Snackbar.LENGTH_LONG)
                        .show();
            } else if (!path.endsWith(".mtz")) {
                Snackbar.make(l, " ! 你选择的不是主题（mtz）文件 ! ", Snackbar.LENGTH_LONG)
                        .show();
            } else {
                boolean result = ThemeUtils.applyTheme(this, path);
                if (result) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("完成")
                            .setMessage("主题已应用完毕. \n" +
                                    "若主题商店版本太老, \n" +
                                    "可能会没有效果. ")
                            .setPositiveButton("OK", null)
                            .show();
                }

                binding.tvFilePath.setText("");
            }
        });
    }

    /**
     * After user choose file, check the file and set filePath.
     *
     * @param requestCode is always 7.
     * @param data        contains user chosen file Uri.
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 7 && resultCode == Activity.RESULT_OK) {
            try {
                // Build the absolutely path of theme file.
                String path = data.getData().getPath();
                String[] filePathSpiltArray = path.split(":");
                String filePathSpilt = path.substring(filePathSpiltArray[0].length() + 1);
                path = Environment.getExternalStorageDirectory().getPath() + "/" + filePathSpilt;

                // Set to text bar
                binding.mbManualEntry.getEditText().setText(path);

                if (!path.endsWith(".mtz")) {
                    binding.tvFilePath.setText(" ! 你选择的文件不是主题（mtz）文件 ! ");
                }
            }
            catch (NullPointerException e) {
                binding.tvFilePath.setText(" ! 报了 NullPointer, 好像不支持自动选择的样子, 请手动输入 ! ");
            }
        }
    }
}
