package org.hydev.themeapplytools.activity

import android.app.Activity
import android.content.Intent
import android.content.Intent.EXTRA_LOCAL_ONLY
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
            intent.type = "*/*"
            intent.putExtra(EXTRA_LOCAL_ONLY, true)

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

            activityApplyThemeBinding.tvFilePath.text = "你选择的文件是：\n\n$path" +
                    if (!path!!.endsWith(".mtz")) "\n\n ! 但它不是主题（mtz）文件 ! " else ""
        }
    }
}
