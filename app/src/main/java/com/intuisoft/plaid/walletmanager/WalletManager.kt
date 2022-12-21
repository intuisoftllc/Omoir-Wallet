package com.intuisoft.plaid.walletmanager

import android.app.Application
import com.intuisoft.plaid.common.listeners.WipeDataListener
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.util.entensions.sha256
import com.intuisoft.plaid.common.util.errors.ClosedWalletErr
import com.intuisoft.plaid.common.util.errors.ExistingWalletErr
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

class WalletManager(
    val application: Application,
    val localStoreRepository: LocalStoreRepository,
    val syncer: SyncManager
): AbstractWalletManager(), WipeDataListener, DatabaseListener, SyncManager.SyncEvent {
    private val localPassphrases: MutableMap<String,String> = mutableMapOf()
    private var _baseMainNetWallet: BitcoinKit? = null
    private var _baseTestNetWallet: BitcoinKit? = null
    private var stateListeners: MutableList<StateListener> = mutableListOf()

    open class BitcoinEventListener: BitcoinKit.Listener {}

    override fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            if(!syncer.isRunning()) {
                _baseMainNetWallet =
                    createBaseWallet(_baseMainNetWallet, BitcoinKit.NetworkType.MainNet)
                _baseTestNetWallet =
                    createBaseWallet(_baseTestNetWallet, BitcoinKit.NetworkType.TestNet)
                localStoreRepository.setOnWipeDataListener(this@WalletManager)
                localStoreRepository.setDatabaseListener(this@WalletManager)
                syncer.addListener(this@WalletManager)
                syncer.start()
                updateWallets()
            }
        }
    }

    override suspend fun stop() {
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

    override fun getOpenedWallet(): LocalWalletModel {
        return syncer.getOpenedWallet() ?: throw ClosedWalletErr("")
    }

    override fun onDatabaseUpdated(dao: Any?) {
        _databaseUpdated.postValue(dao)
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

    override fun canSendTransaction(localWallet: LocalWalletModel) : Boolean {
        return localWallet.walletKit!!.canSendTransaction()
    }

    override fun getFullPublicKeyPath(key: PublicKey): String {
        return getOpenedWallet()!!.walletKit!!.getFullPublicKeyPath(key)
    }

    override fun requiresNewHiddenWallet(wallet: LocalWalletModel, passphrase: String): Boolean {
        val storedWallet = findStoredWallet(wallet.uuid)
        val passphraseWalletUUID = (wallet.uuid + passphrase).sha256()
        return storedWallet?.walletHashIds?.find { it == passphraseWalletUUID } == null
    }

    override fun getWalletCount(): Int {
        return syncer.getWallets().size
    }

    override fun getHiddenWalletCount(wallet: LocalWalletModel): Int {
        val storedWallet = findStoredWallet(wallet.uuid)
        return storedWallet?.walletHashIds?.size?.minus(1) ?: 0
    }

    private fun getTotalBalance(): Long {
        var balance : Long = 0

        syncer.getWallets().forEach {
            balance += it.getWhitelistedBalance(localStoreRepository)
        }

        return balance
    }

    override fun cancelTransfer(id: String) {
        syncer.cancelTransfer(id)
    }

    private fun deleteWalletFromDatabase(localWallet: LocalWalletModel) {
        localWallet.walletKit?.stop()

        findStoredWallet(localWallet.uuid)?.let { walletIdentifier ->
            walletIdentifier.walletHashIds.forEach { hashId ->
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

   override fun getWallets(): List<LocalWalletModel> {
       return syncer.getWallets()
   }

    override fun synchronizeAll(force: Boolean) {
        syncer.syncWallets(force)
    }

    private suspend fun onWalletStateUpdated(wallet: LocalWalletModel) {
        synchronized(this) {
            stateListeners.forEach {
                CoroutineScope(Dispatchers.Main).launch {
                    it.onWalletStateUpdated(wallet)
                }
            }
        }
    }

    override fun onWalletAlreadySynced(wallet: LocalWalletModel) {
        synchronized(this) {
            stateListeners.forEach {
                CoroutineScope(Dispatchers.Main).launch {
                    it.onWalletAlreadySynced(wallet)
                }
            }
        }
    }

    override suspend fun addWalletSyncListener(listener: StateListener) {
        synchronized(this) {
            if(stateListeners.find { it == listener } == null) {
                stateListeners.add(listener)
            }
        }
    }

    override suspend fun removeSyncListener(listener: StateListener) {
        synchronized(this) {
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
       val uuid = seed.joinToString(",").sha256(16)
       if(findStoredWallet(uuid) != null) {
           throw ExistingWalletErr("Wallet Already created")
       }

       saveWallet(
           WalletIdentifier(
               name,
               uuid,
               seed,
               "",
               mutableListOf(),
               bip.ordinal,
               0,
               System.currentTimeMillis(),
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
       val uuid = pubKey.sha256(16)

       var network = BitcoinKit.NetworkType.MainNet
       if(pubKey.startsWith(HDExtendedKeyVersion.tpub.base58Prefix)
           || pubKey.startsWith(HDExtendedKeyVersion.upub.base58Prefix)
           || pubKey.startsWith(HDExtendedKeyVersion.vpub.base58Prefix)) {
           network = BitcoinKit.NetworkType.TestNet
       }

       if(findStoredWallet(uuid) != null) {
           throw ExistingWalletErr("Wallet Already created")
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
               System.currentTimeMillis(),
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
       synchronizeAll(true)
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