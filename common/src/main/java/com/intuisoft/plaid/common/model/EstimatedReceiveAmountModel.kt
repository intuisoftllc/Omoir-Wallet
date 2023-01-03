package com.intuisoft.plaid.common.model

import java.time.Instant

data class EstimatedReceiveAmountModel(
    val rateId: String,
    val validUntil: Instant?,
    val toAmount: Double
)