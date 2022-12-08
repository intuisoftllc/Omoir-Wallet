package com.intuisoft.plaid.common.model

data class AssetTransferModel(
    var id: String,
    var walletId: String,
    var recipientWallet: String,
    var createdAt: Long,
    val batchGap: Int,
    val batchSize: Int,
    val expectedAmount: Long,
    val sent: Long,
    val feesPaid: Long,
    val status: AssetTransferStatus,
    val batches: List<String>
)
