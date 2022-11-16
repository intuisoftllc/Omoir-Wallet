package com.intuisoft.plaid.common.network.nownodes.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BasicPriceDataResponse(
    val bitcoin: BitcoinPriceDataResponse
): Parcelable

@Parcelize
data class BitcoinPriceDataResponse(
    val usd: Double,
    val usd_market_cap: Double,
    val cad: Double,
    val cad_market_cap: Double,
    val eur: Double,
    val eur_market_cap: Double
): Parcelable