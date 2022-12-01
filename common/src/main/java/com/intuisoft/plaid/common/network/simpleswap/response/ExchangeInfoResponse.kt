package com.intuisoft.plaid.common.network.simpleswap.response

import java.time.Instant

data class ExchangeInfoResponse(
    val id: String,
    val type: String,
    val timestamp: String,
    val updated_at: String,
    val currency_from: String,
    val currency_to: String,
    val amount_from: Double,
    val expected_amount: Double,
    val amount_to: Double,
    val address_from: String,
    val extra_id_from: String?,
    val extra_id_to: String?,
    val user_refund_address: String,
    val user_refund_extra_id: String,
    val tx_from: String?,
    val tx_to: String?,
    val status: String
)
