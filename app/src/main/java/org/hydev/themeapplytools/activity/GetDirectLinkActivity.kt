package org.hydev.themeapplytools.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.hydev.themeapplytools.databinding.ActivityGetDirectLinkBinding
import org.hydev.themeapplytools.databinding.DialogThemeShareBinding
import org.hydev.themeapplytools.utils.FileUtils
import org.hydev.themeapplytools.utils.ThemeShareDialogUtils
import org.hydev.themeapplytools.utils.ThemeUtils
import java.io.IOException

class GetDirectLinkActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val activityGetDirectLinkBinding = ActivityGetDirectLinkBinding.inflate(layoutInflater)
        setContentView(activityGetDirectLinkBinding.root)

        ThemeUtils.darkMode(this)

        // Get theme share link and get theme info to show.
        activityGetDirectLinkBinding.mbGetDirectLink.setOnClickListener {
            val inputShareLink = activityGetDirectLinkBinding.tilInputThemeLink.editText!!.text.toString()
            val themeLinkSplit = inputShareLink.split("/detail/")

            if (themeLinkSplit.size != 2)
            {
                MaterialAlertDialogBuilder(this)
                    .setTitle("错误")
                    .setMessage("请输入主题的分享链接，例如：\n$EXAMPLE_THEME_LINK")
                    .setNegativeButton("返回", null)
                    .setPositiveButton("复制") { dialog: DialogInterface?, which: Int ->
                        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText("ExampleUrl", EXAMPLE_THEME_LINK)
                        clipboardManager.setPrimaryClip(clipData)
                        Toast.makeText(this, "已复制示例链接", Toast.LENGTH_SHORT).show()
                    }
                    .show()
                return@setOnClickListener
            }
            ThemeUtils.getThemeDownloadLinkAsync(inputShareLink, object : Callback
            {
                override fun onFailure(call: Call, e: IOException)
                {
                    runOnUiThread {
                        MaterialAlertDialogBuilder(this@GetDirectLinkActivity)
                            .setTitle("错误")
                            .setMessage("""
                                获取直链失败, 
                                请检查网络连接后重试. 
                                """.trimIndent())
                            .setNegativeButton("OK", null)
                            .show()
                    }
                }

                override fun onResponse(call: Call, response: Response)
                {
                    val info = ThemeUtils.getThemeInfo(response.body)

                    // Cannot get theme info, maybe link is wrong.
                    if (info == null)
                    {
                        runOnUiThread {
                            MaterialAlertDialogBuilder(this@GetDirectLinkActivity)
                                .setTitle("失败")
                                .setMessage("""
                                    获取主题信息失败 
                                    可能是链接输入错误 
                                    """.trimIndent())
                                .setNegativeButton("OK", null).show()
                        }
                    }
                    else
                    {
                        val downloadUrl = info.getDownloadUrl()

                        runOnUiThread {
                            MaterialAlertDialogBuilder(this@GetDirectLinkActivity)
                                .setTitle(info.getFileName())
                                .setMessage("""
                                    文件大小：${info.getFileSize()}
                                    
                                    下载链接：
                                    $downloadUrl
                                    
                                    哈希值：${info.getFileHash()}
                                    
                                    """.trimIndent())
                                .setNegativeButton("复制链接") { dialog: DialogInterface?, which: Int -> FileUtils.copyLink(this@GetDirectLinkActivity, downloadUrl) }
                                .setPositiveButton("直接下载") { dialog: DialogInterface?, which: Int -> FileUtils.systemDownload(this@GetDirectLinkActivity, info) }
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

    companion object
    {
        private const val EXAMPLE_THEME_LINK = "http://zhuti.xiaomi.com/detail/d555981b-e6af-4ea9-9eb2-e47cfbc3edfa"
    }
}
