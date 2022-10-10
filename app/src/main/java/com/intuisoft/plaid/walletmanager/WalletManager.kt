package com.intuisoft.plaid.walletmanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.local.WipeDataListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.model.WalletState
import com.intuisoft.plaid.model.WalletType
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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec


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
        state = ManagerState.SYNCHRONIZING
        scope()
        state = ManagerState.NONE
    }

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
                wallet.walletKit!!.refresh()
            }
        }
    }

   fun doesWalletExist(name: String): Boolean {
       localStoreRepository.getStoredWalletInfo()?.walletIdentifiers?.forEach {
           if(it.name == name)
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

    fun findWallet(name: String): LocalWalletModel? =
        _wallets.find { it.name == name }

   fun addWalletIdentifier(wallet: WalletIdentifier) {
       localStoreRepository.getStoredWalletInfo().walletIdentifiers.add(wallet)
       localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
       updateWallets()
   }

   fun createWallet(
       name: String,
       seed: List<String>,
       passphrase: String,
       walletType: WalletType,
       testnetWallet: Boolean
   ) {
       CoroutineScope(Dispatchers.IO).launch {
           loadingScope {
               addWalletIdentifier(
                   WalletIdentifier(
                       name,
                       seed,
                       passphrase,
                       walletType.value,
                       testnetWallet
                   )
               )
           }
       }
   }

   private fun updateWallets() {
       _wallets.clear()

       localStoreRepository.getStoredWalletInfo().walletIdentifiers.forEach { identifier ->
           val model = LocalWalletModel.consume(identifier)

           model.walletKit =
               BitcoinKit(
                   context = application,
                   words = identifier.seedPhrase,
                   passphrase = identifier.passphrase,
                   walletId = model.name,
                   networkType = getWalletNetwork(model),
                   peerSize = Constants.Limit.MAX_PEERS,
                   syncMode = BitcoinCore.SyncMode.Api(),
                   confirmationsThreshold = 3, // todo: make this configurable
                   bip = Bip.BIP44
               )

           model.walletKit!!.listener =
               object: BitcoinEventListener() {
                   override fun onBalanceUpdate(balance: BalanceInfo) {
                       super.onBalanceUpdate(balance)
                       val fd = 0
                       // todo update wallet state
                   }

                   override fun onKitStateUpdate(state: BitcoinCore.KitState) {
                       super.onKitStateUpdate(state)

                       when(state) {
                           is BitcoinCore.KitState.Synced,
                           is BitcoinCore.KitState.NotSynced -> {
                               updateWalletState(WalletState.NONE, model, -1)
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
                       val f = 0
                   }
               }

           _wallets.add(model)
       }
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