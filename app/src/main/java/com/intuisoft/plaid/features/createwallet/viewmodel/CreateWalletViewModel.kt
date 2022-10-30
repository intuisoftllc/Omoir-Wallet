package com.intuisoft.plaid.features.createwallet.viewmodel

import android.app.Application
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