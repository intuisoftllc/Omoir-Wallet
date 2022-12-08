package com.intuisoft.plaid.common.model

data class AssetTransferModel(
    val id: String,
    val walletId: String,
    val recipientWallet: String,
    val createdAt: Long,
    val batchGap: Int,
    val batchSize: Int,
    val expectedAmount: Long,
    var sent: Long,
    var feesPaid: Long,
    var feeRangeLow: Int,
    var feeRangeHigh: Int,
    var dynamicFees: Boolean,
    var status: AssetTransferStatus,
    val batches: List<String>
)
