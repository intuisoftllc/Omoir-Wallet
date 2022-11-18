package com.intuisoft.plaid.common.network.nownodes.repository

import com.intuisoft.plaid.common.model.BasicPriceDataModel
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.model.CurrencyRangeLimitModel
import com.intuisoft.plaid.common.network.nownodes.api.SimpleSwapApi
import com.intuisoft.plaid.common.network.nownodes.response.ChartDataResponse
import com.intuisoft.plaid.common.network.nownodes.response.CurrencyRangeLimitResponse
import com.intuisoft.plaid.common.network.nownodes.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.network.nownodes.response.SupportedCurrencyResponse
import com.intuisoft.plaid.common.util.Constants

interface SimpleSwapRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getAllSupportedCurrencies(): Result<List<SupportedCurrencyModel>>

    fun getAllPairs(fixed: Boolean): Result<List<String>>

    fun getRangeFor(fixed: Boolean, from: String, to: String): Result<CurrencyRangeLimitModel>

    fun getEstimatedReceiveAmount(fixed: Boolean, from: String, to: String, amount: Double): Result<Double>

    private class Impl(
        private val api: SimpleSwapApi,
        private val apiKey: String
    ) : SimpleSwapRepository {

        override fun getAllSupportedCurrencies(): Result<List<SupportedCurrencyModel>> {
            try {
                val currencies = api.getAllSupportedCurrencies(apiKey).execute().body()
                return Result.success(currencies!!.map { SupportedCurrencyModel(it.symbol, it.name, it.image) })
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getAllPairs(fixed: Boolean): Result<List<String>> {
            try {
                val pairs = api.getPairs(apiKey, fixed, "btc").execute().body()
                return Result.success(pairs!!)
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getRangeFor(fixed: Boolean, from: String, to: String): Result<CurrencyRangeLimitModel> {
            try {
                val range = api.getRanges(apiKey, fixed, from, to).execute().body()
                return Result.success(
                    CurrencyRangeLimitModel(
                        min = range!!.min,
                        max = range!!.max
                    )
                )
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getEstimatedReceiveAmount(fixed: Boolean, from: String, to: String, amount: Double): Result<Double> {
            try {
                val estimated = api.getEstimatedReceiveAmount(apiKey, fixed, from, to, amount).execute().body()
                return Result.success(estimated!!.toDouble())
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }
    }

    companion object {
        @JvmStatic
        fun create(
            api: SimpleSwapApi,
            apiKey: String
        ): SimpleSwapRepository = Impl(api, apiKey)
    }
}