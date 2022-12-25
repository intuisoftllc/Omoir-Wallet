package com.intuisoft.plaid.walletmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intuisoft.plaid.common.model.HiddenWalletModel
import com.intuisoft.plaid.common.model.SavedAccountModel
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import io.horizontalsystems.bitcoincore.models.BitcoinPaymentData
import io.horizontalsystems.bitcoincore.models.PublicKey
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDWallet

abstract class AbstractWalletManager {

    protected val _balanceUpdated = MutableLiveData<Long>()
    val balanceUpdated: LiveData<Long> = _balanceUpdated

    protected val _onSyncing = MutableLiveData<Boolean>()
    val onSyncing: LiveData<Boolean> = _onSyncing

    protected val _wallets = MutableLiveData<List<LocalWalletModel>>()
    val wallets: LiveData<List<LocalWalletModel>> = _wallets

    protected val _databaseUpdated = MutableLiveData<Any?>()
    val databaseUpdated: LiveData<Any?> = _databaseUpdated

    abstract fun start()
    abstract suspend fun stop()
    abstract fun updateWalletName(localWallet: LocalWalletModel, newName: String)
    abstract fun validAddress(address: String) : Boolean
    abstract fun parseInvoice(invoiceData: String) : BitcoinPaymentData
    abstract fun canSendTransaction(localWallet: LocalWalletModel) : Boolean
    abstract suspend fun deleteWallet(localWallet: LocalWalletModel, onDeleteFinished: suspend () -> Unit)
    abstract fun synchronize(wallet: LocalWalletModel)
    abstract fun getWallets(): List<LocalWalletModel>
    abstract fun synchronizeAll(force: Boolean)
    abstract fun cancelTransfer(id: String)
    abstract fun setInitialHiddenWallets(storedPassphrases: MutableMap<String, HiddenWalletModel?>)
    abstract fun getHiddenWallets(): MutableMap<String, HiddenWalletModel?>
    abstract suspend fun addWalletSyncListener(listener: StateListener)
    abstract suspend fun removeSyncListener(listener: StateListener)
    abstract fun findLocalWallet(uuid: String): LocalWalletModel?
    abstract fun findStoredWallet(uuid: String): WalletIdentifier?
    abstract suspend fun createWallet(name: String, seed: List<String>, bip: HDWallet.Purpose, testnetWallet: Boolean): String
    abstract suspend fun createWallet(name: String, pubKey: String): String
    abstract fun setCurrentHiddenWallet(localWallet: LocalWalletModel, passphrase: String, account: SavedAccountModel)
    abstract fun getCurrentHiddenWallet(localWallet: LocalWalletModel): HiddenWalletModel?
    abstract fun getBaseWallet(mainNet: Boolean = true): BitcoinKit
    abstract fun openWallet(wallet: LocalWalletModel)
    abstract fun closeWallet()
    abstract fun getWalletCount(): Int
    abstract fun getHiddenWalletCount(wallet: LocalWalletModel): Int
    abstract fun getFullPublicKeyPath(key: PublicKey): String
    abstract fun requiresNewHiddenWallet(wallet: LocalWalletModel, passphrase: String, account: SavedAccountModel): Boolean
    abstract fun getOpenedWallet(): LocalWalletModel
}