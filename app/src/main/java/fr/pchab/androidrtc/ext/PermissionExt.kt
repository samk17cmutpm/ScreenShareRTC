package fr.pchab.androidrtc.ext

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

const val PERMISSIONS_REQUEST_CODE = 6969

/**
 * isSpecificPermissionsGranted
 */
fun Fragment.isSpecificPermissionsGranted(permissions: Array<String>): Boolean {
    val size = permissions.size
    for (index in 0 until size) {
        val result = ContextCompat.checkSelfPermission(context!!, permissions[index])
        if (result != PackageManager.PERMISSION_GRANTED)
            return false
    }
    return true
}

/**
 * requestSpecificPermissions
 */
fun Fragment.requestSpecificPermissions(permissions: Array<String>) {
    val size = permissions.size
    var isshouldShowRequestPermissionRationale = false
    for (index in 0 until size) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permissions[index])) {
            isshouldShowRequestPermissionRationale = true
            break
        }
    }
    if (isshouldShowRequestPermissionRationale) {
        Toast.makeText(context!!, "", Toast.LENGTH_LONG).show()
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        }
    }
}

/**
 * checkRequestPermissionsResult
 */
fun Fragment.checkRequestPermissionsResult(grantResults: IntArray): Boolean {
    var permissionGranted = true
    for (grantResult in grantResults) {
        permissionGranted = permissionGranted and (grantResult == PackageManager.PERMISSION_GRANTED)
    }
    return permissionGranted
}

/**
 * isSpecificPermissionsGranted
 */
fun AppCompatActivity.isSpecificPermissionsGranted(permissions: Array<String>): Boolean {
    val size = permissions.size
    for (index in 0 until size) {
        val result = ContextCompat.checkSelfPermission(this, permissions[index])
        if (result != PackageManager.PERMISSION_GRANTED)
            return false
    }
    return true
}

/**
 * requestSpecificPermissions
 */
fun AppCompatActivity.requestSpecificPermissions(permissions: Array<String>) {
    val size = permissions.size
    var isshouldShowRequestPermissionRationale = false
    for (index in 0 until size) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[index])) {
            isshouldShowRequestPermissionRationale = true
            break
        }
    }
    if (isshouldShowRequestPermissionRationale) {
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
        }
    }
}

/**
 * checkRequestPermissionsResult
 */
fun AppCompatActivity.checkRequestPermissionsResult(grantResults: IntArray): Boolean {
    var permissionGranted = true
    for (grantResult in grantResults) {
        permissionGranted = permissionGranted and (grantResult == PackageManager.PERMISSION_GRANTED)
    }
    return permissionGranted
}

/**
 * openSettings
 */
fun Fragment.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", context?.packageName, null)
    intent.data = uri
    startActivity(intent)
}

fun AppCompatActivity.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}