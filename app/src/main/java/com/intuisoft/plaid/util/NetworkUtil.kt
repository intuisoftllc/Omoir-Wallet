package com.intuisoft.plaid.util

import android.content.Context
import android.net.ConnectivityManager


object NetworkUtil {
    fun hasInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting
        if (isConnected) {
            return true
        }

        return false
    }
}