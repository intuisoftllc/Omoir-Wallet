package com.intuisoft.plaid.common.network.interceptors

import android.content.Context
import android.text.TextUtils
import com.intuisoft.plaid.common.CommonService
import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(
    private val apiKey: String,
    private val apiKeyHeaderName: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authenticatedRequest = chain.request().newBuilder()
            .addHeader(apiKeyHeaderName, apiKey)
            .build()
        return chain.proceed(authenticatedRequest)
    }
}