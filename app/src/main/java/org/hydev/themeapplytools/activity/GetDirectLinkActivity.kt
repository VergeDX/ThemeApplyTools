package org.hydev.themeapplytools.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.hydev.themeapplytools.databinding.ActivityGetDirectLinkBinding
import org.hydev.themeapplytools.databinding.DialogThemeShareBinding
import org.hydev.themeapplytools.utils.FileUtils
import org.hydev.themeapplytools.utils.ProgressDialog
import org.hydev.themeapplytools.utils.ThemeShareDialogUtils
import org.hydev.themeapplytools.utils.ThemeUtils
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy

class GetDirectLinkActivity : AppCompatActivity() {
    private val exampleThemeLink = "http://zhuti.xiaomi.com/detail/d555981b-e6af-4ea9-9eb2-e47cfbc3edfa"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityGetDirectLinkBinding = ActivityGetDirectLinkBinding.inflate(layoutInflater)
        setContentView(activityGetDirectLinkBinding.root)
        ThemeUtils.darkMode(this)

        // A share preferences to get / save config.
        val sharePreferences = getPreferences(Context.MODE_PRIVATE)

        // Get selected miui version, and restore it.
        val checkedMIUIVersion = getCheckedMIUIVersion(sharePreferences, activityGetDirectLinkBinding)
        activityGetDirectLinkBinding.rgMiuiVersion.check(checkedMIUIVersion.id)

        // Save miui 12 value instant.
        activityGetDirectLinkBinding.rgMiuiVersion.setOnCheckedChangeListener { _, checkedId ->
            run {
                when (checkedId) {
                    activityGetDirectLinkBinding.rbMiui10.id -> sharePreferences.edit().putString("miui_version", "v10").apply()
                    activityGetDirectLinkBinding.rbMiui11.id -> sharePreferences.edit().putString("miui_version", "v11").apply()
                    activityGetDirectLinkBinding.rbMiui12.id -> sharePreferences.edit().putString("miui_version", "v12").apply()
                }
            }
        }

