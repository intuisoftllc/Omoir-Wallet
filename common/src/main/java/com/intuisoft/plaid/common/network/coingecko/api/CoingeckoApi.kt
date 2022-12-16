package com.intuisoft.plaid.common.network.blockchair.api

import com.intuisoft.plaid.common.network.blockchair.response.BasicPriceDataResponse
import com.intuisoft.plaid.common.network.blockchair.response.ChartDataResponse
import com.intuisoft.plaid.common.network.blockchair.response.MarketHistoryDataResponse
import retrofit2.Call
import retrofit2.http.*

interface CoingeckoApi {

    @GET("simple/price")
    fun getBasicPriceData(
        @Query("ids") id: String = "bitcoin",
        @Query("vs_currencies") currencies: String = "usd,eur,cad",
        @Query("include_market_cap") includeMarketCap: Boolean = true,
        @Query("include_24hr_vol") include24hrVol: Boolean = true
    ): Call<BasicPriceDataResponse>

    @GET("coins/{id}/market_chart")
    fun get1DayChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 1
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun get7DayChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 7
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun get30DayChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 30
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun get90DayChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 90
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun get6MonthChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 180
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun get1YearChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: Int = 365
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart")
    fun getMaxChartData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currencyCode: String,
        @Query("days") days: String = "max"
    ): Call<ChartDataResponse>

    @GET("coins/{id}/market_chart/range")
    fun getHistoryData(
        @Path("id") id: String = "bitcoin",
        @Query("vs_currency") currency: String,
        @Query("from") from: Long,
        @Query("to") to: Long
    ): Call<MarketHistoryDataResponse>
}