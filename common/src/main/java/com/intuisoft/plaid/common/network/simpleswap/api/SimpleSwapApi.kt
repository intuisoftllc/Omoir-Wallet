package com.intuisoft.plaid.common.network.nownodes.api

import com.intuisoft.plaid.common.network.nownodes.response.BasicPriceDataResponse
import com.intuisoft.plaid.common.network.nownodes.response.CurrencyRangeLimitResponse
import com.intuisoft.plaid.common.network.nownodes.response.SupportedCurrencyResponse
import retrofit2.Call
import retrofit2.http.*

interface SimpleSwapApi {

    @GET("get_all_currencies")
    fun getAllSupportedCurrencies(
        @Query("api_key") apiKey: String
    ): Call<List<SupportedCurrencyResponse>>

    @GET("get_pairs")
    fun getPairs(
        @Query("api_key") apiKey: String,
        @Query("fixed") fixed: Boolean,
        @Query("symbol") symbol: String,
    ): Call<List<String>>

    @GET("get_ranges")
    fun getRanges(
        @Query("api_key") apiKey: String,
        @Query("fixed") fixed: Boolean,
        @Query("currency_from") from: String,
        @Query("currency_to") to: String,
    ): Call<CurrencyRangeLimitResponse>

    @GET("get_estimated")
    fun getEstimatedReceiveAmount(
        @Query("api_key") apiKey: String,
        @Query("fixed") fixed: Boolean,
        @Query("currency_from") from: String,
        @Query("currency_to") to: String,
        @Query("amount") amount: Double,
    ): Call<String>
}