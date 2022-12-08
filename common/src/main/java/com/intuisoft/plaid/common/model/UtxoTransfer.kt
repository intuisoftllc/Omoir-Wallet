package com.intuisoft.plaid.common.model

data class UtxoTransfer(
    var txId: String,
    val address: String,
    var feeRate: Int,
)