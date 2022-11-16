package com.intuisoft.plaid.common.network.nownodes.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BasicNetworkDataResponse(
    val currentSupply: Long,
    val memPoolTxCount: Int,
): Parcelable