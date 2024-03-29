package com.intuisoft.plaid.common.network.blockchair.api

import com.intuisoft.plaid.common.network.coingecko.response.BasicPriceDataResponse
import com.intuisoft.plaid.common.network.coingecko.response.ChartDataResponse
import com.intuisoft.plaid.common.network.coingecko.response.MarketHistoryDataResponse
import retrofit2.Call
import retrofit2.http.*

interface CoingeckoApi {

    @GET("simple/price")
    fun getBasicPriceData(
        @Query("ids") id: String = "bitcoin",
        @Query("vs_currencies") currencies: String = "usd,eur,cad,aed,ars,aud,bdt,bhd,chf,cny,czk,gbp,krw,rub,php,pkr,clp",
        @Query("include_market_cap") includeMarketCap: Boolean = true,
        @Query("include_24hr_vol") include24hrVol: Boolean = true,
        @Query("x_cg_pro_api_key") apiKey: String? = null
    ): Call<BasicPriceDataResponse>

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