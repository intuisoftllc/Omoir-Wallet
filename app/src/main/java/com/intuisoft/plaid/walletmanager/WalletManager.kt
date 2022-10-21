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
import kotlin.collections.HashMap


class WalletManager(
    val application: Application,
    val localStoreRepository: LocalStoreRepository,
    val syncRepository: SyncRepository
): AbstractWalletManager(), WipeDataListener {
    protected var running = false
    protected val _wallets: MutableList<LocalWalletModel> = mutableListOf()
    protected val localPassphrases: MutableMap<String,String> = mutableMapOf()

    open class BitcoinEventListener: BitcoinKit.Listener {}

    override fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            if(!running) {
                running = true

                loadingScope {
                    localStoreRepository.setOnWipeDataListener(this@WalletManager)
                    updateWallets()
                }
            }
        }
    }

    protected suspend fun loadingScope(scope: suspend () -> Unit) {
        var alreadySyncing = false
        if(state != ManagerState.SYNCHRONIZING) {
            state = ManagerState.SYNCHRONIZING
        } else alreadySyncing = true

        scope()

        if(!alreadySyncing)
            state = ManagerState.NONE
    }

    override fun updateWalletName(localWallet: LocalWalletModel, newName: String) {
        findStoredWallet(localWallet.uuid)?.let {
            it.name = newName
            localWallet.name = newName
            localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
        }
    }

    private fun getWalletPassPhrase(walletId: String): String {
        return localPassphrases.get(walletId) ?: ""
    }

    private fun setWalletPassphrase(walletId: String, passphrase: String) {
        localPassphrases.put(walletId, passphrase)
    }

    override fun getWalletPassphrase(localWallet: LocalWalletModel): String {
        return getWalletPassPhrase(localWallet.uuid)
    }

    override fun setWalletPassphrase(localWallet: LocalWalletModel, passphrase: String) {
        setWalletPassphrase(localWallet.uuid, passphrase)
    }

    override fun validAddress(address: String) : Boolean {
        return _wallets.first().walletKit!!.isAddressValid(address)
    }

    override fun arePeersReady() : Boolean {
        return _wallets.first().walletKit!!.arePeersReady()
    }

    private fun getTotalBalance(): Long {
        var balance : Long = 0

        _wallets.forEach {
            balance += (it.walletKit?.balance?.spendable ?: 0L)
        }

        return balance
    }

    override suspend fun deleteWallet(
        localWallet: LocalWalletModel,
        onDeleteFinished: suspend () -> Unit
    ) {
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

        localPassphrases.remove(localWallet.uuid)
        _wallets.remove { it.uuid == localWallet.uuid }
        onDeleteFinished()
    }

    override suspend fun synchronize(wallet: LocalWalletModel) {
        loadingScope {
            if(wallet.walletState == WalletState.NONE) {
                wallet.walletKit!!.refresh()
            }
        }
    }

   override fun doesWalletExist(uuid: String): Boolean {
       localStoreRepository.getStoredWalletInfo().walletIdentifiers.forEach {
           if(it.walletUUID == uuid)
               return true
       }

       return false
   }

   override suspend fun getWallets(): List<LocalWalletModel> {
       waitForSynchronization()
       return _wallets
   }

   private suspend fun waitForSynchronization() {
       while(state == ManagerState.SYNCHRONIZING) {
           delay(1)
       }
   }

    override fun stop() {
        CoroutineScope(Dispatchers.IO).launch {
            loadingScope {
                _wallets.forEach {
                    it.walletKit?.stop()
                }

                _wallets.clear()
                running = false
            }
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
       localPassphrases.clear()
       _balanceUpdated.postValue(0)
   }

   override fun synchronizeAll() {
       CoroutineScope(Dispatchers.IO).launch {
           if(state == ManagerState.SYNCHRONIZING)
               return@launch

           loadingScope {
               _wallets.forEach {
                   synchronize(it)
               }
           }
       }
   }

    override fun findLocalWallet(hashId: String): LocalWalletModel? =
        _wallets.find { it.hashId == hashId }

    override fun findStoredWallet(uuid: String): WalletIdentifier? =
        localStoreRepository.getStoredWalletInfo().walletIdentifiers.find { it.walletUUID == uuid }

   private fun saveWallet(wallet: WalletIdentifier) {
       localStoreRepository.getStoredWalletInfo().walletIdentifiers.add(wallet)
       localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
       updateWallets()
   }

   override fun createWallet(
       name: String,
       seed: List<String>,
       bip: Bip,
       testnetWallet: Boolean
   ): String {
       val uuid = UUID.randomUUID().toString()
       CoroutineScope(Dispatchers.IO).launch {
           loadingScope {
               saveWallet(
                   WalletIdentifier(
                       name,
                       uuid,
                       seed,
                       bip.ordinal,
                       testnetWallet,
                       true
                   )
               )
           }
       }

       return uuid
   }

   private fun updateWallets() {
       _wallets.forEach {
           it.walletKit?.stop()
       }

       _wallets.clear()
       localStoreRepository.getStoredWalletInfo().walletIdentifiers.forEach { identifier ->
           val passphrase = getWalletPassPhrase(identifier.walletUUID)
           val model = LocalWalletModel.consume(identifier, passphrase)

           model.walletKit =
               BitcoinKit(
                   context = application,
                   words = identifier.seedPhrase,
                   passphrase = passphrase,
                   walletId = model.hashId,
                   networkType = getWalletNetwork(model),
                   peerSize = Constants.Limit.MAX_PEERS,
                   gapLimit = 50,
                   syncMode = getWalletSyncMode(identifier.apiSyncMode),
                   confirmationsThreshold = Constants.Limit.MIN_CONFIRMATIONS,
                   bip = Bip.values().find {  it.ordinal == identifier.bip }!!
               )

           model.walletKit!!.listener =
               object: BitcoinEventListener() {
                   override fun onBalanceUpdate(balance: BalanceInfo) {
                       super.onBalanceUpdate(balance)
                       CoroutineScope(Dispatchers.IO).launch {
                           synchronize(model)
                       }
                   }

                   override fun onKitStateUpdate(state: BitcoinCore.KitState) {
                       super.onKitStateUpdate(state)

                       when(state) {
                           is BitcoinCore.KitState.Synced,
                           is BitcoinCore.KitState.NotSynced -> {
                               updateWalletState(WalletState.NONE, model, -1)
                               _balanceUpdated.postValue(getTotalBalance())
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

       fun getWalletSyncMode(apiSync: Boolean): BitcoinCore.SyncMode {
           if(apiSync)
               return BitcoinCore.SyncMode.Api()
           else return BitcoinCore.SyncMode.Full()
       }
   }
}