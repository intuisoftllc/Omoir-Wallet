package com.intuisoft.plaid.walletmanager

import android.app.Application
import com.intuisoft.plaid.common.coroutines.OmoirScope
import com.intuisoft.plaid.common.listeners.WipeDataListener
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.HiddenWalletModel
import com.intuisoft.plaid.common.model.SavedAccountModel
import com.intuisoft.plaid.common.model.WalletIdentifier
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Limit.DEFAULT_GAP_LIMIT
import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.util.errors.ClosedWalletErr
import com.intuisoft.plaid.common.util.errors.ExistingWalletErr
import com.intuisoft.plaid.common.util.extensions.sha256
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
    private var hiddenWallets: MutableMap<String, HiddenWalletModel?> = mutableMapOf()
    private var _baseMainNetWallet: BitcoinKit? = null
    private var _baseTestNetWallet: BitcoinKit? = null
    private var stateListeners: MutableList<StateListener> = mutableListOf()

    open class BitcoinEventListener: BitcoinKit.Listener {}

    override fun start() {
        OmoirScope.applicationScope.launch(Dispatchers.IO) {
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

    override fun isRunning(): Boolean {
        return syncer.isRunning()
    }

    override fun getHiddenWallets(): MutableMap<String, HiddenWalletModel?> {
        return hiddenWallets
    }

    override fun setInitialHiddenWallets(hiddenWallets: MutableMap<String, HiddenWalletModel?>) {
        this.hiddenWallets = hiddenWallets
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
        findAndUpdateBaseWallet(localWallet.uuid) {
            name = newName
            localWallet.name = newName
            localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
        }
    }

    private fun getCurrentHiddenWallet(uuid: String): HiddenWalletModel? {
        return hiddenWallets.get(uuid)
    }

    private fun setCurrentHiddenWallet(walletId: String, passphrase: String, account: SavedAccountModel) {
        val baseWalletId = findAndUpdateBaseWallet(walletId)!!.walletUUID
        if(passphrase.isBlank() && account.account == 0) {
            hiddenWallets.put(baseWalletId, null)
        } else {
            hiddenWallets.put(
                baseWalletId,
                HiddenWalletModel(
                    walletUUID = baseWalletId,
                    passphrase = passphrase,
                    account = account
                )
            )
        }
    }

    override fun getCurrentHiddenWallet(localWallet: LocalWalletModel): HiddenWalletModel? {
        return getCurrentHiddenWallet(findAndUpdateBaseWallet(localWallet.uuid)!!.walletUUID)
    }

    override fun setCurrentHiddenWallet(localWallet: LocalWalletModel, passphrase: String, account: SavedAccountModel) {
        setCurrentHiddenWallet(localWallet.uuid, passphrase, account)
    }

    override fun validAddress(address: String) : Boolean {
        return _baseMainNetWallet!!.isAddressValid(address) || _baseTestNetWallet!!.isAddressValid(address)
    }

    override fun validPubPrivKey(key: String): Boolean {
        return _baseMainNetWallet!!.isPubPrivKeyValid(key)
    }

    private fun validPubKey(key: String): Boolean {
        return _baseMainNetWallet!!.isPubKeyValid(key)
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

    override fun requiresNewHiddenWallet(wallet: LocalWalletModel, passphrase: String, account: SavedAccountModel): Boolean {
        val storedWallet = findStoredWallet(wallet.uuid)
        val passphraseWalletUUID = HiddenWalletModel(wallet.uuid, passphrase, account).uuid
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

        syncer.getWallets()
            .distinctBy {
                val storedWallet = findStoredWallet(it.uuid)

                if(storedWallet?.readOnly == true)
                    storedWallet.pubKey
                else
                    it.walletKit?.getMasterPublicKey(!it.testNetWallet, it.hiddenWallet)
            }.forEach {
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
        localStoreRepository.getStoredWalletInfo().walletIdentifiers.remove(
            findAndUpdateBaseWallet(localWallet.uuid)
        )
        localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())

        hiddenWallets.remove(localWallet.uuid)
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

       hiddenWallets.clear()
       localStoreRepository.setStoredWalletInfo(null)
       _balanceUpdated.postValue(0)
   }

   override fun findLocalWallet(uuid: String): LocalWalletModel? =
       syncer.getWallets().find { it.uuid == uuid }

   override fun findStoredWallet(uuid: String): WalletIdentifier? =
       findAndUpdateBaseWallet(uuid)

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
       testnetWallet: Boolean,
       gapLimit: Int
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
               false,
               gapLimit
           )
       )

       return uuid
   }

   override suspend fun createWallet(
       name: String,
       pubKey: String,
       gapLimit: Int
   ): String {
       val uuid = pubKey.sha256(16)

       var network = BitcoinKit.NetworkType.MainNet
       if(pubKey.startsWith(HDExtendedKeyVersion.tpub.base58Prefix)
           || pubKey.startsWith(HDExtendedKeyVersion.upub.base58Prefix)
           || pubKey.startsWith(HDExtendedKeyVersion.vpub.base58Prefix)) {
           network = BitcoinKit.NetworkType.TestNet
       }

       if(findStoredWallet(uuid) != null) {
           throw ExistingWalletErr("Wallet already created")
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
               validPubKey(pubKey),
               gapLimit
           )
       )

       return uuid
   }

    fun findAndUpdateBaseWallet(walletId: String, block: (WalletIdentifier.() -> Unit)? = null): WalletIdentifier? {
        localStoreRepository.getStoredWalletInfo().walletIdentifiers.find { walletIdentifier ->
            walletIdentifier.walletUUID == walletId || walletIdentifier.walletHashIds.find { it == walletId } != null
        }?.let {
            block?.invoke(it)
            return it
        }

        return null
    }

   private fun updateWallets() {
       syncer.stopAllWallets()
       syncer.clearWallets()

       syncer.addWallets(
           localStoreRepository.getStoredWalletInfo().walletIdentifiers.map { identifier ->
               val hiddenWallet = getCurrentHiddenWallet(identifier.walletUUID)
               val model = LocalWalletModel.consume(
                   identifier,
                   if (!identifier.readOnly) hiddenWallet else null
               )

               // Store wallet hashes for passphrases
               findAndUpdateBaseWallet(identifier.walletUUID) {
                   var save = false

                   if (walletHashIds.find { it == model.uuid } == null) {
                       walletHashIds.add(model.uuid)
                       save = true
                   }

                   if(gapLimit == null) {
                       save = true
                       gapLimit = DEFAULT_GAP_LIMIT
                   }

                   if(save) {
                       localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
                   }
               }


               if (identifier.readOnly || identifier.isPrivateKeyWallet) {
                   model.walletKit = BitcoinKit(
                       context = application,
                       extendedKey = HDExtendedKey(identifier.pubKey),
                       walletId = model.uuid,
                       networkType = getWalletNetwork(model),
                       peerSize = Constants.Limit.MAX_PEERS,
                       gapLimit = identifier.gapLimit ?: DEFAULT_GAP_LIMIT,
                       syncMode = BitcoinCore.SyncMode.Api(),
                       confirmationsThreshold = localStoreRepository.getMinimumConfirmations()
                   )
               } else if(hiddenWallet != null) {
                   model.walletKit =
                       BitcoinKit(
                           context = application,
                           words = identifier.seedPhrase,
                           passphrase = hiddenWallet.passphrase,
                           walletAccount = hiddenWallet.account.account,
                           walletId = model.uuid,
                           networkType = getWalletNetwork(model),
                           peerSize = Constants.Limit.MAX_PEERS,
                           gapLimit = identifier.gapLimit ?: DEFAULT_GAP_LIMIT,
                           syncMode = BitcoinCore.SyncMode.Api(),
                           confirmationsThreshold = localStoreRepository.getMinimumConfirmations(),
                           purpose = HDWallet.Purpose.values()
                               .find { it.ordinal == identifier.bip }!!
                       )
               } else { // enter default HD Wallet
                   model.walletKit =
                       BitcoinKit(
                           context = application,
                           words = identifier.seedPhrase,
                           passphrase = "",
                           walletId = model.uuid,
                           networkType = getWalletNetwork(model),
                           peerSize = Constants.Limit.MAX_PEERS,
                           gapLimit = identifier.gapLimit ?: DEFAULT_GAP_LIMIT,
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

                           if(model.isSynced) {
                               CoroutineScope(Dispatchers.Main).launch {
                                   onWalletStateUpdated(model)
                                   _balanceUpdated.postValue(getTotalBalance())
                               }
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
                                           identifier.createdAt =
                                               (model.walletKit!!.getAllTransactions()
                                                   .lastOrNull()?.timestamp?.times(1000)) ?: identifier.createdAt
                                           localStoreRepository.setStoredWalletInfo(localStoreRepository.getStoredWalletInfo())
                                       }

                                       onWalletStateUpdated(model)
                                       _balanceUpdated.postValue(getTotalBalance())
                                   }
                               }

                               else -> {
                                   if(model.syncPercentage != model.lastSyncPercentage) {
                                       model.lastSyncPercentage = model.syncPercentage

                                       CoroutineScope(Dispatchers.Main).launch {
                                           onWalletStateUpdated(model)
                                       }
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