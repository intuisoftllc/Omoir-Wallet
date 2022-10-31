package com.intuisoft.plaid.features.createwallet.viewmodel

import android.app.Application
import android.content.Context
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic


class CreateWalletViewModel(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, walletManager) {

    private var seed: List<String>? = null
    private var bip: HDWallet.Purpose = HDWallet.Purpose.BIP84
    private var entropyStrength = Mnemonic.EntropyStrength.Default

    var useTestNet = false
        private set

    fun generateNewWallet() {
        generateNewWallet(entropyStrength)
    }

    fun setUseTestNet(use: Boolean) {
        useTestNet = use
    }

    fun setEntropyStrength(strength: Mnemonic.EntropyStrength) {
        entropyStrength = strength
    }

    fun setLocalBip(bip: HDWallet.Purpose) {
        this.bip = bip
    }

    fun getEntropyStrength() = entropyStrength

    fun getLocalBipType() = bip

    fun entropyStrengthToString(context: Context) : String {
        when(getEntropyStrength()) {
            Mnemonic.EntropyStrength.Default -> {
                return context.getString(R.string.create_wallet_advanced_options_entropy_strength_1)
            }
            Mnemonic.EntropyStrength.Low -> {
                return context.getString(R.string.create_wallet_advanced_options_entropy_strength_2)
            }
            Mnemonic.EntropyStrength.Medium -> {
                return context.getString(R.string.create_wallet_advanced_options_entropy_strength_3)
            }
            Mnemonic.EntropyStrength.High -> {
                return context.getString(R.string.create_wallet_advanced_options_entropy_strength_4)
            }
            Mnemonic.EntropyStrength.VeryHigh -> {
                return context.getString(R.string.create_wallet_advanced_options_entropy_strength_5)
            }
            else -> {
                return "unknown"
            }
        }
    }

    fun setLocalSeedPhrase(p: List<String>) {
        seed = p
    }

    fun showLocalSeedPhrase() {
        _seedPhraseGenerated.postValue(seed)
    }

    /**
     * Save wallet to disk every 1 minute
     *
     */
    fun commitWalletToDisk(walletName: String) {
        commitWalletToDisk(
            walletName = walletName,
            seed = seed!!,
            bip = bip,
            testNetWallet = useTestNet
        )
    }
}