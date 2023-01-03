package com.intuisoft.plaid.util.fragmentconfig

import android.os.Parcelable
import androidx.navigation.NavDestination
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SendFundsData(
    val amountToSend: Long,
    val spendFrom: List<String>,
    val address: String? = null,
    val memo: String? = null,
    val exchangeId: String? = null,
    val invoiceSend: Boolean = false,
): Parcelable