package com.intuisoft.plaid.common.model

data class BasicNetworkDataModel(
    var circulatingSupply: Long,
    var memPoolTxCount: Int,
    val maxSupply: Int = 21000000
)