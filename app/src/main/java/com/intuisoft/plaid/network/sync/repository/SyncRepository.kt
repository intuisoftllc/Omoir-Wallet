package com.intuisoft.plaid.network.sync.repository

import android.util.Base64
import android.util.Log
import com.intuisoft.plaid.network.sync.api.SyncApi
import com.intuisoft.plaid.network.sync.response.XpubTxDataResponse
import okhttp3.ResponseBody
import retrofit2.Response
import java.security.SecureRandom

interface SyncRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    suspend fun syncHDWallet(xpub: String): Response<XpubTxDataResponse>

    private class Impl(
        private val api: SyncApi
    ) : SyncRepository {

        override suspend fun syncHDWallet(xpub: String): Response<XpubTxDataResponse> {
            try {
                val response = api.getXpubTxData(xpub, 0)

                if (response.isSuccessful)
                    return Response.success(response.body())
                else
                    return Response.error(null, null)
            } catch (t: Throwable) {
                return Response.error(null, null)
            }
        }
    }

    companion object {
        @JvmStatic
        fun create(
            api: SyncApi
        ): SyncRepository = Impl(api)
    }
}