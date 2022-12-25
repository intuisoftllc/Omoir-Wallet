package com.intuisoft.plaid.common.network.blockchair.repository

import android.util.Log
import com.intuisoft.plaid.common.model.BasicPriceDataModel
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.model.MarketHistoryDataModel
import com.intuisoft.plaid.common.network.blockchair.api.CoingeckoApi
import com.intuisoft.plaid.common.network.blockchair.response.ChartDataResponse
import com.intuisoft.plaid.common.util.Constants

interface CoingeckoRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getBasicPriceData(): Result<List<BasicPriceDataModel>>

    fun getChartData(interval: ChartIntervalType, currencyCode: String): Result<List<ChartDataModel>>

    fun getHistoryData(currencyCode: String, from: Long, to: Long): Result<List<MarketHistoryDataModel>>

    private class Impl(
        private val api: CoingeckoApi,
    ) : CoingeckoRepository {

        override fun getHistoryData(currencyCode: String, from: Long, to: Long): Result<List<MarketHistoryDataModel>> {
            try {
                val history = api.getHistoryData(currency = currencyCode.lowercase(), from = from, to = to).execute().body()

                return Result.success(
                    history!!.prices.map {
                        MarketHistoryDataModel(
                            time = it[0].toLong(),
                            price = it[1]
                        )
                    }
                )
            } catch (t: Throwable) {
                t.printStackTrace()
                return Result.failure(t)
            }
        }

        override fun getBasicPriceData(): Result<List<BasicPriceDataModel>> {
            try {
                val prices = api.getBasicPriceData().execute().body()

                return Result.success(
                    listOf(
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.usd_market_cap,
                            volume24Hr = prices!!.bitcoin.usd_24h_vol,
                            currentPrice = prices!!.bitcoin.usd,
                            currencyCode = Constants.LocalCurrency.USD
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.eur_market_cap,
                            volume24Hr = prices!!.bitcoin.eur_24h_vol,
                            currentPrice = prices!!.bitcoin.eur,
                            currencyCode = Constants.LocalCurrency.EURO
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.cad_market_cap,
                            volume24Hr = prices!!.bitcoin.cad_24h_vol,
                            currentPrice = prices!!.bitcoin.cad,
                            currencyCode = Constants.LocalCurrency.CANADA
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.aed_market_cap,
                            volume24Hr = prices!!.bitcoin.aed_24h_vol,
                            currentPrice = prices!!.bitcoin.aed,
                            currencyCode = Constants.LocalCurrency.AED
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.ars_market_cap,
                            volume24Hr = prices!!.bitcoin.ars_24h_vol,
                            currentPrice = prices!!.bitcoin.ars,
                            currencyCode = Constants.LocalCurrency.ARS
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.aud_market_cap,
                            volume24Hr = prices!!.bitcoin.aud_24h_vol,
                            currentPrice = prices!!.bitcoin.aud,
                            currencyCode = Constants.LocalCurrency.AUD
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.bdt_market_cap,
                            volume24Hr = prices!!.bitcoin.bdt_24h_vol,
                            currentPrice = prices!!.bitcoin.bdt,
                            currencyCode = Constants.LocalCurrency.BDT
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.bhd_market_cap,
                            volume24Hr = prices!!.bitcoin.bhd_24h_vol,
                            currentPrice = prices!!.bitcoin.bhd,
                            currencyCode = Constants.LocalCurrency.BHD
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.chf_market_cap,
                            volume24Hr = prices!!.bitcoin.chf_24h_vol,
                            currentPrice = prices!!.bitcoin.chf,
                            currencyCode = Constants.LocalCurrency.CHF
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.cny_market_cap,
                            volume24Hr = prices!!.bitcoin.cny_24h_vol,
                            currentPrice = prices!!.bitcoin.cny,
                            currencyCode = Constants.LocalCurrency.CNY
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.czk_market_cap,
                            volume24Hr = prices!!.bitcoin.czk_24h_vol,
                            currentPrice = prices!!.bitcoin.czk,
                            currencyCode = Constants.LocalCurrency.CZK
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.gbp_market_cap,
                            volume24Hr = prices!!.bitcoin.gbp_24h_vol,
                            currentPrice = prices!!.bitcoin.gbp,
                            currencyCode = Constants.LocalCurrency.GBP
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.krw_market_cap,
                            volume24Hr = prices!!.bitcoin.krw_24h_vol,
                            currentPrice = prices!!.bitcoin.krw,
                            currencyCode = Constants.LocalCurrency.KRW
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.rub_market_cap,
                            volume24Hr = prices!!.bitcoin.rub_24h_vol,
                            currentPrice = prices!!.bitcoin.rub,
                            currencyCode = Constants.LocalCurrency.RUB
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.php_market_cap,
                            volume24Hr = prices!!.bitcoin.php_24h_vol,
                            currentPrice = prices!!.bitcoin.php,
                            currencyCode = Constants.LocalCurrency.PHP
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.pkr_market_cap,
                            volume24Hr = prices!!.bitcoin.pkr_24h_vol,
                            currentPrice = prices!!.bitcoin.pkr,
                            currencyCode = Constants.LocalCurrency.PKR
                        ),
                        BasicPriceDataModel(
                            marketCap = prices!!.bitcoin.clp_market_cap,
                            volume24Hr = prices!!.bitcoin.clp_24h_vol,
                            currentPrice = prices!!.bitcoin.clp,
                            currencyCode = Constants.LocalCurrency.CLP
                        )
                    )
                )
            } catch (t: Throwable) {
                t.printStackTrace()
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
                t.printStackTrace()
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