package com.intuisoft.plaid.walletmanager

data class WalletIdentifier(
    var name: String,
    val walletUUID: String,
    val seedPhrase: List<String>,
    val walletHashIds: MutableList<String>?,
    var bip: Int,
    var isTestNet: Boolean,
    var apiSyncMode: Boolean
)