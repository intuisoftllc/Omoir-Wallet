package com.intuisoft.plaid.common.network.changenow.response

import com.google.gson.annotations.SerializedName

data class EstimatedReceiveAmountResponse(

    @SerializedName("rateId")
    val rateId: String?,

    @SerializedName("validUntil")
    val validUntil: String?,

    @SerializedName("toAmount")
    val toAmount: Double
)
