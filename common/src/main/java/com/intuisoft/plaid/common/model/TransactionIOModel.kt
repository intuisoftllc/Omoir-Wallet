package com.intuisoft.plaid.common.model

data class TransactionIOModel(
    val mine: Boolean,
    val value: Double,
    val address: String?
)