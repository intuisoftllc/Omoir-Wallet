package com.intuisoft.plaid.androidwrappers

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FragmentConfiguration(
    val actionBarTitle: Int,
    val showActionBar: Boolean,
    val configurationType: FragmentConfigurationType,
    val configData: Parcelable?
): Parcelable