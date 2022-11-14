package com.intuisoft.plaid.common.network.nownodes.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class XpubTxDataResponse(
    val page: Int,
    val totalPages: Int,
    val transactions: List<TransactionResponse>?
) : Parcelable