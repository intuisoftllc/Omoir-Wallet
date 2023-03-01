package com.intuisoft.plaid.common.network.changenow.response

import com.google.gson.annotations.SerializedName

data class AddressValidationResponse(

    @SerializedName("result")
    val result: Boolean,

    @SerializedName("message")
    val message: String?,
)
