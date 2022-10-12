package com.intuisoft.plaid.util.fragmentconfig

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigQrDisplayData(
    val payload: String,
    val qrTitle: String? = null
): Parcelable