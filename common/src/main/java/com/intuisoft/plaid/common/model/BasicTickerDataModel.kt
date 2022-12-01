package com.intuisoft.plaid.common.model

data class BasicTickerDataModel(
    var price: Double,
    var marketCap: Double,
    var volume24Hr: Double,
    var circulatingSupply: Long,
    val maxSupply: Int = 21000000
)