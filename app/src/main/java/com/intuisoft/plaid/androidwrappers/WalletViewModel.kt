package com.intuisoft.plaid.androidwrappers

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.walletmanager.WalletManager
import io.horizontalsystems.bitcoincore.core.Bip
import io.horizontalsystems.bitcoincore.managers.SendValueErrors
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


open class WalletViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
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

    protected val _walletBalance = SingleLiveData<String>()
    val walletBalance: LiveData<String> = _walletBalance

    protected val _walletDisplayUnit = SingleLiveData<BitcoinDisplayUnit>()
    val walletDisplayUnit: LiveData<BitcoinDisplayUnit> = _walletDisplayUnit

    protected val _walletBip = SingleLiveData<Bip>()
    val walletBip: LiveData<Bip> = _walletBip

    protected var localWallet: LocalWalletModel? = null
    private val disposables = CompositeDisposable()

    fun showWalletBalance() {
        _walletBalance.postValue(SimpleCoinNumberFormat.format(localStoreRepository, getWalletBalance(), true))
    }

    fun calculateFee(sats: Long, feeRate: Int, address: String?, retry: Boolean = true) : Long {
        try {
            return localWallet!!.walletKit!!.fee(sats, address, true, feeRate)
        } catch(e: SendValueErrors.Dust) {
            return -2
        } catch(e: SendValueErrors.InsufficientUnspentOutputs) {
            if(retry) {
                var max : Long
                try {
                    max = localWallet!!.walletKit!!.maximumSpendableValue(
                        address,
                        feeRate
                    )

                    return calculateFee(
                        max,
                        feeRate,
                        address,
                        false
                    )
                } catch (e: Exception) {
                    return -1
                }
            } else {
                return -1
            }
        } catch(e: Exception) {
            return 0
        }
    }

    fun calculateFee(unspentOutputs: List<UnspentOutput>, sats: Long, feeRate: Int, address: String?, retry: Boolean = true) : Long {
        try {
            return localWallet!!.walletKit!!.fee(unspentOutputs, sats, address, true, feeRate)
        } catch(e: SendValueErrors.Dust) {
            return -2
        } catch(e: SendValueErrors.InsufficientUnspentOutputs) {
            if(retry) {
                var max : Long
                try {
                    max = localWallet!!.walletKit!!.maximumSpendableValue(
                        unspentOutputs,
                        address,
                        feeRate
                    )

                    return calculateFee(
                        unspentOutputs,
                        max,
                        feeRate,
                        address,
                        false
                    )
                } catch (e: Exception) {
                    return -1
                }
            } else {
                return -1
            }
        } catch(e: Exception) {
            return 0
        }
    }

    fun showWalletName() {
        _walletName.postValue(getWalletName())
    }

    fun showWalletNetwork() {
        _walletNetwork.postValue(getWalletNetwork())
    }

    fun showWalletDisplayUnit() {
        _walletDisplayUnit.postValue(localStoreRepository.getBitcoinDisplayUnit())
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

    protected fun generateNewWallet(
        passphrase: String,
        entropyStrength: Mnemonic.EntropyStrength
    ) {
        viewModelScope.launch {
//            val seed = Mnemonic().generate(entropyStrength)
            val seed = "patient sort can island cute saddle shield crunch knock tourist butter budget".split(" ")
            _seedPhraseGenerated.postValue(seed!!)
            _userPassphrase.postValue(passphrase)
        }
    }

    fun setWallet(uuid: String) {
        localWallet = walletManager.findLocalWallet(uuid)
    }

    fun isAddressValid(address: String): Boolean {
        return localWallet!!.walletKit!!.isAddressValid(address)
    }

    fun getWalletPassphrase() = walletManager.findStoredWallet(localWallet!!.uuid)!!.passphrase

    fun getWalletSeedPhrase() = walletManager.findStoredWallet(localWallet!!.uuid)!!.seedPhrase

    fun getDisplayUnit() = localStoreRepository.getBitcoinDisplayUnit()

    fun setDisplayUnit(displayUnit: BitcoinDisplayUnit) {
        localStoreRepository.updateBitcoinDisplayUnit(displayUnit)
    }

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

    open fun getWalletBalance() = localWallet!!.walletKit!!.balance.spendable

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