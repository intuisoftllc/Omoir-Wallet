package com.intuisoft.plaid.common.network.blockstreaminfo.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddressTransactionsResponse(

    @SerializedName("vout")
    val vout: List<TxOutput>,

    @SerializedName("status")
    val status: TxStatus,

    @SerializedName("txid")
    val txid: String
): Parcelable

@Parcelize
data class TxStatus(

    @SerializedName("block_height")
    val block_height: Int?,

    @SerializedName("block_hash")
    val block_hash: String?,
): Parcelable

@Parcelize
data class TxOutput(

    @SerializedName("scriptpubkey")
    val scriptpubkey: String,

    @SerializedName("scriptpubkey_address")
    val scriptpubkey_address: String,
): Parcelable