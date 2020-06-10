package org.hydev.themeapplytools.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object FileUtils {
    /**
     * Copy the given link to clipboard.
     *
     * @param context need to get clipboard manager service
     * @param link the message to copy.
     */
    fun copyLink(context: Context, link: String?) {
        // Get system service, build clip data, and set clip data.
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("downloadLink", link)
        clipboardManager.setPrimaryClip(clipData)
    }
}
