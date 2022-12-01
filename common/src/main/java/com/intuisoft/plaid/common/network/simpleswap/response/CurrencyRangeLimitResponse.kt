package com.intuisoft.plaid.common.network.blockchair.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CurrencyRangeLimitResponse(
    val min: String,
    val max: String
): Parcelable
