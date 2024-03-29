package io.horizontalsystems.bitcoincore.transactions

import io.horizontalsystems.bitcoincore.core.IPluginData
import io.horizontalsystems.bitcoincore.managers.BloomFilterManager
import io.horizontalsystems.bitcoincore.models.TransactionDataSortType
import io.horizontalsystems.bitcoincore.storage.FullTransaction
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import io.horizontalsystems.bitcoincore.transactions.builder.TransactionBuilder

class TransactionCreator(
        private val builder: TransactionBuilder,
        private val processor: PendingTransactionProcessor,
        private val transactionSender: TransactionSender,
        private val bloomFilterManager: BloomFilterManager) {

    @Throws
    fun create(toAddress: String, value: Long, feeRate: Int, senderPay: Boolean, sortType: TransactionDataSortType, pluginData: Map<Byte, IPluginData>, createOnly: Boolean): FullTransaction {
        if(createOnly)
            return builder.buildTransaction(toAddress, value, feeRate, senderPay, sortType, pluginData)

        return create {
            builder.buildTransaction(toAddress, value, feeRate, senderPay, sortType, pluginData)
        }
    }

    @Throws
    fun create(unspentOutputs: List<Pair<Long, String>>, value: Long, toAddress: String, feeRate: Int, sortType: TransactionDataSortType, createOnly: Boolean, ghostBroadcast: Boolean): FullTransaction {
        if(createOnly)
            return builder.buildTransaction(unspentOutputs, value, toAddress, feeRate, sortType)

        return create(ghostBroadcast) {
            builder.buildTransaction(unspentOutputs, value, toAddress, feeRate, sortType)
        }
    }

    fun canSendTransaction() = transactionSender.canSendTransaction(safeMode = true)

    fun broadcast(transaction: FullTransaction) : FullTransaction {
        try {
            processor.processCreated(transaction)
        } catch (ex: BloomFilterManager.BloomFilterExpired) {
            bloomFilterManager.regenerateBloomFilter()
        }

        try {
            transactionSender.sendPendingTransactions()
        } catch (e: Exception) {
            // ignore any exception since the tx is inserted to the db
        }

        return transaction
    }

    private fun create(ghostBroadcast: Boolean = false, transactionBuilderFunction: () -> FullTransaction): FullTransaction {
        if(!ghostBroadcast) transactionSender.canSendTransaction()
        return broadcast(transactionBuilderFunction.invoke())
    }

    open class TransactionCreationException(msg: String) : Exception(msg)
    class TransactionAlreadyExists(msg: String) : TransactionCreationException(msg)

}
