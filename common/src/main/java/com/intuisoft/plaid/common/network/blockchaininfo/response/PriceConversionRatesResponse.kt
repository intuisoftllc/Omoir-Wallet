package com.intuisoft.plaid.common.network.nownodes.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PriceConversionRatesResponse(
    val USD: PriceConversionRate,
    val EUR: PriceConversionRate,
    val CAD: PriceConversionRate,
): Parcelable

@Parcelize
data class PriceConversionRate(
    val last: Double,
    val symbol: String
): Parcelable