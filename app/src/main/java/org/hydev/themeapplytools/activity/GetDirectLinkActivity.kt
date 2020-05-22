package org.hydev.themeapplytools.activity

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.hydev.themeapplytools.databinding.ActivityGetDirectLinkBinding
import org.hydev.themeapplytools.databinding.DialogThemeShareBinding
import org.hydev.themeapplytools.utils.FileUtils
import org.hydev.themeapplytools.utils.FileUtils.alert
import org.hydev.themeapplytools.utils.FileUtils.alertInfo
import org.hydev.themeapplytools.utils.ThemeShareDialogUtils
import org.hydev.themeapplytools.utils.ThemeUtils
import java.io.IOException

class GetDirectLinkActivity : AppCompatActivity() {
    val EXAMPLE_THEME_LINK = "http://zhuti.xiaomi.com/detail/d555981b-e6af-4ea9-9eb2-e47cfbc3edfa"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityGetDirectLinkBinding = ActivityGetDirectLinkBinding.inflate(layoutInflater)
        setContentView(activityGetDirectLinkBinding.root)

        ThemeUtils.darkMode(this)

        val self = this

        // Get theme share link and get theme info to show.
        activityGetDirectLinkBinding.mbGetDirectLink.setOnClickListener {
            val inputShareLink = activityGetDirectLinkBinding.tilInputThemeLink.editText!!.text.toString()
            val themeLinkSplit = inputShareLink.split("/detail/")

            if (themeLinkSplit.size != 2) {
                alert(this, "错误", "请输入主题的分享链接，例如：\n$EXAMPLE_THEME_LINK")
                    .setNegativeButton("返回", null)
                    .setPositiveButton("复制") { _: DialogInterface?, _: Int -> FileUtils.copyLink(this, EXAMPLE_THEME_LINK) }
                    .show()

                return@setOnClickListener
            }

            // Download
            ThemeUtils.getThemeDownloadLinkAsync(inputShareLink, object : Callback {
                override fun onFailure(call: Call, e: IOException) { alertInfo(self, "错误", "获取直链失败,\n请检查网络连接后重试.") }

                override fun onResponse(call: Call, response: Response) {
                    val info = ThemeUtils.getThemeInfo(response.body)

                    if (info == null) { alertInfo(self, "失败", "获取主题信息失败,\n可能是链接输入错误\n(在国外的话好像不能获取).") }
                    else {
                        val downloadUrl = info.getDownloadUrl()

                        runOnUiThread {
                            MaterialAlertDialogBuilder(self)
                                .setTitle(info.getFileName())
                                .setMessage("""
                                    文件大小：${info.getFileSize()}
                                    
                                    下载链接：
                                    $downloadUrl
                                    
                                    哈希值：${info.getFileHash()}
                                    
                                    """.trimIndent())
                                .setNegativeButton("复制链接") { dialog: DialogInterface?, which: Int -> FileUtils.copyLink(self, downloadUrl) }
                                .setPositiveButton("直接下载") { dialog: DialogInterface?, which: Int -> FileUtils.systemDownload(self, info) }
                                .show()
                        }
                    }
                }
            })
        }

        // Share sites button
        activityGetDirectLinkBinding.mcvThemeShareSite.setOnClickListener {
            val dialogThemeShareBinding = DialogThemeShareBinding.inflate(layoutInflater)
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)

            materialAlertDialogBuilder.setView(dialogThemeShareBinding.root).setPositiveButton("OK", null)
            ThemeShareDialogUtils.setOnClickListener(this, dialogThemeShareBinding)
            materialAlertDialogBuilder.show()
        }
    }
}
