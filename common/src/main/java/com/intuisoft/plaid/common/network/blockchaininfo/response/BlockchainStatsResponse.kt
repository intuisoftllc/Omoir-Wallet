package com.intuisoft.plaid.common.network.blockchaininfo.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BlockchainStatsResponse(
    @SerializedName("minutes_between_blocks")
    val minutes_between_blocks: Double
): Parcelable