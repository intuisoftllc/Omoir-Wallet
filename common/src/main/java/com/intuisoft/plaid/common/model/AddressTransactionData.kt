package com.intuisoft.plaid.common.model

data class AddressTransactionData(
    val outputs: List<TxOutput>,
    val status: TxStatus
)

data class TxOutput(
    val script: String,
    val address: String?,
)

data class TxStatus(
    val height: Int?,
    val blockHash: String?,
)