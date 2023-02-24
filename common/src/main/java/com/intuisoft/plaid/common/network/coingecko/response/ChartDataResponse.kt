package com.intuisoft.plaid.common.network.coingecko.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChartDataResponse(

    @SerializedName("prices")
    val prices: List<List<Double>>
): Parcelable
