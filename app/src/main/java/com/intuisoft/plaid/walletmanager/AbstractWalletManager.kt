package com.intuisoft.plaid.walletmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.local.WipeDataListener
import com.intuisoft.plaid.model.LocalWalletModel
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDWallet

abstract class AbstractWalletManager {
    protected val _stateChanged = SingleLiveData<ManagerState>()
    val stateChanged: LiveData<ManagerState> = _stateChanged

    protected val _balanceUpdated = MutableLiveData<Long>()
    val balanceUpdated: LiveData<Long> = _balanceUpdated

    var state = ManagerState.NONE
        protected set(value) {
            field = value
            _stateChanged.postValue(field)
        }

    abstract fun start()
    abstract fun stop()
    abstract fun updateWalletName(localWallet: LocalWalletModel, newName: String)
    abstract fun validAddress(address: String) : Boolean
    abstract fun arePeersReady() : Boolean
    abstract suspend fun deleteWallet(localWallet: LocalWalletModel, onDeleteFinished: suspend () -> Unit)
    abstract suspend fun synchronize(wallet: LocalWalletModel)
    abstract fun doesWalletExist(uuid: String): Boolean
    abstract suspend fun getWallets(): List<LocalWalletModel>
    abstract fun synchronizeAll()
    abstract fun findLocalWallet(uuid: String): LocalWalletModel?
    abstract fun findStoredWallet(uuid: String): WalletIdentifier?
    abstract fun createWallet(name: String, seed: List<String>, bip: HDWallet.Purpose, testnetWallet: Boolean): String
    abstract fun setWalletPassphrase(localWallet: LocalWalletModel, passphrase: String)
    abstract fun getWalletPassphrase(localWallet: LocalWalletModel): String
    abstract fun getBaseWallet(): BitcoinKit
}