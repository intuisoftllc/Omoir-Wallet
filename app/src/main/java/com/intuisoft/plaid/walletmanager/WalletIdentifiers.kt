package com.intuisoft.plaid.walletmanager

data class WalletIdentifier(
    var name: String,
    val walletUUID: String,
    val seedPhrase: List<String>,
    val passphrase: String,
    var bip: Int,
    var isTestNet: Boolean,
)