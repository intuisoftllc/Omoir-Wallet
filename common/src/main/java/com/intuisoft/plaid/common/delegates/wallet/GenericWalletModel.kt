package com.intuisoft.plaid.common.delegates.wallet

import com.intuisoft.plaid.common.model.HiddenWalletModel
import com.intuisoft.plaid.common.model.WalletIdentifier

abstract class GenericWalletModel(
    val uid: String,
    val walletName: String,
    val testNetWallet: Boolean,
    val hiddenWallet: Boolean,
) {
    abstract val isSyncing: Boolean

    abstract val isSynced: Boolean

    abstract val isRestored: Boolean

    abstract val notStarted: Boolean

    abstract val syncPercentage: Int
}