package com.intuisoft.plaid.common.model

data class StoredHiddenWalletsModel(
    val hiddenWallets: List<Pair<String, HiddenWalletModel?>>
)