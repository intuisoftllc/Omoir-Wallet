package com.intuisoft.plaid.network.interceptors

import android.content.Context
import com.intuisoft.plaid.R
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ConnectivityInterceptor(
    private val connectionMonitor: ConnectionMonitor,
    val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        if (!connectionMonitor.isConnected()) {
            throw NoInternetConnectionException(context.getString(0/*R.string.no_connection_message*/))
        } else {
            return chain.proceed(chain.request())
        }
    }

    interface ConnectionMonitor {
        fun isConnected(): Boolean
    }

    class NoInternetConnectionException(message: String?) : IOException(message)
    {
    }
}
