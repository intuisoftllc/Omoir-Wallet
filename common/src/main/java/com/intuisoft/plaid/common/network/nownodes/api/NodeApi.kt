package com.intuisoft.plaid.common.network.nownodes.api

import com.intuisoft.plaid.common.network.nownodes.request.FeeSuggestionRequest
import com.intuisoft.plaid.common.network.nownodes.response.FeeEstimateResponse
import retrofit2.Call
import retrofit2.http.*

interface NodeApi {

    @POST(".")
    fun getFeeSuggestion(
        @Body request: FeeSuggestionRequest
    ): Call<FeeEstimateResponse>

}