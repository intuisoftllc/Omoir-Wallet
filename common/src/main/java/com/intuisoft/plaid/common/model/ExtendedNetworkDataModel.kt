package com.intuisoft.plaid.common.model

data class ExtendedNetworkDataModel(
    var height: Int,
    var difficulty: Long,
    var blockchainSize: Long,
    var nodesOnNetwork: Int,
    var memPoolSize: Long,
    var txPerSecond: Int,
    var unconfirmedTxs: Int,
    var addressesWithBalance: Long,
    var avgConfTime: Double
)