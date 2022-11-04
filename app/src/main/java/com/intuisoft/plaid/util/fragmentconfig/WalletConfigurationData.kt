package com.intuisoft.plaid.util.fragmentconfig

import android.os.Parcelable
import androidx.navigation.NavDestination
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WalletConfigurationData(
    val testNetWallet: Boolean,
    val wordCount: Int,
    val bip: Int,
    val seedPhrase: List<String>,
    val publicKey: String
): Parcelable