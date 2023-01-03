package com.intuisoft.plaid.common.network.changenow.response

data class ExchangeStatusResponse(
    val id: String,
    val status: String,
    val fromCurrency: String,
    val fromNetwork: String,
    val toCurrency: String,
    val toNetwork: String,
    val expectedAmountFrom: Double?,
    val expectedAmountTo: Double?,
    val amountFrom: Double?,
    val amountTo: Double?,
    val payinAddress: String,
    val payoutAddress: String,
    val payinExtraId: String?,
    val payoutExtraId: String?,
    val refundAddress: String?,
    val refundExtraId: String?,
    val createdAt: String,
    val updatedAt: String,
    val payinHash: String?,
    val payoutHash: String?
)
