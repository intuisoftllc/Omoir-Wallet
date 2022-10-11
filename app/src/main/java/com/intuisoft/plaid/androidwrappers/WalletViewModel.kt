package com.intuisoft.plaid.androidwrappers

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.WalletManager
import io.horizontalsystems.bitcoincore.core.Bip
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


open class WalletViewModel(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletManager
): BaseViewModel(application, localStoreRepository, walletManager) {

    protected val _seedPhraseGenerated = SingleLiveData<List<String>>()
    val seedPhraseGenerated: LiveData<List<String>> = _seedPhraseGenerated

    protected val _userPassphrase = SingleLiveData<String>()
    val userPassphrase: LiveData<String> = _userPassphrase

    protected val _walletCreationError = SingleLiveData<Unit>()
    val walletCreationError: LiveData<Unit> = _walletCreationError

    protected val _transactions = SingleLiveData<List<TransactionInfo>>()
    val transactions: LiveData<List<TransactionInfo>> = _transactions

    protected val _walletCreated = SingleLiveData<String>()
    val walletCreated: LiveData<String> = _walletCreated

    protected val _displayWallet = SingleLiveData<LocalWalletModel>()
    val displayWallet: LiveData<LocalWalletModel> = _displayWallet

    protected val _walletName = SingleLiveData<String>()
    val walletName: LiveData<String> = _walletName

    protected val _walletNetwork = SingleLiveData<BitcoinKit.NetworkType>()
    val walletNetwork: LiveData<BitcoinKit.NetworkType> = _walletNetwork

    protected val _walletBip = SingleLiveData<Bip>()
    val walletBip: LiveData<Bip> = _walletBip

    protected var localWallet: LocalWalletModel? = null
    private val disposables = CompositeDisposable()

    fun showWalletName() {
        _walletName.postValue(getWalletName())
    }

    fun showWalletNetwork() {
        _walletNetwork.postValue(getWalletNetwork())
    }

    fun showWalletBip() {
        _walletBip.postValue(getWalletBip())
    }

    fun showWalletPassphrase() {
        _userPassphrase.postValue(getWalletPassphrase())
    }

    fun displayCurrentWallet() {
        localWallet?.let {
            _displayWallet.postValue(it)
        }
    }

    protected fun generateNewWallet(passphrase: String) {
        viewModelScope.launch {
//            seed = Mnemonic().generate(entropyStrength)
//            val seed = "wrong cousin spell stadium snake enact author piano venue outer question chair".split(" ")
            val seed = "yard impulse luxury drive today throw farm pepper survey wreck glass federal".split(" ")
            _seedPhraseGenerated.postValue(seed!!)
            _userPassphrase.postValue(passphrase)
        }
    }

    fun setWallet(uuid: String) {
        localWallet = walletManager.findLocalWallet(uuid)
    }

    fun getWalletPassphrase() = walletManager.findStoredWallet(localWallet!!.uuid)!!.passphrase

    fun getWalletSeedPhrase() = walletManager.findStoredWallet(localWallet!!.uuid)!!.seedPhrase

    fun getMasterPublicKey() : String {
        return localWallet!!.walletKit!!.getWallet().masterPublicKey()
    }

    fun getRecieveAddress() : String {
        return localWallet!!.walletKit!!.receiveAddress()
    }

    fun getWalletNetwork() =
        if(walletManager.findStoredWallet(localWallet!!.uuid)!!.isTestNet)
            BitcoinKit.NetworkType.TestNet
        else BitcoinKit.NetworkType.MainNet

    fun getWalletName() = localWallet!!.name

    fun getWalletId() = localWallet!!.uuid

    fun getWalletBip(): Bip {
        val bip = walletManager.findStoredWallet(localWallet!!.uuid)!!.bip
        return Bip.values().find { it.ordinal == bip }!!
    }

    fun updateWalletName(name: String) {
        walletManager.updateWalletName(localWallet!!, name)
    }

    fun getTransactions() {
        localWallet!!.walletKit!!.transactions(type = null).subscribe { txList: List<TransactionInfo> ->
            _transactions.postValue(txList)
        }.let {
            disposables.add(it)
        }
    }

    fun syncWallet() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                walletManager.synchronize(localWallet!!, true)
            }
        }
    }

    /**
     * Save wallet to dick every 1 minute
     *
     */
    protected fun commitWalletToDisk(
        walletName: String,
        seed: List<String>,
        passphrase: String,
        bip: Bip,
        testNetWallet: Boolean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (doesWalletExist(walletName)) {
                    _walletCreationError.postValue(Unit)
                } else {
                    val walletId = walletManager.createWallet(
                        name = walletName,
                        seed = seed!!,
                        passphrase = passphrase,
                        bip = bip,
                        testnetWallet = testNetWallet
                    )

                    _walletCreated.postValue(walletId)
                }
            }
        }
    }


    fun deleteWallet(onDeleteFinished: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                walletManager.deleteWallet(localWallet!!) {

                    withContext(Dispatchers.Main) {
                        onDeleteFinished()
                    }
                }
            }
        }
    }

    suspend fun doesWalletExist(walletName: String) : Boolean {
        return walletManager.doesWalletExist(walletName)
    }
}