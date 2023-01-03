package com.intuisoft.plaid.common.network.blockchair.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SupportedCurrencyResponse(
    val ticker: String,
    val name: String,
    val image: String,
    val hasExternalId: Boolean,
    val isFiat: Boolean,
    val network: String
): Parcelable
