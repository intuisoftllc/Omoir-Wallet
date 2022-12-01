package com.intuisoft.plaid.common.network.blockchair.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BlockStatsResponse(
    val data: BlockStatsData
): Parcelable

@Parcelize
data class BlockStatsData(
    val blocks: Int,
    val difficulty: Double,
    val blockchain_size: Long,
    val mempool_transactions: Int,
    val mempool_size: Long,
    val mempool_tps: Double,
    val hodling_addresses : Long,
    val nodes: Int,
): Parcelable
