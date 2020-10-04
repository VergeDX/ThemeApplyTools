package org.hydev.themeapplytools.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.hydev.themeapplytools.R

object ProgressDialog {
    fun showDialog(context: Context): AlertDialog {
        return MaterialAlertDialogBuilder(context)
                .setView(R.layout.dialog_in_progress)
                .setCancelable(false)
                .show()
    }
}
