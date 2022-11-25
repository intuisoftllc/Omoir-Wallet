package com.intuisoft.plaid.common.model

import java.time.Instant

data class ExchangeInfoDataModel(
    val id: String,
    val type: String,
    val timestamp: Instant,
    val lastUpdated: Instant,
    val from: String,
    val to: String,
    val sendAmount: Double,
    val receiveAmount: Double,
    val paymentAddress: String,
    val paymentAddressMemo: String?,
    val receiveAddressMemo: String?,
    val refundAddress: String,
    val refundAddressMemo: String,
    val paymentTxId: String,
    val receiveTxId: String,
    val status: String
)
