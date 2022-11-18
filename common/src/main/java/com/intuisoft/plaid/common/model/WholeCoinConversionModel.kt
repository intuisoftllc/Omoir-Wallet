package com.intuisoft.plaid.common.model

data class WholeCoinConversionModel(
    val from: String,
    val to: String,
    val conversion: Double
)