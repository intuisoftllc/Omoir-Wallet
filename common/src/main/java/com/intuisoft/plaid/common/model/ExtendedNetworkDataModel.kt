package com.intuisoft.plaid.common.model

data class ExtendedNetworkDataModel(
    var height: Int,
    var difficulty: Long,
    var blockchainSize: Long,
    var avgTxSize: Int,
    var avgFeeRate: Int,
    var unconfirmedTxs: Int,
    var avgConfTime: Double
)