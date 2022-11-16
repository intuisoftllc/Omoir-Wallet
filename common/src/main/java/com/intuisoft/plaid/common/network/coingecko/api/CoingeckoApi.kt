package com.intuisoft.plaid.common.network.nownodes.api

import com.intuisoft.plaid.common.network.nownodes.response.BasicPriceDataResponse
import com.intuisoft.plaid.common.network.nownodes.response.BlockchainStatsResponse
import retrofit2.Call
import retrofit2.http.*

interface CoingeckoApi {

    @GET("simple/price")
    fun getBasicPriceData(
        @Query("ids") id: String = "bitcoin",
        @Query("vs_currencies") currencies: String = "usd,eur,cad",
        @Query("include_market_cap") include: Boolean = true
    ): Call<BasicPriceDataResponse>
}