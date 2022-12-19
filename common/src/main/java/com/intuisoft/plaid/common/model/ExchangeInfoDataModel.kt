package com.intuisoft.plaid.common.model

import java.time.Instant

data class ExchangeInfoDataModel(
    val id: String,
    val type: String,
    val timestamp: Instant,
    val lastUpdated: Instant,
    var from: String,
    val fromShort: String,
    var to: String,
    val toShort: String,
    val sendAmount: Double,
    val receiveAmount: Double,
    val paymentAddress: String,
    val paymentAddressMemo: String?,
    val receiveAddressMemo: String?,
    val refundAddress: String,
    val refundAddressMemo: String?,
    val paymentTxId: String?,
    val receiveTxId: String?,
    val status: String
)
