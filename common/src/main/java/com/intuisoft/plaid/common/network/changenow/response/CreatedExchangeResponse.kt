package com.intuisoft.plaid.common.network.changenow.response

data class CreatedExchangeResponse(
    val id: String,
    val fromAmount: Double,
    val toAmount: Double,
    val flow: String,
    val payinAddress: String,
    val payoutAddress: String,
    val payoutExtraId: String?,
    val fromCurrency: String,
    val toCurrency: String,
    val refundAddress: String?,
    val refundExtraId: String?,
    val fromNetwork: String,
    val toNetwork: String
)
