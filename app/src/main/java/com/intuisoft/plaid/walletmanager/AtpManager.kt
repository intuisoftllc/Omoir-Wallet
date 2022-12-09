package com.intuisoft.plaid.walletmanager

import android.app.Application
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.NetworkUtil
import io.horizontalsystems.bitcoincore.extensions.toReversedHex
import io.horizontalsystems.bitcoincore.managers.SendValueErrors
import io.horizontalsystems.bitcoincore.models.TransactionDataSortType
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class AtpManager(
    private val application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val apiRepository: ApiRepository,
    val syncer: SyncManager
) {
    private var masterJob: Job? = null

    private var _running = AtomicBoolean(false)
    protected var running: Boolean
        get() = _running.get()
        set(value) {
            _running.set(value)
        }

    private var _stopped = AtomicBoolean(true)
    protected var stopped: Boolean
        get() = _stopped.get()
        set(value) {
            _stopped.set(value)
        }

    private fun runInBackground(run: suspend () -> Unit) =
        CoroutineScope(Dispatchers.IO + NonCancellable).launch {
            run()
        }

    fun isRunning() = running

    fun start() {
        if(!running) {
            running = true
            run()
        }
    }

    suspend fun stop() {
        if(running) {
            running = false
            while(!stopped) {
                delay(10)
            }
        }
    }

    private suspend fun spendUtxo(
        utxo: UnspentOutput,
        address: String,
        feeRate: Int,
        fallbackFeeRate: Int,
        wallet: LocalWalletModel
    ): UtxoSpendResult? {

        var fee = calculateFeeForMaxSpend(wallet, utxo, feeRate, address)
        if(fee.first > 0) {
            return UtxoSpendResult(
                feeRate = feeRate,
                fees = fee.first,
                txId = wallet!!.walletKit!!.redeem(
                    unspentOutputs = listOf(utxo),
                    value = fee.second,
                    address = address,
                    feeRate = feeRate,
                    sortType = TransactionDataSortType.Shuffle
                ).header.hash.toReversedHex(),
                amountSent = fee.second
            )
        }

        fee = calculateFeeForMaxSpend(wallet, utxo, fallbackFeeRate, address)
        if(fee.first > 0) {
            return UtxoSpendResult(
                feeRate = fallbackFeeRate,
                fees = fee.first,
                txId = wallet!!.walletKit!!.redeem(
                    unspentOutputs = listOf(utxo),
                    value = fee.second,
                    address = address,
                    feeRate = fallbackFeeRate,
                    sortType = TransactionDataSortType.Shuffle
                ).header.hash.toReversedHex(),
                amountSent = fee.second
            )
        }

        return null
    }

    private suspend fun processBatch(
        batch: BatchDataModel,
        lastBatch: BatchDataModel?,
        transfer: AssetTransferModel,
        wallet: LocalWalletModel,
        recipient: LocalWalletModel
    ) {
        if(!NetworkUtil.hasInternet(application)) return

        if(lastBatch == null ||
            (lastBatch.status.id in AssetTransferStatus.PARTIALLY_COMPLETED.id..AssetTransferStatus.CANCELLED.id)) {

            if(lastBatch != null && transfer.batchGap > 0
                && (wallet.walletKit!!.lastBlockInfo?.height ?: 0) <= (transfer.batchGap + lastBatch.completionHeight)) {
                return
            }

            when (batch.status) {
                AssetTransferStatus.NOT_STARTED -> {

                    var addresses = recipient.walletKit!!.receiveAddresses()
                    if(addresses.size < batch.utxos.size) {
                        recipient.walletKit!!.fillGap()
                        addresses = recipient.walletKit!!.receiveAddresses()
                    }

                    batch.utxos.forEachIndexed { index, utxo ->
                        if(utxo.txId.isEmpty()) {
                            val output = wallet.walletKit!!.getUnspentOutputs()
                                .find { it.output.address == utxo.address }
                            val feeRate =
                                if (transfer.dynamicFees) apiRepository.getSuggestedFeeRate(wallet.testNetWallet)?.highFee
                                    ?: utxo.feeRate
                                else utxo.feeRate

                            if (output != null) {
                                val result = spendUtxo(
                                    output,
                                    addresses[index],
                                    feeRate,
                                    transfer.feeRangeLow,
                                    wallet
                                )

                                if (result != null) {
                                    utxo.txId = result.txId
                                    utxo.feeRate = result.feeRate
                                    transfer.feesPaid += result.fees
                                    transfer.sent += result.amountSent
                                } else {
                                    localStoreRepository.blacklistAddress(
                                        BlacklistedAddressModel(address = output.output.address!!),
                                        blacklist = false
                                    )
                                    utxo.txId = "failed to spend due to network fees"
                                    utxo.feeRate = -1
                                }
                            } else {
                                utxo.txId = "externally spent"
                                utxo.feeRate = -1
                            }

                            localStoreRepository.setBatchData(batch)
                        }
                    }

                    batch.status = AssetTransferStatus.IN_PROGRESS
                    localStoreRepository.setBatchData(batch)
                    localStoreRepository.saveAssetTransfer(transfer)
                }

                AssetTransferStatus.IN_PROGRESS -> {
                    val requiredTxs = batch.utxos.filter { it.feeRate > 0 }.map { it.txId }
                    val txs = wallet.walletKit!!.getAllTransactions().filter { transaction ->
                        requiredTxs.find { transaction.transactionHash == it } != null
                    }

                    if(txs.all { it.blockHeight != null }) {
                        batch.status = AssetTransferStatus.COMPLETED
                        batch.completionHeight = txs.map { it.blockHeight!! }.maxOrNull() ?: 0
                    } else if(txs.all { it.status == TransactionStatus.INVALID }) {
                        batch.status = AssetTransferStatus.FAILED
                    } else if(txs.all { it.blockHeight != null || it.status == TransactionStatus.INVALID}) {
                        batch.status = AssetTransferStatus.PARTIALLY_COMPLETED
                        batch.completionHeight = txs.map { it.blockHeight ?: 0 }.maxOrNull() ?: 0
                    }

                    localStoreRepository.setBatchData(batch)
                }
            }
        }
    }

    private suspend fun processTransfer(transfer: AssetTransferModel, wallet: LocalWalletModel) {
        if(!NetworkUtil.hasInternet(application)) return

        if(transfer.status == AssetTransferStatus.IN_PROGRESS || transfer.status == AssetTransferStatus.NOT_STARTED) {
            val batches = localStoreRepository.getBatchDataForTransfer(transfer.id)
            var lastBatch: BatchDataModel? = null
            val recipient = syncer.getWallets().find { it.uuid == transfer.recipientWallet }

            if (recipient != null) {

                wallet.walletKit!!.onEnterForeground()
                wallet.walletKit!!.refresh()
                recipient.walletKit!!.onEnterForeground()
                recipient.walletKit!!.refresh()

                batches.forEach {
                    if (!running) return@forEach
                    processBatch(it, lastBatch, transfer, wallet, recipient)
                    lastBatch = it
                }

                if (transfer.status == AssetTransferStatus.NOT_STARTED) {
                    transfer.status = AssetTransferStatus.IN_PROGRESS
                } else {
                    if (batches.all { it.status == AssetTransferStatus.COMPLETED }) {
                        transfer.status = AssetTransferStatus.COMPLETED
                    } else if (batches.all { it.status == AssetTransferStatus.FAILED }) {
                        transfer.status = AssetTransferStatus.FAILED
                    } else if (
                        batches.all {
                            it.status == AssetTransferStatus.PARTIALLY_COMPLETED
                                    || it.status == AssetTransferStatus.COMPLETED
                                    || it.status == AssetTransferStatus.FAILED
                        }
                    ) {
                        transfer.status = AssetTransferStatus.PARTIALLY_COMPLETED
                    }
                }

                syncer.safeBackground(wallet)
                syncer.safeBackground(recipient)
            } else if(++transfer.retries >= Constants.Limit.ATP_MAX_RETRY_LIMIT) {
                transfer.retries++
                transfer.status = AssetTransferStatus.FAILED
            }

            localStoreRepository.saveAssetTransfer(transfer)
        }
    }

    private fun run() {
        if(running && masterJob == null) {
            masterJob = runInBackground {
                while(true) {
                    if(running && syncer.isRunning()) {
                        stopped = false
                        syncer.getWallets().forEach { wallet ->
                            val transfers = localStoreRepository.getAllAssetTransfers(wallet.uuid)

                            transfers.forEach {
                                if(!running) return@forEach
                                processTransfer(it, wallet)
                            }
                        }

                        stopped = true
                        delay(Constants.Time.MILLS_PER_SEC.toLong() * 5)
                    } else {
                        stopped = true
                        while(!running) {
                            delay(100)
                        }
                    }
                }
            }
        }
    }

    data class UtxoSpendResult(
        val feeRate: Int,
        val fees: Long,
        val txId: String,
        val amountSent: Long
    )

    companion object {

        fun calculateFeeForMaxSpend(wallet: LocalWalletModel, unspentOutput: UnspentOutput, feeRate: Int, address: String?) : Pair<Long, Long> {
            try {
                var max = wallet!!.walletKit!!.maximumSpendableValue(
                    listOf(unspentOutput),
                    address,
                    feeRate
                )

                return wallet!!.walletKit!!.fee(listOf(unspentOutput), max, address, true, feeRate) to max
            } catch(e: SendValueErrors.Dust) {
                return -2L to 0L
            } catch(e: SendValueErrors.InsufficientUnspentOutputs) {
                return -1L to 0L
            } catch(e: Exception) {
                return 0L to 0L
            }
        }
    }

}