package com.intuisoft.plaid.common.network.interceptors

import android.content.Context
import android.text.TextUtils
import com.intuisoft.plaid.common.CommonService
import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authenticatedRequest = chain.request().newBuilder()
            .addHeader("api-key", CommonService.getNowNodesClientSecret())
            .build()
        return chain.proceed(authenticatedRequest)
    }
}