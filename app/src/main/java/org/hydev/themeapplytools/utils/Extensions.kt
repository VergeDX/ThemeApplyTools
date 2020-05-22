package org.hydev.themeapplytools.utils

import android.app.Activity
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder


// Material Alert Dialog Builder patches

/**
 * Show on UI thread
 */
fun MaterialAlertDialogBuilder.show(activity: Activity) = activity.runOnUiThread { show() }
