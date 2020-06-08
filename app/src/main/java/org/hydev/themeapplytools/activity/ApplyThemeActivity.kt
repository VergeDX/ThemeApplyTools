package org.hydev.themeapplytools.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import droidninja.filepicker.utils.ContentUriUtils
import org.hydev.themeapplytools.databinding.ActivityApplyThemeBinding
import org.hydev.themeapplytools.utils.FileUtils.alertInfo
import org.hydev.themeapplytools.utils.ThemeUtils

class ApplyThemeActivity : AppCompatActivity() {
    // Lateinit means that it is not assigned when the object is created
    lateinit var activityApplyThemeBinding: ActivityApplyThemeBinding
    var path: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityApplyThemeBinding = ActivityApplyThemeBinding.inflate(layoutInflater)
        setContentView(activityApplyThemeBinding.root)

        ThemeUtils.darkMode(this)

        // Click "Choose File"
        activityApplyThemeBinding.mbChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"

            // It will call onActivityResult method, 7 is requestCode that distinguish request.
            startActivityForResult(intent, 7)
        }

        // Click "Apply Theme"
        activityApplyThemeBinding.mbApplyTheme.setOnClickListener { l: View? ->

            if (path == null) {
                Snackbar.make(l!!, " ! 你还没有选择文件 ! ", Snackbar.LENGTH_LONG).show()
            } else if (!path!!.endsWith(".mtz")) {
                Snackbar.make(l!!, " ! 你选择的不是主题（mtz）文件 ! ", Snackbar.LENGTH_LONG).show()
            } else {
                if (ThemeUtils.applyTheme(this, path)) {
                    alertInfo(this, "完成", "主题已应用完毕.\n若主题商店版本太老,\n可能会没有效果.")
                }

                // TODO: Delete this?
                path = null
                activityApplyThemeBinding.tvFilePath.text = ""
            }
        }
    }

    /**
     * After user choose file, check the file and set filePath.
     *
     * @param requestCode is always 7.
     * @param data        contains user chosen file Uri.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 7 && resultCode == Activity.RESULT_OK) {
            path = ContentUriUtils.getFilePath(this, data?.data!!)

            // Path maybe null if not select file from internal storage.
            val flags = path?.endsWith(".mtz")
            if (flags != null) {
                activityApplyThemeBinding.tvFilePath.text = "你选择的文件是：\n\n$path" +
                        if (!path!!.endsWith(".mtz")) "\n\n ! 但它不是主题（mtz）文件 ! " else ""
            } else {
                activityApplyThemeBinding.tvFilePath.text = "你需要在内部存储内选择该文件！ \n\n在选择时点击右上角 \n点击 “显示内部存储空间” \n之后在左侧 Tab 中选择"
            }
        }
    }
}
