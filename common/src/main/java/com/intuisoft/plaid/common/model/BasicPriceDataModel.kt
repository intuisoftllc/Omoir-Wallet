package com.intuisoft.plaid.common.model

data class BasicPriceDataModel(
    var marketCap: Double,
    var currentPrice: Double,
    var volume24Hr: Double,
    var currencyCode: String
)