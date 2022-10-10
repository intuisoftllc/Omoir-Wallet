package com.intuisoft.plaid.walletmanager

data class WalletIdentifier(
    val name: String,
    val seedPhrase: List<String>,
    val passphrase: String,
    val type: Long,
    val isTestNet: Boolean,
)