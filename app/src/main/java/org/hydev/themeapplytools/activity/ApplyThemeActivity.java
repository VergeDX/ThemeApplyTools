package org.hydev.themeapplytools.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
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

        MaterialButton mb_chooseFile = findViewById(R.id.mb_chooseFile);
        mb_chooseFile.setOnClickListener(l -> FileUtils.chooseFile(this));

        TextView tv_filePath = findViewById(R.id.tv_filePath);
        MaterialButton mb_applyTheme = findViewById(R.id.mb_applyTheme);
        mb_applyTheme.setOnClickListener(l -> {
            if (filePath == null) {
                Snackbar.make(l, R.string.no_Choose_File, Snackbar.LENGTH_LONG)
                        .show();
            } else {
                ThemeUtils.applyTheme(this, filePath);
                filePath = null;
                tv_filePath.setText("");
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
                    }

                    if (fileName == null) {
                        filePath = null;
                    } else {
                        // MIUI theme manager needs absolute path.
                        String path = fileUri.getPath();
                        String[] filePathSpiltArray = path.split(":");
                        String filePathSpilt = path.substring(filePathSpiltArray[0].length() + 1);
                        filePath = Environment.getExternalStorageDirectory().getPath() + "/" + filePathSpilt;

                        TextView tv_filePath = findViewById(R.id.tv_filePath);
                        tv_filePath.setText("你选择的文件是：\n" + filePath);
                    }
                }
            }
        }
    }
}
