package com.intuisoft.plaid.model

import com.intuisoft.plaid.androidwrappers.SingleLiveData


data class LocalWalletModel(
    var name: String,
    var type: WalletType,
    var testNetWallet: Boolean
) {
//    var wallet: Wallet? = null
    var lastSyncedTime = 0
    var walletState = WalletState.NONE

    var walletStateUpdated = SingleLiveData<Unit>()
}