package com.intuisoft.plaid.common.network.simpleswap.request

data class ExchangeInfoRequest(
    val currency_from: String,
    val currency_to: String,
    val fixed: Boolean,
    val amount: Double,
    val address_to: String,
    val extraIdTo: String,
    val userRefundAddress: String,
    val userRefundExtraId: String,
)
