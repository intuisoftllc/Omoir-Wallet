package com.intuisoft.plaid.walletmanager

data class WalletIdentifier(
    var name: String,
    val walletUUID: String,
    val seedPhrase: List<String>,
    val pubKey: String,
    val walletHashIds: MutableList<String>?,
    var bip: Int,
    var lastSynced: Long,
    var createdAt: Long,
    var isTestNet: Boolean,
    var readOnly: Boolean
)