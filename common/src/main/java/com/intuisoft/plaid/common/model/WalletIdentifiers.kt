package com.intuisoft.plaid.common.model

data class WalletIdentifier(
    var name: String,
    val walletUUID: String,
    val seedPhrase: List<String>,
    val pubKey: String,
    val walletHashIds: MutableList<String>,
    var bip: Int,
    var lastSynced: Long,
    var createdAt: Long,
    var isTestNet: Boolean,
    var readOnly: Boolean,

    // @NonNull
    var gapLimit: Int?
) {

    val isPrivateKeyWallet: Boolean
        get() = seedPhrase.isEmpty() && !readOnly && pubKey.isNotEmpty()
}