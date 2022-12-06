package com.intuisoft.plaid.walletmanager

import android.app.Application
import com.intuisoft.plaid.common.listeners.WipeDataListener
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.models.BalanceInfo
import io.horizontalsystems.bitcoincore.models.BitcoinPaymentData
import io.horizontalsystems.bitcoincore.models.PublicKey
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDExtendedKey
import io.horizontalsystems.hdwalletkit.HDExtendedKeyVersion
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.NumberFormat
import java.util.*

class WalletManager(
    val application: Application,
    val localStoreRepository: LocalStoreRepository,
    val syncer: SyncManager
): AbstractWalletManager(), WipeDataListener, SyncManager.SyncEvent {
    private val localPassphrases: MutableMap<String,String> = mutableMapOf()
    private val mutex = Mutex()
    private var _baseMainNetWallet: BitcoinKit? = null
    private var _baseTestNetWallet: BitcoinKit? = null
    private var stateListeners: MutableList<StateListener> = mutableListOf()

    open class BitcoinEventListener: BitcoinKit.Listener {}

    override fun start() {
        @OptIn(DelicateCoroutinesApi::class)
        CoroutineScope(Dispatchers.IO).launch {
            if(!syncer.isRunning()) {
                _baseMainNetWallet =
                    createBaseWallet(_baseMainNetWallet, BitcoinKit.NetworkType.MainNet)
                _baseTestNetWallet =
                    createBaseWallet(_baseTestNetWallet, BitcoinKit.NetworkType.TestNet)
                localStoreRepository.setOnWipeDataListener(this@WalletManager)
                syncer.start()
                syncer.addListener(this@WalletManager)
                updateWallets()
            }
        }
    }

    override fun stop() {
        syncer.stop()
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

            base.onEnterBackground()
            return base
        } else
            return baseWallet
    }

    override fun openWallet(wallet: LocalWalletModel) {
        syncer.openWallet(wallet)
    }

    override fun closeWallet() {
        syncer.closeWallet()
    }

    override fun getOpenedWallet(): LocalWalletModel? {
        return syncer.getOpenedWallet()
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
        return _baseMainNetWallet!!.isAddressValid(address) || _baseTestNetWallet!!.isAddressValid(address)
    }

    override fun parseInvoice(invoiceData: String) : BitcoinPaymentData {
        return _baseMainNetWallet!!.parsePaymentAddress(invoiceData)
    }

    override fun arePeersReady(localWallet: LocalWalletModel) : Boolean {
        return localWallet.walletKit!!.arePeersReady()
    }

    override fun getFullPublicKeyPath(key: PublicKey): String {
        return getOpenedWallet()!!.walletKit!!.getFullPublicKeyPath(key)
    }

    private fun getTotalBalance(): Long {
        var balance : Long = 0

        syncer.getWallets().forEach {
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
        stop()
        deleteWalletFromDatabase(localWallet)
        localStoreRepository.getStoredWalletInfo().walletIdentifiers.remove { it.walletUUID == localWallet.uuid }
        localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())

        localPassphrases.remove(localWallet.uuid)
        syncer.removeWallet(localWallet.uuid)
        start()
        onDeleteFinished()
    }

    override fun synchronize(wallet: LocalWalletModel) {
        if(!syncer.sync(wallet)) {
            CoroutineScope(Dispatchers.Main).launch {
                onWalletAlreadySynced(wallet)
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

   override fun getWallets(): List<LocalWalletModel> {
       return syncer.getWallets()
   }

    override fun synchronizeAll() {
        syncer.syncWallets()
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

    private suspend fun onWalletAlreadySynced(wallet: LocalWalletModel) {
        mutex.withLock {
            stateListeners.forEach {
                CoroutineScope(Dispatchers.Main).launch {
                    it.onWalletAlreadySynced(wallet)
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

   override fun onWipeData() {
       syncer.stop {
           deleteWalletFromDatabase(it)
       }

       localPassphrases.clear()
       localStoreRepository.setStoredWalletInfo(null)
       _balanceUpdated.postValue(0)
   }

   override fun findLocalWallet(uuid: String): LocalWalletModel? =
       syncer.getWallets().find { it.uuid == uuid }

   override fun findStoredWallet(uuid: String): WalletIdentifier? =
       localStoreRepository.getStoredWalletInfo().walletIdentifiers.find { it.walletUUID == uuid }

   private fun saveWallet(wallet: WalletIdentifier) {
       localStoreRepository.getStoredWalletInfo().walletIdentifiers.add(wallet)
       localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
       updateWallets()
   }

    fun startAutoSync() {
        syncer.startAutoSync()
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
               0,
               testnetWallet,
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

       saveWallet(
           WalletIdentifier(
               name,
               uuid,
               listOf(),
               pubKey,
               mutableListOf(),
               HDExtendedKey(pubKey).info.purpose.ordinal,
               0,
               network == BitcoinKit.NetworkType.TestNet,
               true
           )
       )

       return uuid
   }

   private fun updateWallets() {
       syncer.stopAllWallets()
       syncer.clearWallets()

       syncer.addWallets(
           localStoreRepository.getStoredWalletInfo().walletIdentifiers.map { identifier ->
               val passphrase = getWalletPassPhrase(identifier.walletUUID)
               val model = LocalWalletModel.consume(
                   identifier,
                   if (!identifier.readOnly) passphrase else ""
               )

               // Store wallet hashes for passphrases
               if (identifier.walletHashIds?.find { it == model.hashId } == null) {
                   identifier.walletHashIds!!.add(model.hashId)
                   localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
               }

               if (identifier.readOnly) {
                   model.walletKit = BitcoinKit(
                       context = application,
                       extendedKey = HDExtendedKey(identifier.pubKey),
                       walletId = model.hashId,
                       networkType = getWalletNetwork(model),
                       peerSize = Constants.Limit.MAX_PEERS,
                       gapLimit = 50,
                       syncMode = BitcoinCore.SyncMode.Api(),
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
                           syncMode = BitcoinCore.SyncMode.Api(),
                           confirmationsThreshold = localStoreRepository.getMinimumConfirmations(),
                           purpose = HDWallet.Purpose.values()
                               .find { it.ordinal == identifier.bip }!!
                       )
               }

               model.walletKit!!.onEnterBackground()
               model.walletKit!!.listener =
                   object : BitcoinEventListener() {
                       override fun onBalanceUpdate(balance: BalanceInfo) {
                           super.onBalanceUpdate(balance)
                           CoroutineScope(Dispatchers.Main).launch {
                               onWalletStateUpdated(model)
                               _balanceUpdated.postValue(getTotalBalance())
                           }
                       }

                       override fun onKitStateUpdate(state: BitcoinCore.KitState) {
                           super.onKitStateUpdate(state)

                           when (state) {
                               is BitcoinCore.KitState.NotSynced,
                               is BitcoinCore.KitState.Synced -> {
                                   CoroutineScope(Dispatchers.Main).launch {
                                       if(state == BitcoinCore.KitState.Synced) {
                                           identifier.lastSynced = System.currentTimeMillis()
                                           localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
                                       }

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

               model
           }
       )

       _balanceUpdated.postValue(getTotalBalance())
       synchronizeAll()
   }

   companion object {
       private val TAG = "WalletManager"

       fun getWalletNetwork(wallet: LocalWalletModel): BitcoinKit.NetworkType {
           if(wallet.testNetWallet)
               return BitcoinKit.NetworkType.TestNet
           else return BitcoinKit.NetworkType.MainNet
       }
   }

    override fun onSyncing(isSyncing: Boolean) {
        _onSyncing.postValue(isSyncing)
    }

    override fun onWalletsUpdated(wallets: List<LocalWalletModel>) {
        _wallets.postValue(wallets)
    }

    override fun getLastSyncedTime(wallet: LocalWalletModel): Long {
        return findStoredWallet(wallet.uuid)?.lastSynced ?: 0
    }
}