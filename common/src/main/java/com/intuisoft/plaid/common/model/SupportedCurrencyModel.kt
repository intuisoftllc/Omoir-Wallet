package com.intuisoft.plaid.common.network.blockchair.response

data class SupportedCurrencyModel(
    val ticker: String,
    val id: String,
    val name: String,
    val image: String,
    val network: String,
    val needsMemo: Boolean
)
