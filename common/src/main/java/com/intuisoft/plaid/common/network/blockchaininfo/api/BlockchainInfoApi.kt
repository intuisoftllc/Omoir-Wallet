package com.intuisoft.plaid.common.network.blockchair.api

import com.intuisoft.plaid.common.network.blockchaininfo.response.BlockchainStatsResponse
import retrofit2.Call
import retrofit2.http.*

interface BlockchainInfoApi {

    @GET("stats")
    fun getBlockchainStats(): Call<BlockchainStatsResponse>
}