package com.intuisoft.plaid.common.model

data class BasicTickerDataModel(
    var price: Double,
    var marketCap: Double,
    var volume24Hr: Double,
    var circulatingSupply: Long,
    var memPoolTxCount: Int,
    val maxSupply: Int = 21000000
)