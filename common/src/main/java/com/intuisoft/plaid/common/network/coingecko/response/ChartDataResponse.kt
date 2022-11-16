package com.intuisoft.plaid.common.network.nownodes.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChartDataResponse(
    val prices: List<List<Double>>
): Parcelable
