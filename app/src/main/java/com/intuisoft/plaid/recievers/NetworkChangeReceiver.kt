package com.intuisoft.plaid.recievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import com.intuisoft.plaid.util.NetworkUtil


class NetworkChangeReceiver(private val onConnectionChanged: (Boolean) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        onConnectionChanged(NetworkUtil.hasInternet(context))
    }
}
