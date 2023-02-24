package com.intuisoft.plaid.common.network.blockchair.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SupportedCurrencyResponse(

    @SerializedName("ticker")
    val ticker: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("image")
    val image: String,

    @SerializedName("hasExternalId")
    val hasExternalId: Boolean,

    @SerializedName("isFiat")
    val isFiat: Boolean,

    @SerializedName("network")
    val network: String
): Parcelable
