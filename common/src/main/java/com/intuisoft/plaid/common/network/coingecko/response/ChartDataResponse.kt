package com.intuisoft.plaid.common.network.blockchair.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChartDataResponse(
    val prices: List<List<Double>>
): Parcelable
