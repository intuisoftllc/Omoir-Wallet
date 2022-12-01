package com.intuisoft.plaid.common.network.blockchair.api

import com.intuisoft.plaid.common.network.blockchair.response.CurrencyRangeLimitResponse
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyResponse
import com.intuisoft.plaid.common.network.simpleswap.request.ExchangeInfoRequest
import com.intuisoft.plaid.common.network.simpleswap.response.ExchangeInfoResponse
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

    @POST("create_exchange")
    fun createExchange(
        @Query("api_key") apiKey: String,
        @Body exchangeInfo: ExchangeInfoRequest
    ): Call<ExchangeInfoResponse>

    @GET("get_exchange")
    fun getExchange(
        @Query("api_key") apiKey: String,
        @Query("id") id: String
    ): Call<ExchangeInfoResponse>
}