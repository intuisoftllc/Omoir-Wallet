package com.intuisoft.plaid.network.sync.api

import android.net.Uri
import com.intuisoft.plaid.network.sync.response.XpubTxDataResponse
import com.intuisoft.plaid.util.Constants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SyncApi {

    @GET("xpub/{xpubAddr}")
    suspend fun getXpubTxData(
        @Path("xpubAddr") xpub: String = "code",
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = Constants.Limit.PAGE_LIMIT,
        @Query("details") details: String = Constants.ServerStrings.TRANSACTION_DETAILS
    ): Response<XpubTxDataResponse>

}