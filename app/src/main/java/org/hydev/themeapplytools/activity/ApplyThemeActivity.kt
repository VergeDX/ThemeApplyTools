package org.hydev.themeapplytools.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import droidninja.filepicker.utils.ContentUriUtils
import org.hydev.themeapplytools.databinding.ActivityApplyThemeBinding
import org.hydev.themeapplytools.utils.ThemeUtils

class ApplyThemeActivity : AppCompatActivity() {
    // View-binding
    private lateinit var activityApplyThemeBinding: ActivityApplyThemeBinding

    // Chosen file path
    private var filePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityApplyThemeBinding = ActivityApplyThemeBinding.inflate(layoutInflater)
        setContentView(activityApplyThemeBinding.root)
        ThemeUtils.darkMode(this)

        // Choose file button on click listener.
        activityApplyThemeBinding.mbChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"

            // It will call onActivityResult method, 7 is requestCode to distinguish request.
            startActivityForResult(intent, 7)
        }
        // Apply theme button on click listener.
        activityApplyThemeBinding.mbApplyTheme.setOnClickListener { l: View? ->
            // File path is null, means have not chosen file.
            if (filePath == null) {
                Snackbar.make(l!!, " ! 你还没有选择文件 ! ", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // File path is not theme file (theme file should have extension name "mtz").
            if (!filePath!!.endsWith(".mtz")) {
                Snackbar.make(l!!, " ! 你选择的不是主题（mtz）文件 ! ", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 
            when (val resultCode = ThemeUtils.applyTheme(this, filePath)) {
                // -1, means MIUI theme manager is not exist.
                -1 -> MaterialAlertDialogBuilder(this)
                        .setTitle("错误")
                        .setMessage("MIUI 主题商店不存在！")
                        .setNegativeButton("返回", null)
                        .show()

                // -2, means MIUI theme manager's status is disable.
                -2 -> MaterialAlertDialogBuilder(this)
                        .setTitle("错误")
                        .setMessage("")
                        .setPositiveButton("启用") { _, _ ->
                            run {
                                // Make an intent to start app settings of MIUI theme manager,
                                // then make toast tell user to enable it.
                                val intent = Intent().apply {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.parse("package:com.android.thememanager")
                                }

                                startActivity(intent)
                                Toast.makeText(this, "请点击下方的 “启用”", Toast.LENGTH_LONG).show()
                            }
                        }
                        .setNegativeButton("返回", null)
                        .show()

                // 0, means posted apply theme intent.
                0 -> MaterialAlertDialogBuilder(this)
                        .setTitle("完成")
                        .setMessage("应用主题成功！ " +
                                "\n若主题商店版本较旧" +
                                "\n可能会没有效果")
                        .setPositiveButton("确定", null)
                        .show()

                // Cannot get here unless not handle this result code.
                else -> throw AssertionError("Cannot get here! $resultCode is not been handle. ")
            }

            // Finally, reset status of file path, and text area.
            filePath = null
            activityApplyThemeBinding.tvFilePath.text = ""
        }
    }

    /**
     * After user choose file, check the file and set filePath.
     *
     * @param requestCode is always 7.
     * @param resultCode is OK when user chosen file.
     * @param data        contains user chosen file Uri.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 7 && resultCode == Activity.RESULT_OK) {
            filePath = ContentUriUtils.getFilePath(this, data?.data!!)

            // Path maybe null if not select file from internal storage.
            val isMtzFile = filePath?.endsWith(".mtz")

            // TODO: 20-6-10 Using Quantity Strings instead, see https://developer.android.com/guide/topics/resources/string-resource.
            @SuppressLint("SetTextI18n")

            if (isMtzFile != null) {
                // Show chosen file path, if it not mtz file, append text.
                activityApplyThemeBinding.tvFilePath.text = "你选择的文件是： \n\n$filePath" +
                        if (!isMtzFile) "\n\n ! 但它不是主题（mtz）文件 ! " else ""
            } else {
                // User not choose the file from internal storage.
                activityApplyThemeBinding.tvFilePath.text =
                        "你需要在内部存储内选择该文件！ \n" +
                                "\n在选择时点击右上角 " +
                                "\n点击 “显示内部存储空间” " +
                                "\n之后在左侧 Tab 中选择"
            }
        }
    }
}

// TODO: 20-6-10 Maybe I can remove READ_EXTERNAL_STORAGE permission...?