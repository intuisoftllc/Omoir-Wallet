package com.intuisoft.plaid.common.network.nownodes.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BlockchainInfoResponse(
    val result: BlockchainInfoResult
): Parcelable

@Parcelize
data class BlockchainInfoResult(
    val blocks: Int,
    val difficulty: Double,
    val size_on_disk: Long
): Parcelable