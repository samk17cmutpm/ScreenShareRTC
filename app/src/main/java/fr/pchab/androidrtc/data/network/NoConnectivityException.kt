package fr.pchab.androidrtc.data.network

import android.content.Context
import fr.pchab.androidrtc.R
import java.io.IOException

class NoConnectivityException(
    private val context: Context?
) : IOException() {
    // You can send any message whatever you want from here.
    override val message: String?
        get() = context?.getString(R.string.error_network_error_des)
    // You can send any message whatever you want from here.
}