package fr.pchab.androidrtc.ext

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.pchab.androidrtc.R

object DialogType {
    const val INFO = 0
    const val WARNING = 1
    const val ERROR = 2
}

fun Activity.showDialogMessage(
    type: Int = DialogType.INFO,
    message: String?
) {
    message?.let {
        val icon = when(type) {
            DialogType.INFO -> R.drawable.ic_info_dialog
            DialogType.WARNING -> R.drawable.ic_warning_dialog
            else -> R.drawable.ic_error_dialog
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage(it)
            .setIcon(icon)
            .setPositiveButton(R.string.action_ok, null)
            .setCancelable(true)
            .show()
    }
}

fun Fragment.showDialogMessage(
    type: Int = DialogType.INFO,
    message: String?
) {
    message?.let { mes ->
        val icon = when(type) {
            DialogType.INFO -> R.drawable.ic_info_dialog
            DialogType.WARNING -> R.drawable.ic_warning_dialog
            else -> R.drawable.ic_error_dialog
        }
        context?.let { ctx ->
            MaterialAlertDialogBuilder(ctx)
                .setTitle(getString(R.string.app_name))
                .setMessage(mes)
                .setIcon(icon)
                .setPositiveButton(R.string.action_ok, null)
                .setCancelable(true)
                .show()
        }
    }
}

fun Fragment.toast(
    message: String?
) {
    context?.let {
        Toast.makeText(it, message, Toast.LENGTH_LONG).show()
    }
}