package com.intuisoft.plaid.walletmanager

import android.app.Application
import android.util.Log
import com.intuisoft.plaid.common.coroutines.PlaidScope
import com.intuisoft.plaid.common.model.DevicePerformanceLevel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.common.util.extensions.splitIntoGroupOf
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.util.entensions.ensureActive
import io.horizontalsystems.bitcoincore.network.peer.PeerGroup
import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class SyncManager(
    private val application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val atp: AtpManager
) {
    private val _wallets: CopyOnWriteArrayList<LocalWalletModel> = CopyOnWriteArrayList()
    private var masterSyncJob: Job? = null
    private var autoSyncJob: Job? = null
    private var listener: SyncEvent? = null
    private var resync = false
    private var openedWallet: LocalWalletModel? = null

    private var _running = AtomicBoolean(false)
    protected var running: Boolean
        get() = _running.get()
        set(value) {
            _running.set(value)
        }

    private var _syncing = AtomicBoolean(false)
    protected var syncing: Boolean
        get() = _syncing.get()
        set(value) {
            if(!value) masterSyncJob = null
            _syncing.set(value)
            listener?.onSyncing(value)
        }

    private var lastSynced: Long = 0

    private fun runInBackground(run: suspend () -> Unit) =
        PlaidScope.applicationScope.launch(Dispatchers.IO) {
            run()
        }

    fun isRunning() = running

    fun openWallet(wallet: LocalWalletModel) {
        openedWallet = wallet
        wallet.walletKit?.onEnterForeground()
    }

    fun closeWallet() {
        if(openedWallet?.isSyncing == false)
            openedWallet?.walletKit?.onEnterBackground()
        openedWallet = null
    }

    fun getOpenedWallet() = openedWallet

    fun safeBackground(wallet: LocalWalletModel) {
        if(wallet != openedWallet) {
            wallet.walletKit!!.onEnterBackground()
        }
    }

    fun getWallets() =  _wallets

    fun stopAllWallets() {
        cancelAllJobs()

        _wallets.forEach {
            it.walletKit?.stop()
        }
    }

    fun clearWallets() {
        _wallets.clear()
        lastSynced = 0
    }

    fun addListener(eventsListener: SyncEvent) {
        listener = eventsListener
    }

    fun addWallets(wallets: List<LocalWalletModel>) {
        _wallets.addAll(wallets)
        listener?.onWalletsUpdated(_wallets)
    }

    fun removeWallet(uuid: String) {
        _wallets.remove { it.uuid == uuid }
        listener?.onWalletsUpdated(_wallets)
    }

    fun hasWallets() = _wallets.isNotEmpty()

    fun startAutoSync() {
        if(autoSyncJob == null) {
            autoSyncJob = runInBackground {
                while (true) {
                    autoSyncJob?.ensureActive()
                    delay(Constants.Time.MIN_SYNC_TIME.toLong())
                    lastSynced = 0
                    syncWallets()
                }
            }
        }
    }

    fun start() {
        if(!running) {
            running = true
            resync = false
            syncWallets()
            startAutoSync()
        }
    }

    fun stop(onStopped: ((LocalWalletModel) -> Unit)? = null) {
        if(running) {
            stopAllWallets()
            _wallets.forEach {
                onStopped?.invoke(it)
            }

            clearWallets()
            running = false
        }
    }

    fun stopAutoSyncer() {
        if(running) {
            runBlocking {
                autoSyncJob?.cancelAndJoin()
                autoSyncJob = null
            }
        }
    }

    private fun cancelAllJobs() {
        if(running && syncing) {
            runBlocking {
                masterSyncJob?.cancelAndJoin()
                stopAutoSyncer()
                syncing = false
                resync = false
            }
        }
    }

    fun sync(wallet: LocalWalletModel) : Boolean {
        if (!wallet.isSyncing && ((System.currentTimeMillis() - (listener?.getLastSyncedTime(wallet) ?: 0) >= Constants.Time.MIN_SYNC_TIME) || !wallet.walletKit!!.canSendTransaction())) {
            runInBackground {
                syncInternal(wallet)
            }

            return true
        }

        return false
    }

    private suspend fun syncInternal(wallet: LocalWalletModel) {
        if (!wallet.walletKit!!.restartIfNoPeersFound() && !wallet.isSyncing && !wallet.isSynced) {
            wallet.walletKit!!.onEnterForeground()
            wallet.walletKit!!.refresh()
        } else {
            listener?.onWalletAlreadySynced(wallet)
        }

        while(!wallet.isSynced && NetworkUtil.hasInternet(application)) {
            masterSyncJob?.ensureActive()
            delay(100)
        }

        safeBackground(wallet)
        if(wallet.isSynced && wallet.walletKit!!.canSendTransaction()) {
            if(
                atp.updateTransfers(
                    wallet = wallet,
                    findWallet = { walletId ->
                        getWallets().find { it.uuid == walletId }
                    },
                    job = masterSyncJob
                )
            ) {
                resync = true
            }
        }
    }

    fun cancelTransfer(id: String) {
        PlaidScope.applicationScope.launch(Dispatchers.IO) {
            atp.cancelTransfer(id, openedWallet!!)
            resync = true
            syncWallets(force = true)
        }
    }

    fun syncWallets(force: Boolean = false) {
        if(running && !syncing) {
            syncing = true
            if(!force &&
                ((System.currentTimeMillis() - lastSynced) <= Constants.Time.MIN_SYNC_TIME || _wallets.isEmpty())) {
                syncing = false
                return
            }

            masterSyncJob = runInBackground {
                _wallets
                    .forEach { wallet ->
                        syncInternal(wallet)
                    }

                lastSynced = System.currentTimeMillis()
                syncing = false
                syncWallets(force = resync)
                resync = false
            }
        }
    }

    interface SyncEvent {
        fun onSyncing(isSyncing: Boolean)
        fun onWalletsUpdated(wallets: List<LocalWalletModel>)
        fun getLastSyncedTime(wallet: LocalWalletModel): Long
        fun onWalletAlreadySynced(wallet: LocalWalletModel)
    }
}