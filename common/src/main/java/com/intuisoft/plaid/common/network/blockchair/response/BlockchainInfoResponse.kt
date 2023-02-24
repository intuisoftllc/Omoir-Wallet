package com.intuisoft.plaid.common.network.blockchair.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BlockStatsResponse(
    @SerializedName("data")
    val data: BlockStatsData
): Parcelable

@Parcelize
data class BlockStatsData(
    @SerializedName("blocks")
    val blocks: Int,

    @SerializedName("difficulty")
    val difficulty: Double,

    @SerializedName("blockchain_size")
    val blockchain_size: Long,

    @SerializedName("mempool_transactions")
    val mempool_transactions: Int,

    @SerializedName("mempool_size")
    val mempool_size: Long,

    @SerializedName("mempool_tps")
    val mempool_tps: Double,

    @SerializedName("hodling_addresses")
    val hodling_addresses : Long,

    @SerializedName("nodes")
    val nodes: Int,
): Parcelable
