package org.hydev.themeapplytools.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.*
import org.hydev.themeapplytools.databinding.ActivitySetProxyBinding
import org.hydev.themeapplytools.utils.ThemeUtils
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy

class SetProxyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeUtils.darkMode(this)

        val setProxyActivityBinding = ActivitySetProxyBinding.inflate(layoutInflater)
        setContentView(setProxyActivityBinding.root)

        // Restore user proxy config.
        val sharePreferences: SharedPreferences = getSharedPreferences("proxy_config", Context.MODE_PRIVATE)

        setProxyActivityBinding.tilProxyAddress.editText?.setText(sharePreferences.getString("proxy_address", ""))
        setProxyActivityBinding.tilProxyPort.editText?.setText(sharePreferences.getString("proxy_port", ""))

        when (sharePreferences.getString("proxy_type", "")) {
            Proxy.Type.SOCKS.name -> setProxyActivityBinding.rbSocks.isChecked = true
            Proxy.Type.HTTP.name -> setProxyActivityBinding.rbHttp.isChecked = true
        }

        val api_getIp = "http://api.ip.sb/ip"

        // Test proxy button.
        setProxyActivityBinding.mbTestProxy.setOnClickListener {
            val chosenProxyType = if (setProxyActivityBinding.rbSocks.isChecked) Proxy.Type.SOCKS else Proxy.Type.HTTP

            val address = setProxyActivityBinding.tilProxyAddress.editText?.text.toString()
            val port = setProxyActivityBinding.tilProxyPort.editText?.text.toString()

            // Input address or port is empty.
            if (address.isEmpty() || port.isEmpty()) {
                Toast.makeText(this, "未输入完整的代理信息", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // Input not ip address.
            if (!Patterns.IP_ADDRESS.matcher(address).matches()) {
                Toast.makeText(this, "输入的地址不是 ip！", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val okHttpClient = OkHttpClient.Builder()
                    .proxy(Proxy(chosenProxyType, InetSocketAddress(address, port.toInt())))
                    .build()
            val request = Request.Builder().url(api_getIp).build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        MaterialAlertDialogBuilder(this@SetProxyActivity)
                                .setTitle("失败")
                                .setMessage("测试失败\n无网络连接或代理不可用")
                                .setNegativeButton("返回", null)
                                .show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseString = response.body?.string()

                    runOnUiThread {
                        MaterialAlertDialogBuilder(this@SetProxyActivity)
                                .setTitle("成功")
                                .setMessage("您当前的 ip 是：$responseString")
                                .setPositiveButton("确定", null)
                                .show()
                    }
                }
            })
        }

        // Save proxy config button.
        setProxyActivityBinding.mbSaveProxySetting.setOnClickListener {
            val address = setProxyActivityBinding.tilProxyAddress.editText?.text.toString()
            val port = setProxyActivityBinding.tilProxyPort.editText?.text.toString()

            val selectedRadioButton = findViewById<RadioButton>(setProxyActivityBinding.rgProxyType.checkedRadioButtonId)
            val proxyType = if (selectedRadioButton == setProxyActivityBinding.rbSocks) Proxy.Type.SOCKS else Proxy.Type.HTTP

            saveProxySetting(address, port, proxyType)
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        }

        // Clean all proxy config.
        setProxyActivityBinding.mbCleanConfig.setOnClickListener {
            // Clean the saved proxy config.
            saveProxySetting("", "", null)

            // Clean the input proxy config.
            setProxyActivityBinding.tilProxyAddress.editText?.setText("")
            setProxyActivityBinding.tilProxyPort.editText?.setText("")
            setProxyActivityBinding.rbSocks.isChecked = true

            Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Save the proxy config.
     *
     * @see Patterns.IP_ADDRESS
     * @see Proxy.Type
     */
    private fun saveProxySetting(address: String, port: String, type: Proxy.Type?) {
        val sharePreferences: SharedPreferences = getSharedPreferences("proxy_config", Context.MODE_PRIVATE)
        val typeString = type?.name ?: ""

        sharePreferences.edit().putString("proxy_address", address).apply()
        sharePreferences.edit().putString("proxy_port", port).apply()
        sharePreferences.edit().putString("proxy_type", typeString).apply()
    }
}