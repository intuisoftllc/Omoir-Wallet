package com.intuisoft.plaid.common.network.nownodes.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TransactionResponse(
    val txid: String,
    val hex: String
): Parcelable