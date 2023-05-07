package com.intuisoft.plaid.common.delegates.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intuisoft.plaid.common.model.HiddenWalletModel
import com.intuisoft.plaid.common.model.SavedAccountModel
import com.intuisoft.plaid.common.model.WalletIdentifier

abstract class WalletDelegate {
    // new
    abstract fun start()
    abstract suspend fun stop()
    abstract fun getCurrentHiddenWallet(localWallet: GenericWalletModel): HiddenWalletModel? 
    abstract fun createWalletModel(walletIdentifier: WalletIdentifier, hiddenWallet: HiddenWalletModel?): GenericWalletModel
    abstract fun getOpenedWallet(): GenericWalletModel

    protected val _balanceUpdated = MutableLiveData<Double>()
    val balanceUpdated: LiveData<Double> = _balanceUpdated

    protected val _onSyncing = MutableLiveData<Boolean>()
    val onSyncing: LiveData<Boolean> = _onSyncing

    protected val _wallets = MutableLiveData<List<GenericWalletModel>>()
    val wallets: LiveData<List<GenericWalletModel>> = _wallets

    protected val _databaseUpdated = MutableLiveData<Any?>()
    val databaseUpdated: LiveData<Any?> = _databaseUpdated

    // old




    abstract fun isRunning(): Boolean
    abstract fun updateWalletName(localWallet: LocalWalletModel, newName: String)
    abstract fun validAddress(address: String) : Boolean
    abstract fun validPubPrivKey(key: String) : Boolean
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
    abstract fun getBaseWallet(mainNet: Boolean = true): BitcoinKit
    abstract fun openWallet(wallet: LocalWalletModel)
    abstract fun closeWallet()
    abstract fun getWalletCount(): Int
    abstract fun getHiddenWalletCount(wallet: LocalWalletModel): Int
    abstract fun getFullPublicKeyPath(key: PublicKey): String
    abstract fun requiresNewHiddenWallet(wallet: LocalWalletModel, passphrase: String, account: SavedAccountModel): Boolean
}