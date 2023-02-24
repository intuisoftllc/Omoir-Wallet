package com.intuisoft.plaid.common.network.blockchaininfo.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BasicNetworkDataResponse(
    @SerializedName("currentSupply")
    val currentSupply: Long
): Parcelable