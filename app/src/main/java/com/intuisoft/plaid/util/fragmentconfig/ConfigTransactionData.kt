package com.intuisoft.plaid.util.fragmentconfig

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigTransactionData(
    val payload: String
): Parcelable