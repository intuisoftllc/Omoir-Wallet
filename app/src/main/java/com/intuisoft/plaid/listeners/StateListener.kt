package com.intuisoft.plaid.listeners

import com.intuisoft.plaid.common.delegates.wallet.GenericWalletModel

interface StateListener {
    fun onWalletStateUpdated(wallet: GenericWalletModel)
    fun onWalletAlreadySynced(wallet: GenericWalletModel)
}