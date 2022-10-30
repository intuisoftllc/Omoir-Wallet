package com.intuisoft.plaid.androidwrappers

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FragmentConfiguration(
    val actionBarTitle: Int = 0,
    val actionBarSubtitle: Int = 0,
    val actionbarVariant: Int = 0,
    val actionLeft: Int = 0,
    val actionRight: Int = 0,
    val configurationType: FragmentConfigurationType,
    val configData: Parcelable?
): Parcelable