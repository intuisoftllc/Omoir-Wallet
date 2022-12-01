package com.intuisoft.plaid.common.network.blockchair.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BasicNetworkDataResponse(
    val currentSupply: Long
): Parcelable