package com.intuisoft.plaid.common.network.interceptors

import android.content.Context
import com.intuisoft.plaid.common.R
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ConnectivityInterceptor(
    private val connectionMonitor: ConnectionMonitor,
    val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        if (!connectionMonitor.isConnected()) {
            throw NoInternetConnectionException(context.getString(R.string.no_connection_message))
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
