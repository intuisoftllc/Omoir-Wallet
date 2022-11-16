package com.intuisoft.plaid.common.network.nownodes.repository

import com.intuisoft.plaid.common.model.BasicPriceDataModel
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.network.nownodes.api.CoingeckoApi
import com.intuisoft.plaid.common.network.nownodes.response.ChartDataResponse
import com.intuisoft.plaid.common.util.Constants

interface CoingeckoRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getBasicPriceData(): Result<List<BasicPriceDataModel>>

    fun getChartData(interval: ChartIntervalType, currencyCode: String): Result<List<ChartDataModel>>

    private class Impl(
        private val api: CoingeckoApi,
    ) : CoingeckoRepository {

        override fun getBasicPriceData(): Result<List<BasicPriceDataModel>> {
            try {
                val prices = api.getBasicPriceData().execute().body()

                return Result.success(
                    listOf(
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.usd_market_cap,
                            currentPrice = prices!!.bitcoin.usd,
                            currencyCode = Constants.LocalCurrency.USD
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.eur_market_cap,
                            currentPrice = prices!!.bitcoin.eur,
                            currencyCode = Constants.LocalCurrency.EURO
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.cad_market_cap,
                            currentPrice = prices!!.bitcoin.cad,
                            currencyCode = Constants.LocalCurrency.CANADA
                        )
                    )
                )
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getChartData(interval: ChartIntervalType, currencyCode: String): Result<List<ChartDataModel>> {
            try {
                var data: ChartDataResponse? = null

                when(interval) {
                    ChartIntervalType.INTERVAL_1DAY -> {
                        data = api.get1DayChartData(currencyCode = currencyCode).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_1WEEK -> {
                        data = api.get7DayChartData(currencyCode = currencyCode).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_1MONTH -> {
                        data = api.get30DayChartData(currencyCode = currencyCode).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_3MONTHS -> {
                        data = api.get90DayChartData(currencyCode = currencyCode).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_6MONTHS -> {
                        data = api.get6MonthChartData(currencyCode = currencyCode).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_1YEAR -> {
                        data = api.get1YearChartData(currencyCode = currencyCode).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_ALL_TIME -> {
                        data = api.getMaxChartData(currencyCode = currencyCode).execute().body()!!
                    }
                }

                return Result.success(
                    data!!.prices.map { ChartDataModel(it[0].toLong(), it[1].toFloat()) }
                )
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }
    }

    companion object {
        @JvmStatic
        fun create(
            api: CoingeckoApi,
        ): CoingeckoRepository = Impl(api)
    }
}