package com.intuisoft.plaid.common.network.nownodes.response

data class SupportedCurrencyModel(
    val ticker: String,
    val name: String,
    val image: String,
    val validAddressRegex: String,
    val validMemoRegex: String?
)
