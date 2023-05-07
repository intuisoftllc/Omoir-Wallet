package com.intuisoft.plaid.delegates.wallet.btc

import android.content.Context
import android.widget.TextView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.common.delegates.wallet.GenericWalletModel
import com.intuisoft.plaid.common.delegates.wallet.WalletDelegate
import com.intuisoft.plaid.common.model.HiddenWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.model.WalletIdentifier
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDWallet


data class BTCWalletModel(
    val name: String,
    val uuid: String,
    val testNet: Boolean,
    val hidden: Boolean,
): GenericWalletModel(name, uuid, testNet, hidden) {
    var walletKit: BitcoinKit? = null

    override val isSyncing: Boolean
        get() = walletKit!!.syncState.isSyncing()

    override val isSynced: Boolean
        get() = walletKit!!.syncState.hasSynced()

    override val isRestored: Boolean
        get() = walletKit!!.isRestored

    override val notStarted: Boolean
        get() = !isSynced && !isSynced

    override val syncPercentage: Int
        get() = (walletKit!!.syncState.syncPercentage() * 100).toInt()

    var lastSyncPercentage = -1

    fun walletStateOrType(
        walletState: TextView,
        progress: Int
    ) {
        when(isSyncing) {
            true -> {
                if(progress >= 1) {
                    walletState.text = walletState.context.getString(R.string.syncing_percent, progress.toString())
                } else {
                    walletState.text = walletState.context.getString(R.string.syncing)
                }
            }

            else -> {
                walletType(walletState)
            }
        }
    }

    fun walletType(
        walletState: TextView
    ) {
        if(testNetWallet) {
            if(walletKit!!.watchAccount) {
                walletState.text =
                    walletState.context.getString(R.string.wallet_dashboard_state_testnet_read_only)
            } else {
                walletState.text =
                    walletState.context.getString(R.string.wallet_dashboard_state_testnet)
            }
        } else {
            if(walletKit!!.watchAccount) {
                walletState.text =
                    walletState.context.getString(R.string.wallet_dashboard_state_mainnet_read_only)
            } else {
                when (walletKit!!.getPurpose()) {
                    HDWallet.Purpose.BIP84 -> {
                        walletState.text =
                            walletState.context.getString(R.string.create_wallet_advanced_options_bip_1_short)
                    }
                    HDWallet.Purpose.BIP49 -> {
                        walletState.text =
                            walletState.context.getString(R.string.create_wallet_advanced_options_bip_2_short)
                    }
                    HDWallet.Purpose.BIP44 -> {
                        walletState.text =
                            walletState.context.getString(R.string.create_wallet_advanced_options_bip_3_short)
                    }
                }
            }
        }
    }

    fun getWhitelistedBalance(localStoreRepository: LocalStoreRepository): Long {
        return getWhitelistedUtxos(localStoreRepository).map { it.output.value }.sum()
    }

    fun getWhitelistedUtxos(localStoreRepository: LocalStoreRepository): List<UnspentOutput> {
        val blacklist = localStoreRepository.getAllBlacklistedAddresses()
        val utxos = walletKit!!.getUnspentOutputs()
        return utxos.filter { utxo ->
            blacklist.find { it.address == utxo.output.address!! } == null
        }
    }

    fun getBalance(localStoreRepository: LocalStoreRepository, shortenSats: Boolean): String {
        return SimpleCoinNumberFormat.format(localStoreRepository, getWhitelistedBalance(localStoreRepository), shortenSats)
    }

    fun onWalletStateChanged(
        context: Context,
        progress: Int,
        shortenSats: Boolean,
        localStoreRepository: LocalStoreRepository
    ): String {
        when(isSyncing) {
            true -> {
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

        fun consume(walletIdentifier: WalletIdentifier, hiddenWallet: HiddenWalletModel?): LocalWalletModel =
            LocalWalletModel(
                name = walletIdentifier.name,
                uuid = hiddenWallet?.uuid ?: walletIdentifier.walletUUID,
                testNetWallet = walletIdentifier.isTestNet,
                hiddenWallet = hiddenWallet != null
            )
    }
}