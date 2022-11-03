package com.intuisoft.plaid.util.fragmentconfig

import android.os.Parcelable
import androidx.navigation.NavDestination
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SendFundsData(
    val amountToSend: Long,
    val spendFrom: List<String>
): Parcelable