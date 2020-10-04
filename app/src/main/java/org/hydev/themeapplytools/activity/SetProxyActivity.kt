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
import org.hydev.themeapplytools.utils.ProgressDialog
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

        // Used to restore user proxy config.
        val sharePreferences: SharedPreferences = getSharedPreferences("proxy_config", Context.MODE_PRIVATE)

        // Restore the proxy config.
        setProxyActivityBinding.tilProxyAddress.editText?.setText(sharePreferences.getString("proxy_address", ""))
        setProxyActivityBinding.tilProxyPort.editText?.setText(sharePreferences.getString("proxy_port", ""))
        when (sharePreferences.getString("proxy_type", "")) {
            Proxy.Type.SOCKS.name -> setProxyActivityBinding.rbSocks.isChecked = true
            Proxy.Type.HTTP.name -> setProxyActivityBinding.rbHttp.isChecked = true

            // Else, set config to default.
            else -> sharePreferences.edit().putString("proxy_type", Proxy.Type.SOCKS.name).apply()
        }

        // Test proxy button.
        setProxyActivityBinding.mbTestProxy.setOnClickListener {
            // Get user selected proxy type, and input proxy address & port.
            val chosenProxyType = if (setProxyActivityBinding.rbSocks.isChecked) Proxy.Type.SOCKS else Proxy.Type.HTTP
            val inputAddress = setProxyActivityBinding.tilProxyAddress.editText?.text.toString()
            val inputPort = setProxyActivityBinding.tilProxyPort.editText?.text.toString()

            // Input address or port is empty.
            if (inputAddress.isEmpty() || inputPort.isEmpty()) {
                Toast.makeText(this, "未输入完整的代理信息", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // Input not ip address.
            if (!Patterns.IP_ADDRESS.matcher(inputAddress).matches()) {
                Toast.makeText(this, "输入的地址不是 ip！", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Normal condition, test proxy.
            // Build client and request, request url can return ip address.
            val okHttpClient = OkHttpClient.Builder()
                    .proxy(Proxy(chosenProxyType, InetSocketAddress(inputAddress, inputPort.toInt()))).build()
            val request = Request.Builder().url("http://api.ip.sb/ip").build()

            // Show dialog and post request.
            val progressDialog = ProgressDialog.showDialog(this)
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    progressDialog.cancel()

                    runOnUiThread {
                        MaterialAlertDialogBuilder(this@SetProxyActivity)
                                .setTitle("失败")
                                .setMessage("测试失败 \n无网络连接或代理不可用")
                                .setNegativeButton("返回", null)
                                .show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseString = response.body?.string()
                    progressDialog.cancel()

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
            // Get input address, port, selected radio button.
            val inputAddress = setProxyActivityBinding.tilProxyAddress.editText?.text.toString()
            val inputPort = setProxyActivityBinding.tilProxyPort.editText?.text.toString()
            val selectedRadioButton = findViewById<RadioButton>(setProxyActivityBinding.rgProxyType.checkedRadioButtonId)

            // Parse proxy type and save it.
            val proxyType = if (selectedRadioButton == setProxyActivityBinding.rbSocks) Proxy.Type.SOCKS else Proxy.Type.HTTP
            saveProxySetting(inputAddress, inputPort, proxyType)
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
     * Save the proxy config, using given address, port and proxy type.
     *
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
