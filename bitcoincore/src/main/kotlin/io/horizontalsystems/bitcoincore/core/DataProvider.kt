package io.horizontalsystems.bitcoincore.core

import io.horizontalsystems.bitcoincore.blocks.IBlockchainDataListener
import io.horizontalsystems.bitcoincore.extensions.hexToByteArray
import io.horizontalsystems.bitcoincore.extensions.toReversedHex
import io.horizontalsystems.bitcoincore.managers.UnspentOutputProvider
import io.horizontalsystems.bitcoincore.models.*
import io.horizontalsystems.bitcoincore.storage.TransactionWithBlock
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class DataProvider(
        private val storage: IStorage,
        private val unspentOutputProvider: UnspentOutputProvider,
        private val transactionInfoConverter: ITransactionInfoConverter
) : IBlockchainDataListener {

    interface Listener {
        fun onTransactionsUpdate(inserted: List<TransactionInfo>, updated: List<TransactionInfo>)
        fun onTransactionsDelete(hashes: List<String>)
        fun onBalanceUpdate(balance: BalanceInfo)
        fun onLastBlockInfoUpdate(blockInfo: BlockInfo)
    }

    var listener: Listener? = null

    //  Getters
    val balance: BalanceInfo
        get() = unspentOutputProvider.getBalance()

    fun getUnspentOutputs() =
        unspentOutputProvider.getSpendableUtxo()

    var lastBlockInfo: BlockInfo?
        private set

    init {
        lastBlockInfo = storage.lastBlock()?.let {
            blockInfo(it)
        }
    }

    override fun onBlockInsert(block: Block) {
        if (block.height > lastBlockInfo?.height ?: 0) {
            val blockInfo = blockInfo(block)

            lastBlockInfo = blockInfo
            listener?.onLastBlockInfoUpdate(blockInfo)
            listener?.onBalanceUpdate(balance)
        }
    }

    override fun onTransactionsUpdate(inserted: List<Transaction>, updated: List<Transaction>, block: Block?) {
        listener?.onTransactionsUpdate(
                storage.getFullTransactionInfo(inserted.map { TransactionWithBlock(it, block) }).map { transactionInfoConverter.transactionInfo(it) },
                storage.getFullTransactionInfo(updated.map { TransactionWithBlock(it, block) }).map { transactionInfoConverter.transactionInfo(it) }
        )

        listener?.onBalanceUpdate(balance)
    }

    override fun onTransactionsDelete(hashes: List<String>) {
        listener?.onTransactionsDelete(hashes)
        listener?.onBalanceUpdate(balance)
    }

    fun clear() {
        // do nothing
    }

    fun transactions(fromUid: String?, type: TransactionFilterType? = null, limit: Int? = null): Single<List<TransactionInfo>> {
        return Single.create { emitter ->
            val fromTransaction = fromUid?.let { storage.getValidOrInvalidTransaction(it) }
            val transactions = storage.getFullTransactionInfo(fromTransaction, type, limit)
            emitter.onSuccess(transactions.map { transactionInfoConverter.transactionInfo(it) })
        }
    }

    fun getAllTransactions(): List<TransactionInfo> {
        val transactions = storage.getFullTransactionInfo(null, null, null)
        return transactions.map { transactionInfoConverter.transactionInfo(it) }
    }

    fun getRawTransaction(transactionHash: String): String? {
        val hashByteArray = transactionHash.hexToByteArray().reversedArray()
        return storage.getFullTransactionInfo(hashByteArray)?.rawTransaction
                ?: storage.getInvalidTransaction(hashByteArray)?.rawTransaction
    }

    fun getTransaction(transactionHash: String): TransactionInfo? {
        return storage.getFullTransactionInfo(transactionHash.hexToByteArray().reversedArray())?.let {
            transactionInfoConverter.transactionInfo(it)
        }
    }

    private fun blockInfo(block: Block) = BlockInfo(
            block.headerHash.toReversedHex(),
            block.height,
            block.timestamp)

}
