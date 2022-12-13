package com.intuisoft.plaid.common.model

data class BatchDataModel(
    var id: String,
    var transferId: String,
    var batchNumber: Int,
    var completionHeight: Int,
    var blocksRemaining: Int,
    val utxos: List<UtxoTransfer>,
    var status: AssetTransferStatus
)
