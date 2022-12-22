package io.horizontalsystems.bitcoincore.managers

import com.intuisoft.plaid.common.coroutines.PlaidScope
import com.intuisoft.plaid.common.util.extensions.remove
import io.horizontalsystems.bitcoincore.core.IPublicKeyManager
import io.horizontalsystems.bitcoincore.core.IStorage
import io.horizontalsystems.bitcoincore.models.BlockHash
import io.horizontalsystems.bitcoincore.models.PublicKey
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.logging.Logger

class InitialSyncer(
    private val storage: IStorage,
    private val blockDiscovery: IBlockDiscovery,
    private val publicKeyManager: IPublicKeyManager,
    private val multiAccountPublicKeyFetcher: IMultiAccountPublicKeyFetcher?
) {

    interface Listener {
        fun onSyncSuccess()
        fun onSyncFailed(error: Throwable)
    }

    var listener: Listener? = null

    private val logger = Logger.getLogger("InitialSyncer")
    private var syncJobs: CopyOnWriteArrayList<Job> = CopyOnWriteArrayList()

    fun terminate() {
        syncJobs.forEach {
            it.cancel()
        }

        syncJobs.clear()
    }

    fun sync() {
        val job = PlaidScope.IoScope.launch {
            try {
                blockDiscovery.discoverBlockHashes(this.coroutineContext.job).let { (publicKeys, blockHashes) ->
                    val sortedUniqueBlockHashes = blockHashes.distinctBy { it.height }.sortedBy { it.height }
                    handle(publicKeys, sortedUniqueBlockHashes)
                }

                syncJobs.remove { it == this.coroutineContext.job }
            } catch (e: Throwable) {
                handleError(e)
                syncJobs.remove { it == this.coroutineContext.job }
            }
        }

        syncJobs.add(job)
    }

    private fun handle(keys: List<PublicKey>, blockHashes: List<BlockHash>) {
        publicKeyManager.addKeys(keys)

        if (multiAccountPublicKeyFetcher != null) {
            if (blockHashes.isNotEmpty()) {
                storage.addBlockHashes(blockHashes)
                multiAccountPublicKeyFetcher.increaseAccount()
                sync()
            } else {
                handleSuccess()
            }
        } else {
            storage.addBlockHashes(blockHashes)
            handleSuccess()
        }
    }

    private fun handleSuccess() {
        listener?.onSyncSuccess()
    }

    private fun handleError(error: Throwable) {
        logger.severe("Initial Sync Error: ${error.message}")

        listener?.onSyncFailed(error)
    }
}
