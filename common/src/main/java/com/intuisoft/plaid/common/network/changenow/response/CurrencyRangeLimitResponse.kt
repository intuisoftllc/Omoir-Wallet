package com.intuisoft.plaid.common.network.blockchair.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CurrencyRangeLimitResponse(

    @SerializedName("minAmount")
    val minAmount: String,

    @SerializedName("maxAmount")
    val maxAmount: String?
): Parcelable
