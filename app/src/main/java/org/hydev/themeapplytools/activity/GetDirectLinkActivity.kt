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
import org.hydev.themeapplytools.utils.*
import org.hydev.themeapplytools.utils.FileUtils.alert
import org.hydev.themeapplytools.utils.FileUtils.alertInfo
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

        val self = this

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

        activityGetDirectLinkBinding.mbSetProxy.setOnClickListener {
            val intent = Intent(this, SetProxyActivity::class.java)
            startActivity(intent)
        }

        // Get theme share link and get theme info to show.
        activityGetDirectLinkBinding.mbGetDirectLink.setOnClickListener {
            val inputShareLink = activityGetDirectLinkBinding.tilInputThemeLink.editText!!.text.toString()
            val themeLinkSplit = inputShareLink.split("/detail/")

            if (themeLinkSplit.size != 2 || themeLinkSplit[1].length < 36) {
                alert(this, "错误", "请输入主题的分享链接，例如：\n$exampleThemeLink")
                        .negative("返回") {}
                        .positive("复制") { FileUtils.copyLink(this, exampleThemeLink) }
                        .show(this)

                return@setOnClickListener
            }

            val proxy: Proxy?
            val proxyConfigSharedPreferences = getSharedPreferences("proxy_config", Context.MODE_PRIVATE)

            val address = proxyConfigSharedPreferences.getString("proxy_address", "")
            val port = proxyConfigSharedPreferences.getString("proxy_port", "")
            val type = proxyConfigSharedPreferences.getString("proxy_type", "")
            val proxyType = if (type == Proxy.Type.SOCKS.name) Proxy.Type.SOCKS else Proxy.Type.HTTP

            // Input address or port is empty.
            proxy = if (address!!.isEmpty() || port!!.isEmpty() || !Patterns.IP_ADDRESS.matcher(address).matches()) {
                null
            } else {
                Proxy(proxyType, InetSocketAddress(address, port.toInt()))
            }

            // Selected MIUI version string, used in api url.
            val miuiVersion = getCheckedMIUIVersion(sharePreferences, activityGetDirectLinkBinding).text.toString()

            // Theme token, used to get theme.
            val themeToken = themeLinkSplit[1].substring(0, 36)

            // Show progress dialog and post get theme info request.
            val progressDialog = ProgressDialog.showDialog(this)
            ThemeUtils.getThemeDownloadLinkAsync(themeToken, miuiVersion, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    progressDialog.cancel()
                    alertInfo(self, "错误", "获取直链失败 \n网络连接质量不佳")
                }

                override fun onResponse(call: Call, response: Response) {
                    progressDialog.cancel()

                    val info = ThemeUtils.getThemeInfo(response.body)

                    if (info == null)
                        alertInfo(self, "失败", "获取主题信息失败 \n可能的原因如下： \n1. 该主题没有 MIUI $miuiVersion 的版本 \n2. 你使用了中国大陆外的 ip \n3. 主题链接输入错误")
                    else {
                        val url = info.getDownloadUrl()

                        alert(self, info.getFileName(), """
                                文件大小：${info.getFileSize()}
                                
                                下载链接：
                                $url
                                
                                哈希值：${info.getFileHash()}
                                
                                """)
                                .negative("复制链接") { FileUtils.copyLink(self, url) }
                                .positive("直接下载") {
                                    ThemeShareDialogUtils.openBrowser(self, url)
                                    Toast.makeText(self, "跳转至浏览器进行下载", Toast.LENGTH_SHORT).show()
                                }
                                .show(self)
                    }
                }
            }, proxy)
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

    private fun getCheckedMIUIVersion(sharePreferences: SharedPreferences, activityGetDirectLinkBinding: ActivityGetDirectLinkBinding): RadioButton {
        return when (sharePreferences.getString("miui_version", "v11")) {
            "v10" -> activityGetDirectLinkBinding.rbMiui10
            "v11" -> activityGetDirectLinkBinding.rbMiui11
            "v12" -> activityGetDirectLinkBinding.rbMiui12

            // Else, reset miui_version to default v11.
            else -> kotlin.run {
                sharePreferences.edit().putString("miui_version", "v11").apply()
                activityGetDirectLinkBinding.rbMiui11
            }
        }
    }
}