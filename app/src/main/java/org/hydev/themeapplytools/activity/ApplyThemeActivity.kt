package org.hydev.themeapplytools.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import droidninja.filepicker.utils.ContentUriUtils
import org.hydev.themeapplytools.databinding.ActivityApplyThemeBinding
import org.hydev.themeapplytools.utils.ThemeUtils
import java.util.*

class ApplyThemeActivity : AppCompatActivity() {
    // View-binding
    private lateinit var activityApplyThemeBinding: ActivityApplyThemeBinding

    // Chosen file path, set in onActivityResult function by click choose file button.
    private var mtzFilePath: Optional<String> = Optional.empty()

    companion object {
        private const val CHOOSE_FILE_REQUEST_CODE = 7
    }

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

            // This will call onActivityResult method with CHOOSE_FILE_REQUEST_CODE.
            startActivityForResult(intent, CHOOSE_FILE_REQUEST_CODE)
        }

        // Apply theme button on click listener.
        activityApplyThemeBinding.mbApplyTheme.setOnClickListener {
            // File path not exist, means have not chosen file.
            if (!mtzFilePath.isPresent) {
                Snackbar.make(it, "你还没有选择文件. ", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // File path is not theme file (theme file should have extension name "mtz").
            if (!mtzFilePath.get().endsWith(".mtz")) {
                Snackbar.make(it, "你选择的不是主题（mtz）文件. ", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            when (ThemeUtils.applyTheme(this, mtzFilePath.get())) {
                ThemeUtils.ApplyThemeResult.NO_THEME_MANAGER ->
                    MaterialAlertDialogBuilder(this)
                            .setTitle("错误")
                            .setMessage("""
                                MIUI 主题商店不存在
                                请原生用户不要来搞事x 
                            """.trimIndent())
                            .setNegativeButton("返回", null)
                            .show()

                ThemeUtils.ApplyThemeResult.THEME_MANAGER_DISABLED ->
                    MaterialAlertDialogBuilder(this)
                            .setTitle("错误")
                            .setMessage("""
                                MIUI 主题商店被冻结
                                请手动启用后再次应用主题
                            """.trimIndent())
                            .setPositiveButton("启用") { _, _ ->
                                run {
                                    // Make an intent to start app settings of MIUI theme manager,
                                    // then make toast tell user to enable it.
                                    val intent = Intent().apply {
                                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        data = Uri.parse("package:com.android.thememanager")
                                    }

                                    startActivity(intent)
                                    Toast.makeText(this, "请启用 MIUI 主题商店，以便应用主题", Toast.LENGTH_LONG).show()
                                }
                            }
                            .setNegativeButton("返回", null)
                            .show()

                ThemeUtils.ApplyThemeResult.SENT_INTENT -> {
                    MaterialAlertDialogBuilder(this)
                            .setTitle("完成")
                            .setMessage("""
                                应用主题成功
                                若主题商店版本较旧 / 较新
                                可能会没有效果
                            """.trimIndent())
                            .setPositiveButton("确定", null)
                            .show()

                    // Success sent intent, clean selected file.
                    mtzFilePath = Optional.empty()
                    activityApplyThemeBinding.tvFilePath.text = ""
                }
            }
        }
    }

    /**
     * After click select file button, this function will be called with request code.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        @SuppressLint("SetTextI18n")
        // TODO: 20-6-10 Using Quantity Strings instead, see https://developer.android.com/guide/topics/resources/string-resource.
        if (requestCode == CHOOSE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mtzFilePath = try {
                val filePath = ContentUriUtils.getFilePath(this, data?.data!!)
                Optional.ofNullable(filePath)
            } catch (e: NumberFormatException) {
                // Not select file from internal storage, will cause parse file error.
                activityApplyThemeBinding.tvFilePath.text = """
                    解析文件路径时发生错误
                    请尝试在内部存储中选择文件
                """.trimIndent()

                return
            }

            // Show chosen file path.
            activityApplyThemeBinding.tvFilePath.text = """
                    你选择的文件是：
                    ${mtzFilePath.get()}
                """.trimIndent()
        }
    }
}
