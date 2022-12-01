package com.intuisoft.plaid.common.network.blockstreaminfo.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FeeEstimatesResponse(
    @SerializedName("144")
    val low: Double,

    @SerializedName("6")
    val med: Double,

    @SerializedName("1")
    val high: Double,
): Parcelable