package com.intuisoft.plaid.common.network.blockchair.repository

import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.api.ChangeNowApi
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.network.changenow.request.ExchangeInfoRequest
import com.intuisoft.plaid.common.network.changenow.response.EstimatedReceiveAmountResponse
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import java.time.Instant

interface ChangeNowRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getAllSupportedCurrencies(): Result<List<SupportedCurrencyModel>>

    fun getRangeFor(from: SupportedCurrencyModel, to: SupportedCurrencyModel): Result<CurrencyRangeLimitModel>

    fun getExchange(id: String): Result<ExchangeInfoDataModel>

    fun isAddressValid(currency: SupportedCurrencyModel, address: String): Result<Pair<Boolean, String?>>

    fun getEstimatedReceiveAmount(from: SupportedCurrencyModel, to: SupportedCurrencyModel, amount: Double): Result<EstimatedReceiveAmountModel>

    fun createExchange(
        from: SupportedCurrencyModel,
        to: SupportedCurrencyModel,
        rateId: String?,
        receiveAddress: String,
        receiveAddressMemo: String,
        refundAddress: String,
        refundAddressMemo: String,
        amount: Double
    ): Result<ExchangeInfoDataModel>

    private class Impl(
        private val api: ChangeNowApi,
        private val localStoreRepository: LocalStoreRepository
    ) : ChangeNowRepository {

        override fun getAllSupportedCurrencies(): Result<List<SupportedCurrencyModel>> {
            try {
                val currencies = api.getAllSupportedCurrencies().execute().body()

                return Result.success(currencies!!.filter { !it.isFiat }.map {
                    SupportedCurrencyModel(
                        ticker = it.ticker,
                        name = it.name,
                        id = "",
                        image = it.image,
                        network = it.network,
                        needsMemo = it.hasExternalId
                    )
                })
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun isAddressValid(currency: SupportedCurrencyModel, address: String): Result<Pair<Boolean, String?>> {
            try {
                val result = api.validateAddress(currency.network, address).execute().body()
                return Result.success(result!!.result to result!!.message)
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getExchange(id: String): Result<ExchangeInfoDataModel> {
            try {
                val result = api.getExchangeInfo(id).execute().body()

                return Result.success(
                    ExchangeInfoDataModel(
                        id = result!!.id,
                        type = Constants.Strings.DEFAULT_EXCHANGE_FLOW,
                        timestamp = Instant.parse(result.createdAt),
                        lastUpdated = Instant.parse(result.updatedAt),
                        from = getFullName(result.fromCurrency),
                        fromId = getId(result.fromCurrency, result.fromNetwork),
                        to = getFullName(result.toCurrency),
                        toId = getId(result.toCurrency, result.toNetwork),
                        fromShort = result.fromCurrency.uppercase(),
                        toShort = result.toCurrency.uppercase(),
                        sendAmount = result.amountFrom ?: 0.0,
                        receiveAmount = result.amountTo ?: 0.0,
                        expectedSendAmount = result.expectedAmountFrom ?: 0.0,
                        expectedReceiveAmount = result.expectedAmountTo ?: 0.0,
                        paymentAddress = result.payinAddress,
                        paymentAddressMemo = result.payinExtraId,
                        receiveAddressMemo = result.payoutExtraId,
                        refundAddress = result.refundAddress ?: "",
                        refundAddressMemo = result.refundExtraId,
                        paymentTxId = result.payinHash,
                        receiveTxId = result.payoutHash,
                        status = result.status
                    )
                )
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun createExchange(
            from: SupportedCurrencyModel,
            to: SupportedCurrencyModel,
            rateId: String?,
            receiveAddress: String,
            receiveAddressMemo: String,
            refundAddress: String,
            refundAddressMemo: String,
            amount: Double
        ): Result<ExchangeInfoDataModel> {
            try {
                val result = api.createExchange(
                          ExchangeInfoRequest(
                              fromCurrency = from.ticker,
                              fromNetwork = from.network,
                              toCurrency = to.ticker,
                              toNetwork = to.network,
                              fromAmount = amount,
                              address = receiveAddress,
                              extraId = receiveAddressMemo,
                              refundAddress = refundAddress,
                              refundExtraId = refundAddressMemo,
                              contactEmail = Constants.Strings.SUPPORT_EMAIL,
                              flow = Constants.Strings.DEFAULT_EXCHANGE_FLOW,
                          )
                      ).execute().body()

                return getExchange(result!!.id)
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getRangeFor(from: SupportedCurrencyModel, to: SupportedCurrencyModel): Result<CurrencyRangeLimitModel> {
            try {
                val range = api.getRange(
                    fromCurrency = from.ticker,
                    toCurrency = to.ticker,
                    fromNetwork = from.network,
                    toNetwork = to.network
                ).execute()

                return Result.success(
                    CurrencyRangeLimitModel(
                        min = range.body()!!.minAmount,
                        max = range.body()!!.maxAmount
                    )
                )
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getEstimatedReceiveAmount(from: SupportedCurrencyModel, to: SupportedCurrencyModel, amount: Double): Result<EstimatedReceiveAmountModel> {
            try {
                val estimated = api.getEstimatedReceiveAmount(
                    fromCurrency = from.ticker,
                    toCurrency = to.ticker,
                    fromAmount = amount,
                    fromNetwork = from.network,
                    toNetwork = to.network
                ).execute().body()
                return Result.success(
                    EstimatedReceiveAmountModel(
                        rateId = estimated!!.rateId ?: "",
                        validUntil = if(estimated!!.validUntil?.isNotBlank() == true) Instant.parse(estimated!!.validUntil) else null,
                        toAmount = estimated!!.toAmount,
                    )
                )
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        private fun getNetwork(ticker: String) : String {
            if(ticker.lowercase() == Constants.Strings.BTC_TICKER) {
                return Constants.Strings.BTC_TICKER
            } else {
                val currencies = localStoreRepository.getSupportedCurrenciesData()
                return currencies.first { it.ticker == ticker }.network
            }
        }

        private fun getFullName(ticker: String) : String {
            val currencies = localStoreRepository.getSupportedCurrenciesData()
            return currencies.first { it.ticker == ticker }.name
        }

        private fun getId(ticker: String, network: String) : String {
            val currencies = localStoreRepository.getSupportedCurrenciesData()
            return currencies.first { it.ticker == ticker && it.network == network }.id
        }
    }

    companion object {
        @JvmStatic
        fun create(
            api: ChangeNowApi,
            localStoreRepository: LocalStoreRepository
        ): ChangeNowRepository = Impl(api, localStoreRepository)
    }
}