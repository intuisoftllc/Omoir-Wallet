package com.intuisoft.plaid.common.network.changenow.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CreatedExchangeResponse(

    @SerializedName("id")
    val id: String,

    @SerializedName("fromAmount")
    val fromAmount: Double,

    @SerializedName("toAmount")
    val toAmount: Double,

    @SerializedName("flow")
    val flow: String,

    @SerializedName("payinAddress")
    val payinAddress: String,

    @SerializedName("payoutAddress")
    val payoutAddress: String,

    @SerializedName("payoutExtraId")
    val payoutExtraId: String?,

    @SerializedName("fromCurrency")
    val fromCurrency: String,

    @SerializedName("toCurrency")
    val toCurrency: String,

    @SerializedName("refundAddress")
    val refundAddress: String?,

    @SerializedName("refundExtraId")
    val refundExtraId: String?,

    @SerializedName("fromNetwork")
    val fromNetwork: String,

    @SerializedName("toNetwork")
    val toNetwork: String

): Parcelable
