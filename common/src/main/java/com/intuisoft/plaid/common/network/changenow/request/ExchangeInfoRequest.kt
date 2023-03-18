package com.intuisoft.plaid.common.network.changenow.request

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExchangeInfoRequest(
    @SerializedName("fromCurrency")
    val fromCurrency: String,

    @SerializedName("fromNetwork")
    val fromNetwork: String,

    @SerializedName("toCurrency")
    val toCurrency: String,

    @SerializedName("toNetwork")
    val toNetwork: String,

    @SerializedName("fromAmount")
    val fromAmount: Double,

    @SerializedName("address")
    val address: String,

    @SerializedName("extraId")
    val extraId: String,

    @SerializedName("refundAddress")
    val refundAddress: String,

    @SerializedName("refundExtraId")
    val refundExtraId: String,

    @SerializedName("contactEmail")
    val contactEmail: String,

    @SerializedName("flow")
    val flow: String,

    @SerializedName("type")
    val type: String = "direct"
): Parcelable
