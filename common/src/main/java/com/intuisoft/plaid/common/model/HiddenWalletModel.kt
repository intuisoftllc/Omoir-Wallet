package com.intuisoft.plaid.common.model

import com.intuisoft.plaid.common.util.extensions.sha256
import java.util.UUID

data class HiddenWalletModel(
    val walletUUID: String,
    var passphrase: String,
    val account: SavedAccountModel
) {
    val uuid: String
        get() = "$walletUUID: $passphrase - ${account.account}".sha256(16)
}