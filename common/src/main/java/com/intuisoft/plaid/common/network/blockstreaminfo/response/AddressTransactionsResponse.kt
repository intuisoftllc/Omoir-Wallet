package com.intuisoft.plaid.common.network.blockstreaminfo.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddressTransactionsResponse(
    val vout: List<TxOutput>,
    val status: TxStatus
): Parcelable

@Parcelize
data class TxStatus(
    val block_height: Int?,
    val block_hash: String?,
): Parcelable

@Parcelize
data class TxOutput(
    val scriptpubkey: String,
    val scriptpubkey_address: String,
): Parcelable