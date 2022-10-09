package com.intuisoft.plaid.network.sync.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate


@Parcelize
data class XpubTxDataResponse(
    val page: Int,
    val totalPages: Int,
    val transactions: List<TransactionResponse>?
) : Parcelable