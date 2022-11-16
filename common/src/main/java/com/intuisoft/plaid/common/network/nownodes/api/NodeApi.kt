package com.intuisoft.plaid.common.network.nownodes.api

import com.intuisoft.plaid.common.network.nownodes.request.BlockStatsRequest
import com.intuisoft.plaid.common.network.nownodes.request.BlockchainInfoRequest
import com.intuisoft.plaid.common.network.nownodes.request.FeeSuggestionRequest
import com.intuisoft.plaid.common.network.nownodes.response.BlockStatsResponse
import com.intuisoft.plaid.common.network.nownodes.response.BlockchainInfoResponse
import com.intuisoft.plaid.common.network.nownodes.response.FeeEstimateResponse
import retrofit2.Call
import retrofit2.http.*

interface NodeApi {

    @POST(".")
    fun getFeeSuggestion(
        @Body request: FeeSuggestionRequest
    ): Call<FeeEstimateResponse>

    @POST(".")
    fun getBlockStats(
        @Body request: BlockStatsRequest
    ): Call<BlockStatsResponse>

    @POST(".")
    fun getBlockchainInfo(
        @Body request: BlockchainInfoRequest
    ): Call<BlockchainInfoResponse>
}