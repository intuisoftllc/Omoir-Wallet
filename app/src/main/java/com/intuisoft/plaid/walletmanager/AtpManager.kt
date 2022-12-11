package com.intuisoft.plaid.walletmanager

import android.app.Application
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
        recipient: LocalWalletModel
    ): Boolean {
        if(!NetworkUtil.hasInternet(application)) return false

        if(lastBatch == null ||
            (lastBatch.status.id in AssetTransferStatus.PARTIALLY_COMPLETED.id..AssetTransferStatus.CANCELLED.id)) {

            if(lastBatch != null && transfer.batchGap > 0
                        && (wallet.walletKit!!.lastBlockInfo?.height ?: 0) <= (transfer.batchGap + lastBatch.completionHeight)) {
                batch.status = AssetTransferStatus.WAITING
                localStoreRepository.setBatchData(batch)
                return false
            }

            when (batch.status) {
                AssetTransferStatus.NOT_STARTED -> {

                    var utxoSent = false
                    val random = SecureRandom()
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
                    return false
                }
            }
        }

        return false
    }

    private suspend fun processTransfer(transfer: AssetTransferModel, wallet: LocalWalletModel, findWallet: (String) -> LocalWalletModel?): Boolean {
        if(!NetworkUtil.hasInternet(application)) return false

        if(transfer.status == AssetTransferStatus.IN_PROGRESS || transfer.status == AssetTransferStatus.NOT_STARTED) {
            val batches = localStoreRepository.getBatchDataForTransfer(transfer.id)
            var lastBatch: BatchDataModel? = null
            val recipient = findWallet(transfer.recipientWallet)
            var utxoSent = false

            if (recipient != null) {
                batches.forEach {
                    if(processBatch(it, lastBatch, transfer, wallet, recipient))
                        utxoSent = true
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
            } else if(++transfer.retries >= Constants.Limit.ATP_MAX_RETRY_LIMIT) {
                transfer.retries++
                transfer.status = AssetTransferStatus.FAILED
            }

            localStoreRepository.saveAssetTransfer(transfer)
            return utxoSent
        }

        return false
    }

    suspend fun updateTransfers(wallet: LocalWalletModel, findWallet: (String) -> LocalWalletModel?, job: Job?): Boolean {
        val transfers = localStoreRepository.getAllAssetTransfers(wallet.uuid)
        var utxoSent = false

        transfers.forEach {
            job?.ensureActive()
            if(processTransfer(it, wallet, findWallet))
                utxoSent = true
        }

        return utxoSent
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