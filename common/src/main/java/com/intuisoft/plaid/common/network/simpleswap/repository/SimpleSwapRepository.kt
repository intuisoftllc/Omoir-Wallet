package com.intuisoft.plaid.common.network.nownodes.repository

import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.nownodes.api.SimpleSwapApi
import com.intuisoft.plaid.common.network.nownodes.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.network.simpleswap.request.ExchangeInfoRequest

interface SimpleSwapRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getAllSupportedCurrencies(): Result<List<SupportedCurrencyModel>>

    fun getAllPairs(fixed: Boolean): Result<List<String>>

    fun getRangeFor(fixed: Boolean, from: String, to: String): Result<CurrencyRangeLimitModel>

    fun getEstimatedReceiveAmount(fixed: Boolean, from: String, to: String, amount: Double): Result<Double>

    fun createExchange(
        fixed: Boolean, from: String, to: String, receiveAddress: String,
        receiveAddressMemo: String, refundAddress: String, refundAddressMemo: String,
        amount: Double
    ): Result<ExchangeInfoDataModel>

    private class Impl(
        private val api: SimpleSwapApi,
        private val apiKey: String
    ) : SimpleSwapRepository {

        override fun getAllSupportedCurrencies(): Result<List<SupportedCurrencyModel>> {
            try {
                val currencies = api.getAllSupportedCurrencies(apiKey).execute().body()

                return Result.success(currencies!!.filter { it.validation_address != null }.map {
                    SupportedCurrencyModel(it.symbol, it.name, it.image, it.validation_address!!, it.validation_extra)
                })
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

        override fun createExchange(
            fixed: Boolean,
            from: String,
            to: String,
            receiveAddress: String,
            receiveAddressMemo: String,
            refundAddress: String,
            refundAddressMemo: String,
            amount: Double
        ): Result<ExchangeInfoDataModel> {
            try {
                val result = api.createExchange(
                                apiKey,
                                ExchangeInfoRequest(
                                    currency_from = from,
                                    currency_to = to,
                                    fixed = fixed,
                                    amount = amount,
                                    address_to = receiveAddress,
                                    extraIdTo = receiveAddressMemo,
                                    userRefundAddress = refundAddress,
                                    userRefundExtraId = refundAddressMemo,
                                )
                            ).execute().body()

                return Result.success(
                    ExchangeInfoDataModel(
                        id = result!!.id,
                        type = result.type,
                        timestamp = result.timestamp,
                        lastUpdated = result.updated_at,
                        from = result.currency_from,
                        to = result.currency_to,
                        sendAmount = result.amount_from,
                        receiveAmount = result.amount_to,
                        paymentAddress = result.address_from,
                        paymentAddressMemo = result.extra_id_from,
                        receiveAddressMemo = result.extra_id_to,
                        refundAddress = result.user_refund_address,
                        refundAddressMemo = result.user_refund_extra_id,
                        paymentTxId = result.tx_from,
                        receiveTxId = result.tx_to,
                        status = result.status
                    )
                )
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