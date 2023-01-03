package com.intuisoft.plaid.common.network.changenow.request

data class ExchangeInfoRequest(
    val fromCurrency: String,
    val fromNetwork: String,
    val toCurrency: String,
    val toNetwork: String,
    val fromAmount: Double,
    val address: String,
    val extraId: String,
    val refundAddress: String,
    val refundExtraId: String,
    val contactEmail: String,
    val flow: String,
    val type: String = "direct",
)
