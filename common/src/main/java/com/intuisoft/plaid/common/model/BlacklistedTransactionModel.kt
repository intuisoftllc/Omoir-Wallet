package com.intuisoft.plaid.common.model

data class BlacklistedTransactionModel(
    var txId: String,
    var walletId: String
)