package com.intuisoft.plaid.common.network.interceptors

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class AppConnectionMonitor(val context: Context) : ConnectivityInterceptor.ConnectionMonitor {
    override fun isConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}