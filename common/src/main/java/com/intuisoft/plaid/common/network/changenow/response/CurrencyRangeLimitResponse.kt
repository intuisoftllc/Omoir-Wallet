package com.intuisoft.plaid.common.network.blockchair.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CurrencyRangeLimitResponse(
    val minAmount: String,
    val maxAmount: String?
): Parcelable
