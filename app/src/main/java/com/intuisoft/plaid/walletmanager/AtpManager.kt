package com.intuisoft.plaid.walletmanager

import android.app.Application
import androidx.work.Operation.State.IN_PROGRESS
import com.intuisoft.plaid.R
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.nextInt
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.util.entensions.ensureActive
import io.horizontalsystems.bitcoincore.extensions.toReversedHex
import io.horizontalsystems.bitcoincore.managers.SendValueErrors
import io.horizontalsystems.bitcoincore.models.TransactionDataSortType
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import kotlinx.coroutines.*
import kotlinx.coroutines.internal.synchronized
import java.security.SecureRandom

class AtpManager(
    private val application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val apiRepository: ApiRepository
) {
    private fun spendUtxo(
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
                    sortType = TransactionDataSortType.Shuffle,
                    ghostBroadcast = true
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
                    sortType = TransactionDataSortType.Shuffle,
                    ghostBroadcast = true
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
        recipient: LocalWalletModel,
        job: Job?
    ): Boolean {
        if(!NetworkUtil.hasInternet(application)) return false
        if(transfer.status != AssetTransferStatus.CANCELLED &&
                (lastBatch == null || (lastBatch.status.id in AssetTransferStatus.IN_PROGRESS.id..AssetTransferStatus.CANCELLED.id))) {

            lastBatch?.apply {
                when {
                    transfer.batchGap == 0 ||
                    status !in AssetTransferStatus.PARTIALLY_COMPLETED .. AssetTransferStatus.COMPLETED -> {
                        batch.blocksRemaining = 0
                    }

                    else -> {
                        batch.blocksRemaining = (transfer.batchGap + completionHeight) - (wallet.walletKit!!.lastBlockInfo?.height ?: 0)
                    }
                }

                if(batch.blocksRemaining > 0) {
                    batch.status = AssetTransferStatus.WAITING
                    localStoreRepository.setBatchData(batch)
                    return false
                } else if(status == AssetTransferStatus.IN_PROGRESS) {
                    return false
                }
            }

            when (batch.status) {
                AssetTransferStatus.WAITING,
                AssetTransferStatus.NOT_STARTED -> {

                    var utxoSent = false
                    val random = SecureRandom()
                    var addresses = recipient.walletKit!!.receiveAddresses()
                    if(addresses.size < batch.utxos.size) {
                        recipient.walletKit!!.fillGap()
                        addresses = recipient.walletKit!!.receiveAddresses()
                    }

                    batch.blocksRemaining = 0
                    transfer.status = AssetTransferStatus.IN_PROGRESS
                    batch.utxos.forEachIndexed { index, utxo ->
                        job?.ensureActive()

                        if(utxo.txId.isEmpty()) {
                            val output = wallet.walletKit!!.getUnspentOutputs()
                                .find { it.output.address == utxo.address }
                            val feeRate =
                                if (transfer.dynamicFees)
                                    Math.min(apiRepository.getSuggestedFeeRate(wallet.testNetWallet)?.highFee?.plus(random.nextInt(transfer.feeRangeLow, transfer.feeRangeHigh)) ?: utxo.feeRate, utxo.feeRate)
                                else utxo.feeRate

                            if (output != null) {
                                if(NetworkUtil.hasInternet(application)) {
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

                                        localStoreRepository.blacklistTransaction(
                                            BlacklistedTransactionModel(txId = result.txId, wallet.uuid),
                                            blacklist = true
                                        )
                                    } else {
                                        localStoreRepository.blacklistAddress(
                                            BlacklistedAddressModel(address = output.output.address!!),
                                            blacklist = false
                                        )
                                        utxo.txId = application.getString(R.string.atp_externally_spent, "$addresses")
                                        utxo.feeRate = -1
                                    }

                                    utxoSent = true
                                } else {
                                    return utxoSent
                                }
                            } else {
                                utxo.txId = application.getString(R.string.atp_failed_to_spend, "$addresses")
                                utxo.feeRate = -1
                            }

                            localStoreRepository.setBatchData(batch)
                        }
                    }

                    batch.status = AssetTransferStatus.IN_PROGRESS
                    localStoreRepository.setBatchData(batch)
                    localStoreRepository.saveAssetTransfer(transfer)
                    return utxoSent
                }

                AssetTransferStatus.IN_PROGRESS -> {
                    processInProgressBatch(batch, wallet)
                    transfer.status = AssetTransferStatus.IN_PROGRESS
                    localStoreRepository.saveAssetTransfer(transfer)
                    return false
                }
            }
        }

        return false
    }

    private suspend fun processInProgressBatch(
        batch: BatchDataModel,
        wallet: LocalWalletModel
    ) {
        val requiredTxs = batch.utxos.filter { it.feeRate > 0 }.map { it.txId }
        val txs = wallet.walletKit!!.getAllTransactions().filter { transaction ->
            requiredTxs.find { transaction.transactionHash == it } != null
        }

        if (txs.all { it.blockHeight != null }) {
            batch.status = AssetTransferStatus.COMPLETED
            batch.completionHeight = txs.map { it.blockHeight!! }.maxOrNull() ?: 0
        } else if (txs.all { it.status == TransactionStatus.INVALID }) {
            batch.status = AssetTransferStatus.FAILED
        } else if (txs.all { it.blockHeight != null || it.status == TransactionStatus.INVALID }) {
            batch.status = AssetTransferStatus.PARTIALLY_COMPLETED
            batch.completionHeight = txs.map { it.blockHeight ?: 0 }.maxOrNull() ?: 0
        }

        localStoreRepository.setBatchData(batch)
    }

    private suspend fun processTransfer(transfer: AssetTransferModel, wallet: LocalWalletModel, findWallet: (String) -> LocalWalletModel?, job: Job?): Boolean {
        if(!NetworkUtil.hasInternet(application)) return false

        val batches = localStoreRepository.getBatchDataForTransfer(transfer.id)
        var lastBatch: BatchDataModel? = null
        val recipient = findWallet(transfer.recipientWallet)
        var utxoSent = false

        if (recipient != null) {
            batches.forEach {
                job?.ensureActive()
                if(processBatch(it, lastBatch, transfer, wallet, recipient, job))
                    utxoSent = true
                lastBatch = it
            }

            if (transfer.status == AssetTransferStatus.NOT_STARTED) {
                transfer.status = AssetTransferStatus.IN_PROGRESS
            } else if(transfer.status != AssetTransferStatus.CANCELLED){
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
        } else if(++transfer.retries >= Constants.Limit.ATP_MAX_RETRY_LIMIT) {
            transfer.retries++
            transfer.status = AssetTransferStatus.FAILED
        }

        localStoreRepository.saveAssetTransfer(transfer)
        return utxoSent
    }

    private suspend fun cancelTransfer(transfer: AssetTransferModel, wallet: LocalWalletModel) {
        val batches = localStoreRepository.getBatchDataForTransfer(transfer.id)

        batches.forEach { batch ->
            if(batch.status.id in AssetTransferStatus.NOT_STARTED.id..AssetTransferStatus.WAITING.id) {
                batch.status = AssetTransferStatus.CANCELLED
                localStoreRepository.setBatchData(batch)
                batch.utxos.forEach {
                    localStoreRepository.blacklistAddress(address = BlacklistedAddressModel(address = it.address), blacklist = false)
                }
            } else if(batch.status == AssetTransferStatus.IN_PROGRESS) {
                processInProgressBatch(batch, wallet)
                batch.utxos.forEach {
                    localStoreRepository.blacklistAddress(address = BlacklistedAddressModel(address = it.address), blacklist = false)
                }
            } else if(batch.status != AssetTransferStatus.CANCELLED) {
                batch.utxos.forEach {
                    localStoreRepository.blacklistAddress(address = BlacklistedAddressModel(address = it.address), blacklist = false)
                }
            }
        }
    }

    suspend fun updateTransfers(wallet: LocalWalletModel, findWallet: (String) -> LocalWalletModel?, job: Job?): Boolean {
        val transfers = localStoreRepository.getAllAssetTransfers(wallet.uuid)
        var utxoSent = false

        transfers.lastOrNull {
            it.status == AssetTransferStatus.WAITING
                    || it.status == AssetTransferStatus.IN_PROGRESS || it.status == AssetTransferStatus.NOT_STARTED
        }?.let { // run the most recent executing transfer to prevent "hacking" the protocol by creating many 50 utxo single batch transfers
            job?.ensureActive()
            if (processTransfer(it, wallet, findWallet, job))
                utxoSent = true
        }

        transfers.filter {
            it.status == AssetTransferStatus.CANCELLED
        }.forEach {
            cancelTransfer(it, wallet)
        }

        return utxoSent
    }

    suspend fun cancelTransfer(transferId: String, wallet: LocalWalletModel) {
        val transfers = localStoreRepository.getAllAssetTransfers(wallet.uuid)

        transfers.firstOrNull {
            it.id == transferId
        }?.let {
            if(it.status.id in AssetTransferStatus.NOT_STARTED.id..AssetTransferStatus.IN_PROGRESS.id) {
                it.status = AssetTransferStatus.CANCELLED
                localStoreRepository.saveAssetTransfer(it)
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