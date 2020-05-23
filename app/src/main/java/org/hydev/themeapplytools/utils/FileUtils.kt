package org.hydev.themeapplytools.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object FileUtils {
    /**
     * Copy any message (link) to clipboard.
     *
     * @param link message needs to copy.
     */
    fun copyLink(activity: Activity, link: String?) {
        val clipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("downloadLink", link)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(activity, "已复制到剪切版", Toast.LENGTH_LONG).show()
    }

    /**
     * Create an alert
     */
    fun alert(activity: Activity, title: String, message: String): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(activity).setTitle(title).setMessage(message.trimIndent())
    }

    /**
     * Show info alert
     */
    fun alertInfo(activity: Activity, title: String, message: String) {
        alert(activity, title, message).setPositiveButton("OK", null).show(activity)
    }
}
