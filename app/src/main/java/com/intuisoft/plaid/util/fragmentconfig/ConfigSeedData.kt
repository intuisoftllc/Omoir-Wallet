package com.intuisoft.plaid.util.fragmentconfig

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigSeedData(
    val seedPhrase: List<String>
): Parcelable