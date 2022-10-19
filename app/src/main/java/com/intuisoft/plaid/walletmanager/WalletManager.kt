package com.intuisoft.plaid.walletmanager

import android.app.Application
import androidx.lifecycle.LiveData
import com.docformative.docformative.remove
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.local.WipeDataListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.model.WalletState
import com.intuisoft.plaid.network.sync.repository.SyncRepository
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.core.Bip
import io.horizontalsystems.bitcoincore.models.BalanceInfo
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoinkit.BitcoinKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class WalletManager(
    val application: Application,
    val localStoreRepository: LocalStoreRepository,
    val syncRepository: SyncRepository
): WipeDataListener {
    private var initialized = false
    private val _wallets: MutableList<LocalWalletModel> = mutableListOf()
    private val _stateChanged = SingleLiveData<ManagerState>()
    val stateChanged: LiveData<ManagerState> = _stateChanged

    var state = ManagerState.NONE
        private set(value) {
            field = value
            _stateChanged.postValue(field)
        }

    open class BitcoinEventListener: BitcoinKit.Listener {}

    fun initialize() {
        if(!initialized) {
            initialized = true

            CoroutineScope(Dispatchers.IO).launch {
                loadingScope {
                    localStoreRepository.setOnWipeDataListener(this@WalletManager)
                    updateWallets()
                }
            }
        }
    }

    private suspend fun loadingScope(scope: suspend () -> Unit) {
        var alreadySyncing = false
        if(state != ManagerState.SYNCHRONIZING) {
            state = ManagerState.SYNCHRONIZING
        } else alreadySyncing = true

        scope()

        if(!alreadySyncing)
            state = ManagerState.NONE
    }

    fun updateWalletName(localWallet: LocalWalletModel, newName: String) {
        val storedWallet = findStoredWallet(localWallet.uuid)

        storedWallet?.let {
            it.name = newName
            localWallet.name = newName
            localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
        }
    }

    fun validAddress(address: String) : Boolean {
        return _wallets.first().walletKit!!.isAddressValid(address)
    }

    fun arePeersReady() : Boolean {
        return _wallets.first().walletKit!!.arePeersReady()
    }

    suspend fun deleteWallet(localWallet: LocalWalletModel, onDeleteFinished: suspend () -> Unit) {
        localWallet.walletKit!!.stop()
        localStoreRepository.getStoredWalletInfo().walletIdentifiers.remove { it.walletUUID == localWallet.uuid }
        localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())


        BitcoinKit.clear(
            application,
            if(localWallet.testNetWallet)
                BitcoinKit.NetworkType.TestNet
            else BitcoinKit.NetworkType.MainNet,
            localWallet.name
        )

        _wallets.remove { it.uuid == localWallet.uuid }
        onDeleteFinished()
    }

    suspend fun synchronize(wallet: LocalWalletModel) {
       synchronize(wallet, false)
    }

    suspend fun synchronize(wallet: LocalWalletModel, forceSync: Boolean) {
        loadingScope {
            val time = System.currentTimeMillis() / 1000

            if(wallet.walletState == WalletState.NONE &&
                (
                   forceSync
                   || wallet.lastSyncedTime == 0
                   || (time - wallet.lastSyncedTime) > localStoreRepository.getWalletSyncTime()
                )
            ) {
                wallet.walletKit!!.refresh()
            }
        }
    }

   fun doesWalletExist(uuid: String): Boolean {
       localStoreRepository.getStoredWalletInfo()?.walletIdentifiers?.forEach {
           if(it.walletUUID == uuid)
               return true
       }

       return false
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

   override fun onWipeData() {
       localStoreRepository.setStoredWalletInfo(null)
       _wallets.forEach {
           it.walletKit?.stop()
           BitcoinKit.clear(
               application,
               if(it.testNetWallet)
                   BitcoinKit.NetworkType.TestNet
               else BitcoinKit.NetworkType.MainNet,
               it.name
           )
       }

       _wallets.clear()
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

    fun findLocalWallet(uuid: String): LocalWalletModel? =
        _wallets.find { it.uuid == uuid }

    fun findStoredWallet(uuid: String): WalletIdentifier? =
        localStoreRepository.getStoredWalletInfo().walletIdentifiers.find { it.walletUUID == uuid }

   fun addWalletIdentifier(wallet: WalletIdentifier) {
       localStoreRepository.getStoredWalletInfo().walletIdentifiers.add(wallet)
       localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
       updateWallets()
   }

   fun createWallet(
       name: String,
       seed: List<String>,
       passphrase: String,
       bip: Bip,
       testnetWallet: Boolean
   ): String {
       val uuid = UUID.randomUUID().toString()
       CoroutineScope(Dispatchers.IO).launch {
           loadingScope {
               addWalletIdentifier(
                   WalletIdentifier(
                       name,
                       uuid,
                       seed,
                       passphrase,
                       bip.ordinal,
                       testnetWallet
                   )
               )
           }
       }

       return uuid
   }

    private fun restart() {
        // todo: impl for when app needs a restart
    }

   private fun updateWallets() {
       _wallets.forEach {
           it.walletKit?.stop()
       }

       _wallets.clear()
       localStoreRepository.getStoredWalletInfo().walletIdentifiers.forEach { identifier ->
           val model = LocalWalletModel.consume(identifier)

           model.walletKit =
               BitcoinKit(
                   context = application,
                   words = identifier.seedPhrase,
                   passphrase = identifier.passphrase,
                   walletId = model.uuid,
                   networkType = getWalletNetwork(model),
                   peerSize = Constants.Limit.MAX_PEERS,
                   syncMode = BitcoinCore.SyncMode.Api(),
                   confirmationsThreshold = Constants.Limit.MIN_CONFIRMATIONS,
                   bip = Bip.values().find {  it.ordinal == identifier.bip }!!
               )

           model.walletKit!!.listener =
               object: BitcoinEventListener() {
                   override fun onBalanceUpdate(balance: BalanceInfo) {
                       super.onBalanceUpdate(balance)
                       CoroutineScope(Dispatchers.IO).launch {
                           synchronize(model, true)
                       }
                   }

                   override fun onKitStateUpdate(state: BitcoinCore.KitState) {
                       super.onKitStateUpdate(state)

                       when(state) {
                           is BitcoinCore.KitState.Synced,
                           is BitcoinCore.KitState.NotSynced -> {
                               updateWalletState(WalletState.NONE, model, -1)
//                               val trans = _wallets[0].walletKit!!.send("bc1qs49s2vlmgk7ptr9kwfpqytn4ve0hu0sxz2vvtu", 1000, true, 1, TransactionDataSortType.None)
//                               val r = 0
                           }

                           is BitcoinCore.KitState.Syncing -> {
                               updateWalletState(WalletState.SYNCING, model, (state.progress * 100).toInt())

                           }

                           is BitcoinCore.KitState.ApiSyncing -> {
                               updateWalletState(WalletState.SYNCING, model, -1)
                           }
                       }
                   }

                   override fun onTransactionsUpdate(
                       inserted: List<TransactionInfo>,
                       updated: List<TransactionInfo>
                   ) {
                       super.onTransactionsUpdate(inserted, updated)
                       // ignore
                   }
               }

           _wallets.add(model)
       }

       synchronizeAll()
   }

   private fun updateWalletState(state: WalletState, wallet: LocalWalletModel, syncPercentage: Int) {
       wallet.walletState = state
       wallet.walletStateUpdated.postValue(syncPercentage)
   }

   companion object {
       private val TAG = "WalletManager"

       fun getWalletNetwork(wallet: LocalWalletModel): BitcoinKit.NetworkType {
           if(wallet.testNetWallet)
               return BitcoinKit.NetworkType.TestNet
           else return BitcoinKit.NetworkType.MainNet
       }
   }
}