package com.intuisoft.plaid.common.model

data class UtxoTransfer(
    val txId: String,
    val address: String,
    val feeRate: Int
)