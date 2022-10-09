package com.intuisoft.plaid.walletmanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.intuisoft.emojiigame.framework.db.LocalWallet
import com.intuisoft.emojiigame.framework.db.PlaidDatabase
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.local.db.DatabaseListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.model.WalletState
import com.intuisoft.plaid.network.sync.repository.SyncRepository
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


class WalletManager(
    val application: Application,
    val localStoreRepository: LocalStoreRepository,
    val syncRepository: SyncRepository
): DatabaseListener {
    private var initialized = false
    private val _wallets: MutableList<LocalWalletModel> = mutableListOf()
    private val _stateChanged = SingleLiveData<ManagerState>()
    val stateChanged: LiveData<ManagerState> = _stateChanged

    var state = ManagerState.NONE
        private set(value) {
            field = value
            _stateChanged.postValue(field)
        }

    fun initialize() {
        if(!initialized) {
            initialized = true

            CoroutineScope(Dispatchers.IO).launch {


                loadingScope {
                    localStoreRepository.setDatabaseListener(this@WalletManager)
                    addDiscoveredWallets(localStoreRepository.getAllWallets())
                }
            }
        }
    }

    private suspend fun loadingScope(scope: suspend () -> Unit) {
        state = ManagerState.SYNCHRONIZING
        scope()
        state = ManagerState.NONE
    }

    // todo: add onDatabaseUpdated() function and add this manager as a listener to the database and if a user deletes a wallet, adds a wallet or clears the db it will reset the ;local wallet list
    private suspend fun synchronize(wallet: LocalWalletModel, forceSync: Boolean) {
        loadingScope {
            val time = System.currentTimeMillis() / 1000

            if(wallet.walletState == WalletState.NONE &&
                (
                   forceSync
                   || wallet.lastSyncedTime == 0
                   || (time - wallet.lastSyncedTime) > localStoreRepository.getWalletSyncTime()
                )
            ) {
//                val pubKey = wallet.wallet?.watchingKey
//                updateWalletState(WalletState.SYNCING, wallet)
//                wallet.lastSyncedTime = time.toInt()
//                delay(1000)
//
//                val response = syncRepository.syncHDWallet("xpub6CUGRUonZSQ4TWtTMmzXdrXDtypWKiKrhko4egpiMZbpiaQL2jkwSB1icqYh2cfDfVxdx4df189oLKnC5fSwqPfgyP3hooxujYzAu3fDVmz")
//                val tx = Transaction(MainNetParams.get())
//
//                val target1 = wallet.wallet!!.freshReceiveAddress()
//                val target2 = wallet.wallet!!.freshReceiveAddress()
//
//                tx.addOutput(Coin.valueOf(322), target1);
//                tx.addInput(Coin.valueOf( 20), target2);
//                wallet.wallet!!.receivePending()
//                val sendRequest = SendRequest.forTx(tx)
//                wallet.wallet!!.signTransaction(sendRequest)
//
//                // todo make servercall to exxteral API to pull down transactions
//
//                wallet.wallet?.clearTransactions(0);
                // todo: add recieved transactions to wallet
                // todo: call callback for wallet
                updateWalletState(WalletState.NONE, wallet)
            }
        }
    }

    override fun onDatabaseUpdated() {
        CoroutineScope(Dispatchers.IO).launch {
            waitForSynchronization()

            loadingScope {
                val dbWallets = localStoreRepository.getAllWallets()

                dbWallets?.let {
                    removeUnusedWallets(it)
                    _wallets.clear()
                    addDiscoveredWallets(it)
                }
            }
        }
    }

    suspend fun getWallets(): List<LocalWalletModel> {
        waitForSynchronization()
        return _wallets
    }

    private suspend fun waitForSynchronization() {
        while(state == ManagerState.SYNCHRONIZING) {
            delay(1)
        }
    }

    fun synchronizeAll() {
        CoroutineScope(Dispatchers.IO).launch {
            if(state == ManagerState.SYNCHRONIZING)
                return@launch

            loadingScope {
                _wallets.forEach {
                    synchronize(it, true)
                }
            }
        }
    }

    private fun removeUnusedWallets(newWalletsList: List<LocalWallet>) {
        _wallets.forEach { localWallet ->
            if (newWalletsList.find { it.name == localWallet.name } == null) {
                getWalletFile(application, localWallet.name).delete()
            }
        }
    }

    private fun addDiscoveredWallets(newWalletsList: List<LocalWallet>?) {
        newWalletsList?.forEach {
            val model = PlaidDatabase.fromDb(it)
//            model.wallet = Wallet.loadFromFile(
//                getWalletFile(application, it.name)
//            )

            _wallets.add(model)
        }
    }

    private fun updateWalletState(state: WalletState, wallet: LocalWalletModel) {
        wallet.walletState = state
        wallet.walletStateUpdated.postValue(Unit)
    }

    companion object {
        private val TAG = "WalletManager"

        fun getWalletFile(context: Context, name: String) : File {
            return File(
                context.filesDir,
                Constants.Strings.USER_WALLET_FILENAME_PREFIX + name
            )
        }
    }
}