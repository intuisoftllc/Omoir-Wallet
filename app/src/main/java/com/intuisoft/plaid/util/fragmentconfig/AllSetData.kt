package com.intuisoft.plaid.util.fragmentconfig

import android.os.Parcelable
import androidx.navigation.NavDestination
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AllSetData(
    val title: String,
    val subtitle: String,
    val positiveText: String,
    val negativeText: String,
    val positiveDestination: Int,
    val negativeDestination: Int
): Parcelable