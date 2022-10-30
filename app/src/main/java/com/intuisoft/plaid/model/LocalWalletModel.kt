package com.intuisoft.plaid.model

import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
                    walletState.text = Constants.Strings.SYNCING + " ($progress%)"
                } else {
                    walletState.text = Constants.Strings.SYNCING
                }
            } else -> {
                when(walletKit!!.getPurpose()) {
                    HDWallet.Purpose.BIP84 -> {
                        walletState.text = Constants.Strings.BIP_TYPE_84
                    }
                    HDWallet.Purpose.BIP49 -> {
                        walletState.text =Constants.Strings.BIP_TYPE_49
                    }
                    HDWallet.Purpose.BIP44 -> {
                        walletState.text = Constants.Strings.BIP_TYPE_44
                    }
                }
            }
        }
    }

    fun getBalance(localStoreRepository: LocalStoreRepository, fullValue: Boolean): String {
        return SimpleCoinNumberFormat.format(localStoreRepository, walletKit!!.balance.spendable, fullValue)
    }

    fun onWalletStateChanged(
        walletBalance: TextView,
        progress: Int,
        showFullBalance: Boolean,
        localStoreRepository: LocalStoreRepository
    ) {
        when(walletState) {
            WalletState.SYNCING -> {
                if(progress >= 1) {
                    walletBalance.text = Constants.Strings.SYNCING + " ($progress%)"
                } else {
                    walletBalance.text = Constants.Strings.SYNCING
                }
            } else -> {
                walletBalance.text = getBalance(localStoreRepository, showFullBalance)
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