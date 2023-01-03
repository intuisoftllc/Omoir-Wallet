package com.intuisoft.plaid.common.network.changenow.response

data class EstimatedReceiveAmountResponse(
    val rateId: String?,
    val validUntil: String?,
    val toAmount: Double
)
