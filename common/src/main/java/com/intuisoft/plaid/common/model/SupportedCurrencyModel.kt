package com.intuisoft.plaid.common.network.blockchair.response

data class SupportedCurrencyModel(
    val ticker: String,
    val name: String,
    val image: String,
    val validAddressRegex: String,
    val validMemoRegex: String?
)
