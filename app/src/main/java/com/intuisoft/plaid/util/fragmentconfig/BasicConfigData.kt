package com.intuisoft.plaid.util.fragmentconfig

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BasicConfigData(
    val payload: String
): Parcelable