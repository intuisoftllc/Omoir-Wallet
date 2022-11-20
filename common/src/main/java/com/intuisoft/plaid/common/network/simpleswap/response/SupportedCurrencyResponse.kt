package com.intuisoft.plaid.common.network.nownodes.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SupportedCurrencyResponse(
    val symbol: String,
    val name: String,
    val image: String,
    val validation_address: String?,
    val validation_extra: String?
): Parcelable
