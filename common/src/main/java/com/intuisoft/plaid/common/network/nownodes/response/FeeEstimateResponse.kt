package com.intuisoft.plaid.common.network.nownodes.response

import android.os.Parcelable
import com.google.gson.JsonArray
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FeeEstimateResponse(
    val result: FeeResultEstimate
): Parcelable

@Parcelize
data class FeeResultEstimate(
    val feerate: Double
): Parcelable