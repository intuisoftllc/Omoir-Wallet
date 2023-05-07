package com.intuisoft.plaid.common.model

data class TransactionModel(
    val id: String?,
    val hash: String,
    val inputs: List<TransactionIOModel>,
    val outputs: List<TransactionIOModel>,
    val amount: Double,
    val fee: Double,
    val time: Long,
    val type: TransactionType,
)