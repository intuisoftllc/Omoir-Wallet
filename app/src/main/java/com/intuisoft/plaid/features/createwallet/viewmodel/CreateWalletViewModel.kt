package com.intuisoft.plaid.features.createwallet.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.WalletManager
import io.horizontalsystems.bitcoincore.core.Bip
import io.horizontalsystems.hdwalletkit.Mnemonic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit


class CreateWalletViewModel(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletManager
): WalletViewModel(application, localStoreRepository, walletManager) {

    private var seed: List<String>? = null
    private var bip: Bip = Bip.BIP84
    private var passphrase = ""
    private var entropyStrength = Mnemonic.EntropyStrength.Default

    var useTestNet = false
        private set

    fun generateNewWallet() {
        generateNewWallet(passphrase)
    }

    fun setUseTestNet(use: Boolean) {
        useTestNet = use
    }

    fun setEntropyStrength(strength: Mnemonic.EntropyStrength) {
        entropyStrength = strength
    }

    fun setBip(bip: Bip) {
        this.bip = bip
    }

    fun getEntropyStrength() = entropyStrength

    fun getBipType() = bip

    fun setPassphrase(p: String) {
        passphrase = p
    }

    fun setSeedPhrase(p: List<String>) {
        seed = p
    }

    fun showLocalSeedPhrase() {
        _seedPhraseGenerated.postValue(seed)
    }

    fun showLocalPassPhrase() {
        _userPassphrase.postValue(passphrase)
    }

    /**
     * Save wallet to dick every 1 minute
     *
     */
    fun commitWalletToDisk(walletName: String) {
        commitWalletToDisk(
            walletName = walletName,
            seed = seed!!,
            passphrase = passphrase,
            bip = bip,
            testNetWallet = useTestNet
        )
    }
}