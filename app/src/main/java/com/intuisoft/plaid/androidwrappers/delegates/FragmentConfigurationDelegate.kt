package com.intuisoft.plaid.androidwrappers.delegates

import android.os.Parcelable
import com.intuisoft.plaid.androidwrappers.FragmentConfigurationType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FragmentConfiguration(
    val actionBarTitle: Int = 0,
    val actionBarSubtitle: Int = 0,
    val actionBarVariant: Int = 0,
    val actionLeft: Int = 0,
    val actionRight: Int = 0,
    val configurationType: FragmentConfigurationType,
    val configData: Parcelable?
): Parcelable