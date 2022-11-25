package com.intuisoft.plaid.util.fragmentconfig

import android.os.Parcelable
import androidx.navigation.NavDestination
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InvoiceData(
    val amountToSend: Double,
    val address: String,
    val memo: String? = null
): Parcelable