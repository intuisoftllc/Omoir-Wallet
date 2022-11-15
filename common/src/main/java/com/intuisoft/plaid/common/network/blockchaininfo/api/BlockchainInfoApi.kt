package com.intuisoft.plaid.common.network.nownodes.api

import com.intuisoft.plaid.common.network.nownodes.request.FeeSuggestionRequest
import com.intuisoft.plaid.common.network.nownodes.response.FeeEstimateResponse
import com.intuisoft.plaid.common.network.nownodes.response.PriceConversionRatesResponse
import retrofit2.Call
import retrofit2.http.*

interface BlockchainInfoApi {

    @GET("ticker")
    fun getPriceConversionRates(): Call<PriceConversionRatesResponse>

}