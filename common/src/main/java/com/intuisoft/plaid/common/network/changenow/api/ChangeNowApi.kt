package com.intuisoft.plaid.common.network.blockchair.api

import com.intuisoft.plaid.common.network.blockchair.response.CurrencyRangeLimitResponse
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyResponse
import com.intuisoft.plaid.common.network.changenow.request.ExchangeInfoRequest
import com.intuisoft.plaid.common.network.changenow.response.AddressValidationResponse
import com.intuisoft.plaid.common.network.changenow.response.EstimatedReceiveAmountResponse
import com.intuisoft.plaid.common.network.changenow.response.CreatedExchangeResponse
import com.intuisoft.plaid.common.network.changenow.response.ExchangeStatusResponse
import com.intuisoft.plaid.common.util.Constants
import retrofit2.Call
import retrofit2.http.*

interface ChangeNowApi {

    @GET("exchange/currencies")
    fun getAllSupportedCurrencies(
        @Query("flow") flow: String = Constants.Strings.DEFAULT_EXCHANGE_FLOW,
        @Query("active") active: Boolean = true
    ): Call<List<SupportedCurrencyResponse>>

    @GET("exchange/by-id")
    fun getExchangeInfo(
        @Query("id") id: String
    ): Call<ExchangeStatusResponse>

    @GET("validate/address")
    fun validateAddress(
        @Query("currency") ticker: String,
        @Query("address") address: String
    ): Call<AddressValidationResponse>

    @POST("exchange")
    fun createExchange(
        @Body exchangeInfo: ExchangeInfoRequest
    ): Call<CreatedExchangeResponse>

    @GET("exchange/range")
    fun getRange(
        @Query("fromCurrency") fromCurrency: String,
        @Query("toCurrency") toCurrency: String,
        @Query("fromNetwork") fromNetwork: String,
        @Query("toNetwork") toNetwork: String,
        @Query("flow") flow: String = Constants.Strings.DEFAULT_EXCHANGE_FLOW,
    ): Call<CurrencyRangeLimitResponse>

    @GET("exchange/estimated-amount")
    fun getEstimatedReceiveAmount(
        @Query("fromCurrency") fromCurrency: String,
        @Query("toCurrency") toCurrency: String,
        @Query("fromAmount") fromAmount: Double,
        @Query("fromNetwork") fromNetwork: String,
        @Query("toNetwork") toNetwork: String,
        @Query("flow") flow: String = Constants.Strings.DEFAULT_EXCHANGE_FLOW,
    ): Call<EstimatedReceiveAmountResponse>
}