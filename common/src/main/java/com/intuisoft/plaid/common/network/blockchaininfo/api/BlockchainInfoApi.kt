package com.intuisoft.plaid.common.network.nownodes.api

import com.intuisoft.plaid.common.network.nownodes.response.BlockchainStatsResponse
import retrofit2.Call
import retrofit2.http.*

interface BlockchainInfoApi {

    @GET("q/unconfirmedcount")
    fun getUnconfirmedTxSize(): Call<Int>

    @GET("q/totalbc")
    fun getCirculatingSupply(): Call<Long>

    @GET("stats")
    fun getBlockchainStats(): Call<BlockchainStatsResponse>
}