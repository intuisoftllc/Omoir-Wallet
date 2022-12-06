package com.intuisoft.plaid.walletmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import io.horizontalsystems.bitcoincore.models.BitcoinPaymentData
import io.horizontalsystems.bitcoincore.models.PublicKey
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDWallet
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractWalletManager {

    protected val _balanceUpdated = MutableLiveData<Long>()
    val balanceUpdated: LiveData<Long> = _balanceUpdated

    protected val _onSyncing = MutableLiveData<Boolean>()
    val onSyncing: LiveData<Boolean> = _onSyncing

    protected val _wallets = MutableLiveData<List<LocalWalletModel>>()
    val wallets: LiveData<List<LocalWalletModel>> = _wallets

    abstract fun start()
    abstract fun stop()
    abstract fun updateWalletName(localWallet: LocalWalletModel, newName: String)
    abstract fun validAddress(address: String) : Boolean
    abstract fun parseInvoice(invoiceData: String) : BitcoinPaymentData
    abstract fun arePeersReady(localWallet: LocalWalletModel) : Boolean
    abstract suspend fun deleteWallet(localWallet: LocalWalletModel, onDeleteFinished: suspend () -> Unit)
    abstract fun synchronize(wallet: LocalWalletModel)
    abstract fun doesWalletExist(uuid: String): Boolean
    abstract fun getWallets(): List<LocalWalletModel>
    abstract fun synchronizeAll()
    abstract suspend fun addWalletSyncListener(listener: StateListener)
    abstract suspend fun removeSyncListener(listener: StateListener)
    abstract fun findLocalWallet(uuid: String): LocalWalletModel?
    abstract fun findStoredWallet(uuid: String): WalletIdentifier?
    abstract suspend fun createWallet(name: String, seed: List<String>, bip: HDWallet.Purpose, testnetWallet: Boolean): String
    abstract suspend fun createWallet(name: String, pubKey: String): String
    abstract fun setWalletPassphrase(localWallet: LocalWalletModel, passphrase: String)
    abstract fun getWalletPassphrase(localWallet: LocalWalletModel): String
    abstract fun getBaseWallet(mainNet: Boolean = true): BitcoinKit
    abstract fun openWallet(wallet: LocalWalletModel)
    abstract fun closeWallet()
    abstract fun getFullPublicKeyPath(key: PublicKey): String
    abstract fun getOpenedWallet(): LocalWalletModel?
}