        // Get info / Download file button.
        activityGetDirectLinkBinding.mbGetDirectLink.setOnClickListener {
            // Get user input, and split it.
            val inputShareLink = activityGetDirectLinkBinding.tilInputThemeLink.editText!!.text.toString()
            val themeLinkSplit = inputShareLink.split("/detail/")

            // If split array size != 2, or array[1]'s length < 36,
            // means user not input theme link or theme link text.
            // By the way, the text of "《Pure 轻雨》这个主题不错，推荐一下。下载地址: http://zhuti.xiaomi.com/detail/d555981b-e6af-4ea9-9eb2-e47cfbc3edfa ，来自 @MIUI"
            // are also acceptable.
            if (themeLinkSplit.size != 2 || themeLinkSplit[1].length < 36) {
                // Wrong theme share link, show dialog and return.
                MaterialAlertDialogBuilder(this)
                        .setTitle("错误")
                        .setMessage("请输入主题的分享链接，例如： \n$exampleThemeLink")
                        .setNegativeButton("返回", null)
                        .setPositiveButton("复制") { _, _ ->
                            run {
                                FileUtils.copyLink(this, exampleThemeLink)
                                Toast.makeText(this, "已复制到剪切版", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .show()

                return@setOnClickListener
            }

            // At here, input theme link is acceptable,
            // so we get proxy settings.
            val proxy: Proxy?
            val proxyConfigSharedPreferences = getSharedPreferences("proxy_config", Context.MODE_PRIVATE)

            // Raw data from the proxy config shared preferences.
            val address = proxyConfigSharedPreferences.getString("proxy_address", "")
            val port = proxyConfigSharedPreferences.getString("proxy_port", "")
            val type = proxyConfigSharedPreferences.getString("proxy_type", "")

            // Cast proxy type to specific object.
            val proxyType = if (type == Proxy.Type.SOCKS.name) Proxy.Type.SOCKS else Proxy.Type.HTTP

            // If there is no address, port, or address is not legal,
            // make proxy is null, which is no proxy settings.
            // Else, build a proxy object.
            proxy = if (address!!.isEmpty() || port!!.isEmpty() || !Patterns.IP_ADDRESS.matcher(address).matches()) null
            else Proxy(proxyType, InetSocketAddress(address, port.toInt()))

            // Get selected MIUI version radio button,
            // then cast it to string, so we can used it in api.
            val miuiVersion = getCheckedMIUIVersion(sharePreferences, activityGetDirectLinkBinding).text.toString()
            // Theme token, used to get theme. At here, theme token is exist.
            val themeToken = themeLinkSplit[1].substring(0, 36)

            // Show progress dialog and post get theme info request.
            // save alert dialog object, so we can cancel it.
            val progressDialog = ProgressDialog.showDialog(this)
            ThemeUtils.getThemeDownloadLinkAsync(themeToken, miuiVersion, proxy, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Failure here, cancel progress dialog, and show error dialog.
                    progressDialog.cancel()
                    runOnUiThread {
                        MaterialAlertDialogBuilder(this@GetDirectLinkActivity)
                                .setTitle("错误")
                                .setMessage("获取直链失败 \n网络连接质量不佳")
                                .setNegativeButton("返回", null)
                                .show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    // Get server response, cancel progress dialog.
                    progressDialog.cancel()

                    // From the official server, the apiData section.
                    // It maybe null if apiCode is -1, at this time apiData is not object.
                    // So parse will fail when apiCode is -1.
                    // Also, response's body cannot be null if using callback.
                    val themeApiData = ThemeUtils.parseThemeInfo(response.body!!.string())

                    // If it is null, means apiCode is -1.
                    if (themeApiData == null) {
                        runOnUiThread {
                            MaterialAlertDialogBuilder(this@GetDirectLinkActivity)
                                    .setTitle("失败")
                                    .setMessage("""
                                        获取主题信息失败 
                                        可能的原因如下： 
                                        1. 该主题没有 MIUI $miuiVersion 的版本 
                                        2. 你使用了中国大陆外的 ip 
                                        3. 主题链接输入错误 
                                    """.trimIndent())
                                    .setNegativeButton("返回", null)
                                    .show()
                        }

                        return
                    }

                    // Normal condition, simply show the result dialog.
                    // Theme download url, it was decoded.
                    val themeDownloadLink = themeApiData.themeDownloadUrl
                    runOnUiThread {
                        // Make a dialog to show theme info.
                        MaterialAlertDialogBuilder(this@GetDirectLinkActivity)
                                .setTitle(themeApiData.themeFileName)
                                .setMessage("""
                                    文件大小： ${themeApiData.themeFileSize}
                                    
                                    下载链接： 
                                    $themeDownloadLink
                                    
                                    哈希值： 
                                    ${themeApiData.themeFileHash}
                                    """.trimIndent())
                                // Copy download link to clipboard, and make a toast.
                                .setNegativeButton("复制链接") { _, _ ->
                                    run {
                                        FileUtils.copyLink(this@GetDirectLinkActivity, themeDownloadLink)
                                        Toast.makeText(this@GetDirectLinkActivity, "", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                // Open browser to download theme file, and make a toast.
                                .setPositiveButton("下载文件") { _, _ ->
                                    run {
                                        ThemeShareDialogUtils.openBrowser(this@GetDirectLinkActivity, themeDownloadLink)
                                        Toast.makeText(this@GetDirectLinkActivity, "已在浏览器中打开下载链接", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .show()
                    }
                }
            })
        }
        // Non China mainland ip, set proxy's button.
        activityGetDirectLinkBinding.mbSetProxy.setOnClickListener {
            val intent = Intent(this, SetProxyActivity::class.java)
            startActivity(intent)
        }
        // Third-party theme share site button.
        activityGetDirectLinkBinding.mcvThemeShareSite.setOnClickListener {
            // Inflate the dialog, and show it.
            val dialogThemeShareBinding = DialogThemeShareBinding.inflate(layoutInflater)

            // Build the dialog.
            MaterialAlertDialogBuilder(this).apply {
                setView(dialogThemeShareBinding.root)
                setPositiveButton("确定", null)

                // Register click event of theme site, and show it.
                ThemeShareDialogUtils.setOnClickListener(this@GetDirectLinkActivity, dialogThemeShareBinding)
                show()
            }
        }
    }

    /**
     * Get selected MIUI version radio button from share preferences.
     *
     * @param sharePreferences to read configuration.
     * @param activityGetDirectLinkBinding to get radio button.
     * @return chosen radio button object.
     */
    private fun getCheckedMIUIVersion(sharePreferences: SharedPreferences, activityGetDirectLinkBinding: ActivityGetDirectLinkBinding): RadioButton {
        // Return corresponding radio button object.
        return when (sharePreferences.getString("miui_version", "v11")) {
            "v10" -> activityGetDirectLinkBinding.rbMiui10
            "v11" -> activityGetDirectLinkBinding.rbMiui11
            "v12" -> activityGetDirectLinkBinding.rbMiui12

            // Else, reset miui_version to default v11.
            else -> run {
                sharePreferences.edit().putString("miui_version", "v11").apply()
                activityGetDirectLinkBinding.rbMiui11
            }
        }
    }
}

// TODO: 20-6-10 Using """text""" in all multi-line text context.