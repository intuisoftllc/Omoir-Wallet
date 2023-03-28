package com.intuisoft.plaid.common.network.coingecko.api

import com.intuisoft.plaid.common.network.coingecko.response.ChartDataResponse
import com.intuisoft.plaid.common.network.coingecko.response.CryptoInfoDataResponse
import com.intuisoft.plaid.common.network.coingecko.response.MarketHistoryDataResponse
import retrofit2.Call
import retrofit2.http.*

interface CoingeckoApi {

    @GET("coins/{ticker}")
    fun getBasicPriceData(
        @Path("ticker") ticker: String,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = true,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") devData: Boolean = false,
        @Query("localization") localization: String = "en",
        @Query("x_cg_pro_api_key") apiKey: String? = null
    ): Call<CryptoInfoDataResponse>

    @GET("coins/{id}/market_chart")
    fun get1DayChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 1,
        @Query("x_cg_pro_api_key") apiKey: String? = null
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun get7DayChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 7,
        @Query("x_cg_pro_api_key") apiKey: String? = null
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun get30DayChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 30,
        @Query("x_cg_pro_api_key") apiKey: String? = null
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun get90DayChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 90,
        @Query("x_cg_pro_api_key") apiKey: String? = null
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun get6MonthChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 180,
        @Query("x_cg_pro_api_key") apiKey: String? = null
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun get1YearChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 365,
        @Query("x_cg_pro_api_key") apiKey: String? = null
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun getMaxChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: String = "max",
        @Query("x_cg_pro_api_key") apiKey: String? = null
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart/range")
    fun getHistoryData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currency: String,
        @Query("from") from: Long,
        @Query("to") to: Long,
        @Query("x_cg_pro_api_key") apiKey: String? = null
    ): Call<MarketHistoryDataResponse>
}