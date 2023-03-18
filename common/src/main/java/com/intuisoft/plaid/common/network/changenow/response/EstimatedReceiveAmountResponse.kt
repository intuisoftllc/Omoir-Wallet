package com.intuisoft.plaid.common.network.changenow.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EstimatedReceiveAmountResponse(

    @SerializedName("rateId")
    val rateId: String?,

    @SerializedName("validUntil")
    val validUntil: String?,

    @SerializedName("toAmount")
    val toAmount: Double
): Parcelable
