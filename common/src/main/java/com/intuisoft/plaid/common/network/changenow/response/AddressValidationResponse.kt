package com.intuisoft.plaid.common.network.changenow.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddressValidationResponse(

    @SerializedName("result")
    val result: Boolean,

    @SerializedName("message")
    val message: String?,
): Parcelable
