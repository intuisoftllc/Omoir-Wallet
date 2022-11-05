package com.intuisoft.plaid.listeners

import com.intuisoft.plaid.model.LocalWalletModel

interface StateListener {
    fun onWalletStateUpdated(wallet: LocalWalletModel)
}