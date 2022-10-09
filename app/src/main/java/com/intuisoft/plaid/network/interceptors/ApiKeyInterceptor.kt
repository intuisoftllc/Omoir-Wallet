package com.intuisoft.plaid.network.interceptors

import android.content.Context
import android.text.TextUtils
import com.intuisoft.plaid.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authenticatedRequest = chain.request().newBuilder()
            .addHeader("api-key", BuildConfig.NOW_NODES_CLIENT_SECRET)
            .build()
        return chain.proceed(authenticatedRequest)
    }
}