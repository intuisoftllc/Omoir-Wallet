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
import io.horizontalsystems.bitcoincore.models.BalanceInfo
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDWallet
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
    private var _baseWallet: BitcoinKit? = null

    open class BitcoinEventListener: BitcoinKit.Listener {}

    override fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            if(!running) {
                running = true

                loadingScope {
                    if(_baseWallet == null) {
                        _baseWallet = BitcoinKit(
                            context = application,
                            words = Constants.Strings.TEST_WALLET_1.split(" "),
                            passphrase = "",
                            walletId = Constants.Strings.BASE_WALLET,
                            networkType = BitcoinKit.NetworkType.MainNet,
                            peerSize = Constants.Limit.MAX_PEERS,
                            gapLimit = 50,
                            syncMode = BitcoinCore.SyncMode.Api(),
                            confirmationsThreshold = 1,
                            purpose = HDWallet.Purpose.BIP44
                        )

                        _baseWallet!!.refresh()
                    }

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

    private fun deleteWalletFromDatabase(localWallet: LocalWalletModel) {
        localWallet.walletKit?.stop()

        findStoredWallet(localWallet.uuid)?.let { walletIdentifier ->
            walletIdentifier.walletHashIds?.forEach { hashId ->
                BitcoinKit.clear(
                    application,
                    if(walletIdentifier.isTestNet)
                        BitcoinKit.NetworkType.TestNet
                    else BitcoinKit.NetworkType.MainNet,
                    hashId
                )
            }
        }
    }

    override suspend fun deleteWallet(
        localWallet: LocalWalletModel,
        onDeleteFinished: suspend () -> Unit
    ) {
        deleteWalletFromDatabase(localWallet)
        localStoreRepository.getStoredWalletInfo().walletIdentifiers.remove { it.walletUUID == localWallet.uuid }
        localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())

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
       _wallets.forEach {
           deleteWalletFromDatabase(it)
       }

       _wallets.clear()
       localPassphrases.clear()
       localStoreRepository.setStoredWalletInfo(null)
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

               _balanceUpdated.postValue(getTotalBalance())
           }
       }
   }

    override fun findLocalWallet(uuid: String): LocalWalletModel? =
        _wallets.find { it.uuid == uuid }

    override fun findStoredWallet(uuid: String): WalletIdentifier? =
        localStoreRepository.getStoredWalletInfo().walletIdentifiers.find { it.walletUUID == uuid }

   private fun saveWallet(wallet: WalletIdentifier) {
       localStoreRepository.getStoredWalletInfo().walletIdentifiers.add(wallet)
       localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
       updateWallets()
   }

   override fun getBaseWallet() = _baseWallet!!

   override fun createWallet(
       name: String,
       seed: List<String>,
       bip: HDWallet.Purpose,
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
                       mutableListOf(),
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

           // Store wallet hashes for passphrases
           if(identifier.walletHashIds?.find { it == model.hashId } == null) {
               identifier.walletHashIds!!.add(model.hashId)
               localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
           }

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
                   confirmationsThreshold = localStoreRepository.getMinimumConfirmations(),
                   purpose = HDWallet.Purpose.values().find {  it.ordinal == identifier.bip }!!
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