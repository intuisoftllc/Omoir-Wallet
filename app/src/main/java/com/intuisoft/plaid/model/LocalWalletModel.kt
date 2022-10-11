package com.intuisoft.plaid.model

import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.walletmanager.StoredWalletInfo
import com.intuisoft.plaid.walletmanager.WalletIdentifier
import io.horizontalsystems.bitcoinkit.BitcoinKit
import kotlinx.android.synthetic.main.basic_wallet_list_item.view.*


data class LocalWalletModel(
    var name: String,
    var uuid: String,
    var testNetWallet: Boolean
) {
    var walletKit: BitcoinKit? = null
    var lastSyncedTime = 0
    var walletState = WalletState.NONE

    var walletStateUpdated = MutableLiveData<Int>()


    fun getBalance(localStoreRepository: LocalStoreRepository): String {
        if(localStoreRepository.getBitcoinDisplayUnit() == BitcoinDisplayUnit.BTC) {
            return "" + this.walletKit!!.balance.spendable / Constants.Limit.SATS_PER_BTC + " BTC"
        } else {
            val balance = 0L//wallet.wallet?.balance?.value ?: 0L
            val postfix = if(balance == 1L) "Sat" else "Sats"

            return SimpleCoinNumberFormat.format(balance) + " " + postfix
        }
    }

    fun onWalletStateChanged(
        walletBalance: TextView,
        progress: Int,
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
                walletBalance.text = getBalance(localStoreRepository)
            }
        }
    }

    companion object {

        fun consume(walletIdentifier: WalletIdentifier): LocalWalletModel =
            LocalWalletModel(
                name = walletIdentifier.name,
                uuid = walletIdentifier.walletUUID,
                testNetWallet = walletIdentifier.isTestNet
            )
    }
}