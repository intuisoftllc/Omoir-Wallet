package com.intuisoft.plaid.common.network.nownodes.response

import android.os.Parcelable
import com.google.gson.JsonArray
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BlockStatsResponse(
    val result: BlockStatsResult
): Parcelable

@Parcelize
data class BlockStatsResult(
    val height: Int,
    val avgtxsize: Int,
    val avgfeerate: Int,
): Parcelable