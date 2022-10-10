package com.intuisoft.plaid.features.createwallet.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.model.WalletType
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.WalletManager
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
): BaseViewModel(application, localStoreRepository, walletManager) {

    private var seed: List<String>? = null
    private var walletType: WalletType? = null

    private val _walletAlreadyExists = SingleLiveData<Boolean>()
    val walletAlreadyExists: LiveData<Boolean> = _walletAlreadyExists

    private val _seedPhraseGenerated = SingleLiveData<List<String>>()
    val seedPhraseGenerated: LiveData<List<String>> = _seedPhraseGenerated

    private val _userPassphrase = SingleLiveData<String>()
    val userPassphrase: LiveData<String> = _userPassphrase

    var useTestNet = false
        private set

    private var passphrase = ""

    private var entropyStrength = Mnemonic.EntropyStrength.Default


    fun generateNewWallet() {
        viewModelScope.launch {
            walletType = WalletType.READ_WRITE

//            seed = Mnemonic().generate(entropyStrength)
            seed = "yard impulse luxury drive today throw farm pepper survey wreck glass federal".split(" ")
            _seedPhraseGenerated.postValue(seed!!)
            _userPassphrase.postValue(passphrase)
        }
    }

    fun setUseTestNet(use: Boolean) {
        useTestNet = use
    }

    fun setEntropyStrength(strength: Mnemonic.EntropyStrength) {
        entropyStrength = strength
    }

    fun getPassphrase() = passphrase

    fun getEntropyStrength() = entropyStrength

    fun setPassphrase(p: String) {
        passphrase = p
    }

    /**
     * Save wallet to dick every 1 minute
     *
     */
    fun commitWalletToDisk(walletName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (doesWalletExist(walletName)) {
                    _walletAlreadyExists.postValue(true)
                } else {
                    _walletAlreadyExists.postValue(false)
                    walletManager.createWallet(
                        name = walletName,
                        seed = seed!!,
                        passphrase = passphrase,
                        walletType = walletType!!,
                        testnetWallet = useTestNet
                    )
                }
            }
        }
    }
}