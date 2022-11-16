package com.intuisoft.plaid.common.network.nownodes.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BlockchainStatsResponse(
    val minutes_between_blocks: Double
): Parcelable