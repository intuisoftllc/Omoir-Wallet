package com.intuisoft.plaid.common.network.blockchair.repository

import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.coingecko.api.CoingeckoApi
import com.intuisoft.plaid.common.network.coingecko.response.ChartDataResponse
import com.intuisoft.plaid.common.network.coingecko.response.PriceDataResponse

interface CoingeckoRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getBasicInfoData(coin: String): Result<CoinInfoDataModel>

    fun getChartData(interval: ChartIntervalType, currencyCode: String): Result<List<ChartDataModel>>

    fun getHistoryData(currencyCode: String, from: Long, to: Long): Result<List<MarketHistoryDataModel>>

    private class Impl(
        private val api: CoingeckoApi,
        private val apiKey: String?
    ) : CoingeckoRepository {

        override fun getHistoryData(currencyCode: String, from: Long, to: Long): Result<List<MarketHistoryDataModel>> {
            try {
                val history = api.getHistoryData(currency = currencyCode.lowercase(), from = from, to = to, apiKey = apiKey).execute().body()

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

        override fun getBasicInfoData(coin: String): Result<CoinInfoDataModel> {
            try {
                val response = api.getBasicPriceData(
                    ticker = coin,
                    apiKey = apiKey
                ).execute().body()

                return Result.success(
                    CoinInfoDataModel(
                        id = response!!.id,
                        marketData = CoinMarketData(
                            currentPrice = consumePriceDataResponse(response.marketData.currentPrice),
                            marketCap = consumePriceDataResponse(response.marketData.marketCap),
                            totalVolume = consumePriceDataResponse(response.marketData.totalVolume),
                            maxSupply = response.marketData.maxSupply,
                            circulatingSupply = response.marketData.circulatingSupply
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
                        data = api.get1DayChartData(currencyCode = currencyCode, apiKey = apiKey).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_1WEEK -> {
                        data = api.get7DayChartData(currencyCode = currencyCode, apiKey = apiKey).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_1MONTH -> {
                        data = api.get30DayChartData(currencyCode = currencyCode, apiKey = apiKey).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_3MONTHS -> {
                        data = api.get90DayChartData(currencyCode = currencyCode, apiKey = apiKey).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_6MONTHS -> {
                        data = api.get6MonthChartData(currencyCode = currencyCode, apiKey = apiKey).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_1YEAR -> {
                        data = api.get1YearChartData(currencyCode = currencyCode, apiKey = apiKey).execute().body()!!
                    }
                    ChartIntervalType.INTERVAL_ALL_TIME -> {
                        data = api.getMaxChartData(currencyCode = currencyCode, apiKey = apiKey).execute().body()!!
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

        private fun consumePriceDataResponse(response: PriceDataResponse): PriceData {
            return PriceData(
                usd = response.usd,
                cad = response.cad,
                eur = response.eur,
                ars = response.ars,
                aud = response.aud,
                bdt = response.bdt,
                bhd = response.bhd,
                chf = response.chf,
                cny = response.cny,
                czk = response.czk,
                gbp = response.gbp,
                krw = response.krw,
                rub = response.rub,
                php = response.php,
                pkr = response.pkr,
                clp = response.clp,
                aed = response.aed
            )
        }
    }

    companion object {
        @JvmStatic
        fun create(
            api: CoingeckoApi,
            apiKey: String?
        ): CoingeckoRepository = Impl(api, apiKey)
    }
}