package com.intuisoft.plaid.common.network.changenow.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExchangeStatusResponse(

    @SerializedName("id")
    val id: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("fromCurrency")
    val fromCurrency: String,

    @SerializedName("fromNetwork")
    val fromNetwork: String,

    @SerializedName("toCurrency")
    val toCurrency: String,

    @SerializedName("toNetwork")
    val toNetwork: String,

    @SerializedName("expectedAmountFrom")
    val expectedAmountFrom: Double?,

    @SerializedName("expectedAmountTo")
    val expectedAmountTo: Double?,

    @SerializedName("amountFrom")
    val amountFrom: Double?,

    @SerializedName("amountTo")
    val amountTo: Double?,

    @SerializedName("payinAddress")
    val payinAddress: String,

    @SerializedName("payoutAddress")
    val payoutAddress: String,

    @SerializedName("payinExtraId")
    val payinExtraId: String?,

    @SerializedName("payoutExtraId")
    val payoutExtraId: String?,

    @SerializedName("refundAddress")
    val refundAddress: String?,

    @SerializedName("refundExtraId")
    val refundExtraId: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("payinHash")
    val payinHash: String?,

    @SerializedName("payoutHash")
    val payoutHash: String?
): Parcelable
