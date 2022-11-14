package com.intuisoft.plaid.common.model

import com.intuisoft.plaid.walletmanager.WalletIdentifier

data class StoredWalletInfo(
    val walletIdentifiers: MutableList<WalletIdentifier>
)