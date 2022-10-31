package com.intuisoft.plaid.model

import android.content.Context
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.util.entensions.sha256
import com.intuisoft.plaid.walletmanager.StoredWalletInfo
import com.intuisoft.plaid.walletmanager.WalletIdentifier
import com.intuisoft.plaid.walletmanager.WalletManager
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Utils


data class LocalWalletModel(
    var name: String,
    var hashId: String,
    var uuid: String,
    var testNetWallet: Boolean
) {
    var walletKit: BitcoinKit? = null
    var lastSyncedTime = 0
    var walletState = WalletState.NONE

    var walletStateUpdated = MutableLiveData<Int>()

    fun walletStateOrType(
        walletState: TextView,
        progress: Int
    ) {
        when(this.walletState) {
            WalletState.SYNCING -> {
                if(progress >= 1) {
                    walletState.text = walletState.context.getString(R.string.syncing_percent, progress.toString())
                } else {
                    walletState.text = walletState.context.getString(R.string.syncing)
                }
            } else -> {
                when(walletKit!!.getPurpose()) {
                    HDWallet.Purpose.BIP84 -> {
                        walletState.text = walletState.context.getString(R.string.create_wallet_advanced_options_bip_1_short)

                    }
                    HDWallet.Purpose.BIP49 -> {
                        walletState.text = walletState.context.getString(R.string.create_wallet_advanced_options_bip_2_short)
                    }
                    HDWallet.Purpose.BIP44 -> {
                        walletState.text = walletState.context.getString(R.string.create_wallet_advanced_options_bip_3_short)
                    }
                }
            }
        }
    }

    fun getBalance(localStoreRepository: LocalStoreRepository, shortenSats: Boolean): String {
        return SimpleCoinNumberFormat.format(localStoreRepository, walletKit!!.balance.spendable, shortenSats)
    }

    fun onWalletStateChanged(
        context: Context,
        progress: Int,
        shortenSats: Boolean,
        localStoreRepository: LocalStoreRepository
    ): String {
        when(walletState) {
            WalletState.SYNCING -> {
                if(progress >= 1) {
                    return context.getString(R.string.syncing_percent, progress.toString())
                } else {
                    return context.getString(R.string.syncing)
                }
            } else -> {
                return getBalance(localStoreRepository, shortenSats)
            }
        }
    }

    companion object {

        fun consume(walletIdentifier: WalletIdentifier, passphrase: String): LocalWalletModel =
            LocalWalletModel(
                name = walletIdentifier.name,
                uuid = walletIdentifier.walletUUID,
                hashId = (walletIdentifier.walletUUID + passphrase).sha256(),
                testNetWallet = walletIdentifier.isTestNet
            )
    }
}