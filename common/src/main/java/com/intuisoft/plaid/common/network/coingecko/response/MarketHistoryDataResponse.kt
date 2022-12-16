package com.intuisoft.plaid.common.network.blockchair.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MarketHistoryDataResponse(
    val prices: List<List<Double>>
): Parcelable
