package com.intuisoft.plaid.common.network.blockchair.api

import com.intuisoft.plaid.common.network.blockchair.response.BlockStatsResponse
import retrofit2.Call
import retrofit2.http.*

interface BlockchairApi {
    @GET("{coin}/stats")
    fun getBlockStats(
        @Path("coin") coin: String,
        @Query("key") akiKey: String? = null
    ): Call<BlockStatsResponse>
    @GET("{coin}/testnet/stats")
    fun getTestnetBlockStats(
        @Path("coin") coin: String,
        @Query("key") akiKey: String? = null
    ): Call<BlockStatsResponse>
}