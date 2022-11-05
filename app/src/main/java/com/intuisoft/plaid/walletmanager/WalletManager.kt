package com.intuisoft.plaid.walletmanager

import android.app.Application
import com.docformative.docformative.remove
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.local.WipeDataListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.network.sync.repository.SyncRepository
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.entensions.splitIntoGroupOf
import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.models.BalanceInfo
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDExtendedKey
import io.horizontalsystems.hdwalletkit.HDExtendedKeyVersion
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class WalletManager(
    val application: Application,
    val localStoreRepository: LocalStoreRepository,
    val syncRepository: SyncRepository
): AbstractWalletManager(), WipeDataListener {
    private var _running = AtomicBoolean(false)
    private var running: Boolean
        get() = _running.get()
        set(value) {
            _running.set(value)
        }

    private var _syncing = AtomicBoolean(false)
    private var syncing: Boolean
        get() = _syncing.get()
        set(value) {
            _syncing.set(value)
            _fullySynced.postValue(!value)
        }

    private val _wallets: MutableList<LocalWalletModel> = mutableListOf()
    private val localPassphrases: MutableMap<String,String> = mutableMapOf()
    private var syncJobs: MutableList<Job> = mutableListOf()
    private var masterSyncJob: Job? = null
    private val mutex = Mutex()
    private var _baseMainNetWallet: BitcoinKit? = null
    private var _baseTestNetWallet: BitcoinKit? = null
    private var currentWallet: LocalWalletModel? = null
    private var stateListeners: MutableList<StateListener> = mutableListOf()

    open class BitcoinEventListener: BitcoinKit.Listener {}

    override fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            if(!running) {
                running = true

                _baseMainNetWallet = createBaseWallet(_baseMainNetWallet, BitcoinKit.NetworkType.MainNet)
                _baseTestNetWallet = createBaseWallet(_baseTestNetWallet, BitcoinKit.NetworkType.TestNet)
                localStoreRepository.setOnWipeDataListener(this@WalletManager)
                updateWallets()
                autoSync()
            }
        }
    }

    private fun createBaseWallet(baseWallet: BitcoinKit?, network: BitcoinKit.NetworkType): BitcoinKit {
        if(baseWallet == null) {
            var seed = localStoreRepository.getBaseWalletSeed()
            if(seed.isEmpty()) {
                seed = Mnemonic().generate(Mnemonic.EntropyStrength.VeryHigh)
                localStoreRepository.saveBaseWalletSeed(seed)
            }

            val base = BitcoinKit(
                context = application,
                words = seed,
                passphrase = "",
                walletId = Constants.Strings.BASE_WALLET,
                networkType = network,
                peerSize = Constants.Limit.MAX_PEERS,
                gapLimit = 50,
                syncMode = BitcoinCore.SyncMode.Api(),
                confirmationsThreshold = 1,
                purpose = HDWallet.Purpose.BIP44
            )

            base.refresh() // todo: check to see if we even need to start them?
            return base
        } else
            return baseWallet
    }

    override fun updateWalletName(localWallet: LocalWalletModel, newName: String) {
        findStoredWallet(localWallet.uuid)?.let {
            it.name = newName
            localWallet.name = newName
            localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
        }
    }

    override fun updateWalletSyncMode(localWallet: LocalWalletModel, apiSync: Boolean) {
        findStoredWallet(localWallet.uuid)?.let {
            it.apiSyncMode = apiSync
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
        return _baseMainNetWallet!!.isAddressValid(address) || _baseTestNetWallet!!.isAddressValid(address)
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
        if(!wallet.isSyncing) {
            if(wallet.isSynced) {
                onWalletStateUpdated(wallet)
            } else {
                wallet.walletKit!!.onEnterForeground()
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

   override suspend fun getWalletsAsync(): List<LocalWalletModel> {
       if(_wallets.isEmpty() &&localStoreRepository.getStoredWalletInfo().walletIdentifiers.isNotEmpty()) {
           updateWallets()
       }

       return _wallets
   }

    @Synchronized
   override fun getWallets(): List<LocalWalletModel> {
       return _wallets
   }

    private fun autoSync() {
        CoroutineScope(Dispatchers.IO).launch {
            while(true) {
                delay(Constants.Time.AUTO_SYNC_TIME)
                synchronizeAll()
            }
        }
    }

    override fun stop() {
        CoroutineScope(Dispatchers.IO).launch {
            cancelAllJobs()

            _wallets.forEach {
                it.walletKit?.stop()
            }

            _wallets.clear()
            running = false
        }
    }

    private suspend fun onWalletStateUpdated(wallet: LocalWalletModel) {
        mutex.withLock {
            stateListeners.forEach {
                CoroutineScope(Dispatchers.Main).launch {
                    it.onWalletStateUpdated(wallet)
                }
            }
        }
    }

    override suspend fun addWalletSyncListener(listener: StateListener) {
        mutex.withLock {
            if(stateListeners.find { it == listener } == null) {
                stateListeners.add(listener)
            }
        }
    }

    override suspend fun removeSyncListener(listener: StateListener) {
        mutex.withLock {
            stateListeners.remove { it == listener }
        }
    }

    override fun enterWallet(wallet: LocalWalletModel) {
        currentWallet = wallet
        wallet.walletKit!!.onEnterForeground()
    }

    override fun exitWallet() {
        if(currentWallet?.isSyncing != true)
            currentWallet?.walletKit?.onEnterBackground()
    }

    private suspend fun cancelAllJobs() {
        if(syncing) {
            masterSyncJob?.cancel()
            mutex.withLock {
                syncJobs.forEach {
                    it.cancel()
                }
                syncJobs.clear()
            }

            syncing = false
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

   @Synchronized
   override fun synchronizeAll() {
       if(!syncing && syncJobs.isEmpty()) {
           syncing = true

           _wallets.filter { findStoredWallet(it.uuid)!!.apiSyncMode }.splitIntoGroupOf(3).forEach { wallets ->
               syncJobs.add(
                   CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
                       var startTime = System.currentTimeMillis()
                       wallets.items.remove { it.uuid == currentWallet?.uuid }
                       wallets.items.forEach {
                           synchronize(it)
                       }

                       // wait for this group of wallets to sync
                       while(!wallets.items.all { it.isSynced }) {
                           delay(100)

                           if((System.currentTimeMillis() - startTime) >= Constants.Time.SYNC_TIMEOUT) {
                               with(wallets.items.iterator()) {
                                   forEach {
                                       if (it.syncPercentage == 0) { // restart stuck wallets
                                           it.walletKit!!.restart()
                                           startTime = System.currentTimeMillis()
                                       }
                                   }
                               }
                           }
                       }

                       wallets.items.forEach {
                          it.walletKit!!.onEnterBackground()
                       }
                   }
               )
           }

           _wallets.filter { !findStoredWallet(it.uuid)!!.apiSyncMode }.forEach {
               CoroutineScope(Dispatchers.IO).launch {
                   synchronize(it)
               }
           }

           masterSyncJob = CoroutineScope(Dispatchers.IO).launch {
               syncJobs.forEach {
                   it.start()
                   it.join()
               }

               mutex.withLock {
                   syncJobs.clear()
               }

               syncing = false
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

   override fun getBaseWallet(mainNet: Boolean) =
       if(mainNet)
           _baseMainNetWallet!!
       else
           _baseTestNetWallet!!

   override suspend fun createWallet(
       name: String,
       seed: List<String>,
       bip: HDWallet.Purpose,
       testnetWallet: Boolean
   ): String {
       val uuid = UUID.randomUUID().toString()

       saveWallet(
           WalletIdentifier(
               name,
               uuid,
               seed,
               "",
               mutableListOf(),
               bip.ordinal,
               testnetWallet,
               true,
               false
           )
       )
       return uuid
   }

   override suspend fun createWallet(
       name: String,
       pubKey: String
   ): String {
       val uuid = UUID.randomUUID().toString()

       var network = BitcoinKit.NetworkType.MainNet
       if(pubKey.startsWith(HDExtendedKeyVersion.tpub.base58Prefix)
           || pubKey.startsWith(HDExtendedKeyVersion.upub.base58Prefix)
           || pubKey.startsWith(HDExtendedKeyVersion.vpub.base58Prefix)) {
           network = BitcoinKit.NetworkType.TestNet
       }

       val temp = BitcoinKit(
           context = application,
           extendedKey = HDExtendedKey(pubKey),
           walletId = uuid,
           networkType = network,
           peerSize = Constants.Limit.MAX_PEERS,
           gapLimit = 50,
           syncMode = BitcoinCore.SyncMode.Api(),
           confirmationsThreshold = localStoreRepository.getMinimumConfirmations()
       )
       temp.stop() // just in case

       saveWallet(
           WalletIdentifier(
               name,
               uuid,
               listOf(),
               pubKey,
               mutableListOf(),
               temp.getPurpose().ordinal,
               network == BitcoinKit.NetworkType.TestNet,
               true,
               true
           )
       )

       return uuid
   }

   private fun updateWallets() {
       _wallets.forEach {
           it.walletKit?.stop()
       }

       _wallets.clear()
       localStoreRepository.getStoredWalletInfo().walletIdentifiers.forEach { identifier ->
           val passphrase = getWalletPassPhrase(identifier.walletUUID)
           val model = LocalWalletModel.consume(identifier, if(!identifier.readOnly) passphrase else "")

           // Store wallet hashes for passphrases
           if(identifier.walletHashIds?.find { it == model.hashId } == null) {
               identifier.walletHashIds!!.add(model.hashId)
               localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
           }

           if(identifier.readOnly) {
               model.walletKit = BitcoinKit(
                   context = application,
                   extendedKey = HDExtendedKey(identifier.pubKey),
                   walletId = model.hashId,
                   networkType = getWalletNetwork(model),
                   peerSize = Constants.Limit.MAX_PEERS,
                   gapLimit = 50,
                   syncMode = getWalletSyncMode(identifier.apiSyncMode),
                   confirmationsThreshold = localStoreRepository.getMinimumConfirmations()
               )
           } else {
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
                       purpose = HDWallet.Purpose.values().find { it.ordinal == identifier.bip }!!
                   )
           }


           model.walletKit!!.listener =
               object: BitcoinEventListener() {
                   override fun onBalanceUpdate(balance: BalanceInfo) {
                       super.onBalanceUpdate(balance)
                       _balanceUpdated.postValue(getTotalBalance())
                   }

                   override fun onKitStateUpdate(state: BitcoinCore.KitState) {
                       super.onKitStateUpdate(state)

                       when(state) {
                           is BitcoinCore.KitState.NotSynced,
                           is BitcoinCore.KitState.Synced -> {
                               CoroutineScope(Dispatchers.Main).launch {
                                   onWalletStateUpdated(model)
                                   _balanceUpdated.postValue(getTotalBalance())
                               }
                           }

                           else -> {
                               CoroutineScope(Dispatchers.Main).launch {
                                   onWalletStateUpdated(model)
                               }
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