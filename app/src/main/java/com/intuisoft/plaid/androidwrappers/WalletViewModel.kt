package com.intuisoft.plaid.androidwrappers

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.model.TransactionMemoModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.managers.SendValueErrors
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDExtendedKey
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*


open class WalletViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val apiRepository: ApiRepository,
    private val walletManager: AbstractWalletManager
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

    protected val _readOnlyWallet = SingleLiveData<Unit>()
    val readOnlyWallet: LiveData<Unit> = _readOnlyWallet

    protected val _walletName = SingleLiveData<String>()
    val walletName: LiveData<String> = _walletName

    protected val _walletNetwork = SingleLiveData<BitcoinKit.NetworkType>()
    val walletNetwork: LiveData<BitcoinKit.NetworkType> = _walletNetwork

    protected val _walletBalance = SingleLiveData<String>()
    val walletBalance: LiveData<String> = _walletBalance

    protected val _walletDisplayUnit = SingleLiveData<BitcoinDisplayUnit>()
    val walletDisplayUnit: LiveData<BitcoinDisplayUnit> = _walletDisplayUnit

    protected val _walletBip = SingleLiveData<HDWallet.Purpose>()
    val walletBip: LiveData<HDWallet.Purpose> = _walletBip

    protected val _txMemo = SingleLiveData<String>()
    val txMemo: LiveData<String> = _txMemo

    protected val _onEditMemo = SingleLiveData<TransactionMemoModel>()
    val onUpdateMemo: LiveData<TransactionMemoModel> = _onEditMemo

    protected val localWallet: LocalWalletModel?
        get() = walletManager.getOpenedWallet()

    var selectedUTXOs: MutableList<UnspentOutput> = mutableListOf()

    private val disposables = CompositeDisposable()

    fun showWalletBalance(context: Context) {
        _walletBalance.postValue(localWallet!!.onWalletStateChanged(context, 0, false, localStoreRepository))
    }

    fun setWalletPassphrase(passphrase: String) {
        walletManager.setWalletPassphrase(localWallet!!, passphrase)
    }

    fun isWalletSyncing() = localWallet!!.isSyncing

    fun getConfirmations(transaction: TransactionInfo) : Int {
        val currentBlock = getCurrentBlock()

        if(currentBlock == 0 || transaction.blockHeight == null
            || (currentBlock - transaction.blockHeight!!) < 0) {
            return 0
        } else return (currentBlock - transaction.blockHeight!!) + 1
    }

    fun getWalletName(id: String): String {
        val wallet = walletManager.getWallets().find {
            it.uuid == id
        }

        return if(wallet != null) {
            wallet.name
        } else {
            id
        }
    }

    fun getCurrentBlock() = localWallet!!.walletKit!!.lastBlockInfo?.height ?: 0

    fun getUnspentOutputs() = localWallet!!.getWhitelistedUtxos(localStoreRepository)

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

    suspend fun getSuggestedFees(testnetWallet: Boolean) =
        apiRepository.getSuggestedFeeRate(testnetWallet)

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

    fun getMemoForTx(txId: String) {
        viewModelScope.launch {
            _txMemo.postValue(localStoreRepository.getTransactionMemo(txId)?.memo ?: getApplication<PlaidApp>().getString(R.string.not_applicable))
        }
    }

    fun setMemoForTx(txId: String, memo: String) {
        viewModelScope.launch {
            localStoreRepository.setTransactionMemo(txId, memo) // todo: add wallet uuid to delete entries once wallet is deleted
            _txMemo.postValue(memo)
        }
    }

    fun onMemoEdit(txId: String) {
        viewModelScope.launch {
            _onEditMemo.postValue(localStoreRepository.getTransactionMemo(txId) ?: TransactionMemoModel(txId, ""))
        }
    }

    fun canTransferToWallet(recepient: LocalWalletModel): Boolean {
        return recepient.testNetWallet == localWallet!!.testNetWallet
                && recepient.uuid != localWallet!!.uuid // ensure same network transferability
    }

    fun getWallets() = walletManager.getWallets()

    fun showWalletName() {
        _walletName.postValue(getWalletName())
    }

    fun showWalletNetwork() {
        _walletNetwork.postValue(getWalletNetwork())
    }

    fun refreshLocalCache() {
        CoroutineScope(Dispatchers.IO).launch {
            // go to network and get fee rates if we have passed the cache time
            apiRepository.refreshLocalCache()
        }
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

    fun checkReadOnlyStatus() {
        if(isReadOnly()) _readOnlyWallet.postValue(Unit)
    }

    fun displayCurrentWallet() {
        localWallet?.let {
            _displayWallet.postValue(it)
        }
    }

    protected fun generateNewWallet(
        entropyStrength: Mnemonic.EntropyStrength
    ) {
        viewModelScope.launch {
            _seedPhraseGenerated.postValue(Mnemonic().generate(entropyStrength))
        }
    }

    fun checkOpenedWallet(fragment: Fragment) {
        if(localWallet == null) {
            softRestart(fragment)
        }
    }

    fun getWallet() = localWallet

    fun isAddressValid(address: String): Boolean {
        if(localWallet != null) {
            return localWallet!!.walletKit!!.isAddressValid(address)
        } else {
            return walletManager.validAddress(address)
        }
    }

    fun isPublicKeyAddressValid(address: String) : Boolean {
        try {
            HDExtendedKey.validate(address, true)
            return true
        } catch (e: Throwable) {
            return false
        }
    }

    fun getWalletPassphrase() = walletManager.getWalletPassphrase(localWallet!!)

    fun getWalletSeedPhrase() = walletManager.findStoredWallet(localWallet!!.uuid)!!.seedPhrase

    fun getDisplayUnit() = localStoreRepository.getBitcoinDisplayUnit()

    fun setDisplayUnit(displayUnit: BitcoinDisplayUnit) {
        localStoreRepository.updateBitcoinDisplayUnit(displayUnit)
    }

    fun isReadOnly() = localWallet!!.walletKit!!.watchAccount

    fun getMasterPublicKey() : String {
        if(localWallet!!.walletKit!!.watchAccount) {
            return walletManager.findStoredWallet(localWallet!!.uuid)!!.pubKey
        } else {
            return localWallet!!.walletKit!!.getMasterPublicKey(!localWallet!!.testNetWallet)
        }
    }

    fun getRecieveAddress() : String {
        return localWallet!!.walletKit!!.receiveAddress()
    }

    fun getWalletNetwork() =
        if(walletManager.findStoredWallet(localWallet!!.uuid)!!.isTestNet)
            BitcoinKit.NetworkType.TestNet
        else BitcoinKit.NetworkType.MainNet

    fun isTestNetWallet() = walletManager.findStoredWallet(localWallet!!.uuid)!!.isTestNet

    fun getWalletName() = localWallet!!.name

    fun getWalletId() = localWallet!!.uuid

    open fun getWalletBalance() = localWallet!!.getWhitelistedBalance(localStoreRepository)


    open fun setInitialSpendUtxo(utxo: String) {
        selectedUTXOs = mutableListOf(getUnspentOutputs().find { it.output.address == utxo }!!)
    }

    open fun updateUTXOs(utxos: MutableList<UnspentOutput>) {
        selectedUTXOs = utxos
    }

    open fun addSingleUTXO(utxo: UnspentOutput) {
        if(selectedUTXOs.find { it == utxo } == null) {
            selectedUTXOs.add(utxo)
        }
    }

    open fun getMaxSpend() : RateConverter {
        val rate = RateConverter(localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0)
        rate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, getAdjustedWalletBalance().toDouble())
        return rate
    }

    fun getAdjustedWalletBalance() : Long {
        if(selectedUTXOs.isEmpty())
            return localWallet!!.getWhitelistedBalance(localStoreRepository)
        else {
            var balance = 0L
            selectedUTXOs.forEach {
                balance += it.output.value
            }

            return balance
        }
    }

    fun getWalletBip(): HDWallet.Purpose {
        val bip = walletManager.findStoredWallet(localWallet!!.uuid)!!.bip
        return HDWallet.Purpose.values().find { it.ordinal == bip }!!
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
        localWallet?.let {
            walletManager.synchronize(it)
        }
    }

    protected fun commitWalletToDisk(
        walletName: String,
        seed: List<String>,
        bip: HDWallet.Purpose,
        testNetWallet: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (doesWalletExist(walletName)) {
                _walletCreationError.postValue(Unit)
            } else {
                val walletId = walletManager.createWallet(
                    name = walletName,
                    seed = seed!!,
                    bip = bip,
                    testnetWallet = testNetWallet
                )

                _walletCreated.postValue(walletId)
            }
        }
    }

    fun addWalletStateListener(listener: StateListener) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                walletManager.addWalletSyncListener(listener)
            }
        }
    }

    fun removeWalletSyncListener(listener: StateListener) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                walletManager.removeSyncListener(listener)
            }
        }
    }

    protected fun commitWalletToDisk(
        walletName: String,
        pubKey: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (doesWalletExist(walletName)) {
                _walletCreationError.postValue(Unit)
            } else {
                val walletId = walletManager.createWallet(
                    name = walletName,
                    pubKey = pubKey
                )

                _walletCreated.postValue(walletId)
            }
        }
    }


    fun deleteWallet(onDeleteFinished: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
          walletManager.deleteWallet(localWallet!!) {
              withContext(Dispatchers.Main) {
                  onDeleteFinished()
              }
          }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    suspend fun doesWalletExist(walletName: String) : Boolean {
        return walletManager.doesWalletExist(walletName)
    }
}