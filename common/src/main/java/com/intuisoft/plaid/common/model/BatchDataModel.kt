package com.intuisoft.plaid.common.model

data class BatchDataModel(
    var id: String,
    var transferId: String,
    var batchNumber: Int,
    val utxos: List<UtxoTransfer>,
    val status: AssetTransferStatus
)
