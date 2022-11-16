package com.intuisoft.plaid.common.network.nownodes.repository

import com.intuisoft.plaid.common.model.BasicPriceDataModel
import com.intuisoft.plaid.common.network.nownodes.api.CoingeckoApi
import com.intuisoft.plaid.common.util.Constants

interface CoingeckoRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getBasicPriceData(): Result<List<BasicPriceDataModel>>

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
    }

    companion object {
        @JvmStatic
        fun create(
            api: CoingeckoApi,
        ): CoingeckoRepository = Impl(api)
    }
}