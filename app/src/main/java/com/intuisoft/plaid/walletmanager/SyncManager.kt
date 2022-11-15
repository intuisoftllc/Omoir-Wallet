package com.intuisoft.plaid.walletmanager

import android.app.Application
import android.util.Log
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.common.model.DevicePerformanceLevel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.util.entensions.splitIntoGroupOf
import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class SyncManager(
    private val application: Application,
    private val localStoreRepository: LocalStoreRepository
) {
    private val _wallets: CopyOnWriteArrayList<LocalWalletModel> = CopyOnWriteArrayList()
    private var syncJobs: MutableList<Job> = mutableListOf()
    private var masterSyncJob: Job? = null
    private var autoSyncJob: Job? = null
    private var listener: SyncEvent? = null

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
            _syncing.set(value)
            listener?.onSyncing(value)
        }

    private fun runInBackground(run: suspend () -> Unit) =
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            run()
        }

    private fun lazyRunInBackground(run: suspend () -> Unit) =
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(start = CoroutineStart.LAZY) {
            run()
        }

    fun getWallets() =  _wallets

    fun stopAllWallets() {
        cancelAllJobs()

        _wallets.forEach {
            it.walletKit?.stop()
        }
    }

    fun clearWallets() {
        Log.e("LOOK", "wallets cleared")
        _wallets.clear()
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
        autoSyncJob = runInBackground {
            while(true) {
                delay(Constants.Time.AUTO_SYNC_TIME)
                syncWallets()
            }
        }
    }

    fun start() {
        if(!running) {
            running = true

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
            autoSyncJob?.cancel()
        }
    }

    private fun cancelAllJobs() {
        if(running && syncing) {
            masterSyncJob?.cancel()
            stopAutoSyncer()
            syncJobs.forEach {
                it.cancel()
            }

            syncJobs.clear()
            syncing = false
        }
    }

    fun sync(wallet: LocalWalletModel) : Boolean {
        if (!wallet.isSyncing && !wallet.isSynced) {
            runInBackground {
                wallet.walletKit!!.refresh()
            }

            return true
        }

        return false
    }

    fun getSyncGrouping(): Int { // todo: prevent user from syncing more wallets than allowed based on performance specs
        return when(localStoreRepository.getDevicePerformanceLevel()) {
            DevicePerformanceLevel.DEFAULT -> {
                1
            }

            DevicePerformanceLevel.MED -> {
                2
            }

            DevicePerformanceLevel.HIGH -> {
                3
            }
        }
    }

    fun syncWallets() {
        if(running && !syncing && syncJobs.isEmpty()) {
            syncing = true

            _wallets
                .splitIntoGroupOf(getSyncGrouping())
                .forEach { group ->
                    syncJobs.add(
                        lazyRunInBackground {
                            var startTime = System.currentTimeMillis()
                            var restarts = 0
                            group.items.forEach {
                                sync(it)
                            }

                            // wait for this group of wallets to sync
                            while(!group.items.all { it.isSynced }) {
                                delay(100)

                                if((System.currentTimeMillis() - startTime) >= Constants.Time.SYNC_TIMEOUT) {
                                    group.items.forEach {
                                        if (it.isRestored && !it.isSynced && it.syncPercentage == 0) { // restart stuck wallets
                                            it.walletKit!!.restart()
                                            startTime = System.currentTimeMillis()
                                            ++restarts
                                        }
                                    }

                                    if(restarts > Constants.Limit.SYNC_RESTART_LIMIT) {
                                        break
                                    }
                                }
                            }

                            group.items.forEach {
                                it.walletKit!!.onEnterBackground()
                            }
                        }
                    )
                }

            masterSyncJob = runInBackground {
                syncJobs.forEach {
                    it.start()
                    it.join()
                }

                syncJobs.clear()
                syncing = false
            }
        }
    }

    interface SyncEvent {
        fun onSyncing(isSyncing: Boolean)
        fun onWalletsUpdated(wallets: List<LocalWalletModel>)
    }
}