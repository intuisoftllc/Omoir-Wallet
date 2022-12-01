package com.intuisoft.plaid.common.network.blockchair.api

import com.intuisoft.plaid.common.network.blockchair.response.BlockStatsResponse
import retrofit2.Call
import retrofit2.http.*

interface BlockchairApi {
    @GET("stats")
    fun getBlockStats(
        @Query("key") akiKey: String? = null
    ): Call<BlockStatsResponse>
}