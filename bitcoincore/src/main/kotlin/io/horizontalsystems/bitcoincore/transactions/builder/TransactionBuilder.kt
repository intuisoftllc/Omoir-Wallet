package io.horizontalsystems.bitcoincore.transactions.builder

import io.horizontalsystems.bitcoincore.core.IPluginData
import io.horizontalsystems.bitcoincore.core.IRecipientSetter
import io.horizontalsystems.bitcoincore.models.TransactionDataSortType
import io.horizontalsystems.bitcoincore.storage.FullTransaction

class TransactionBuilder(
        private val recipientSetter: IRecipientSetter,
        private val outputSetter: OutputSetter,
        private val inputSetter: InputSetter,
        private val signer: TransactionSigner,
        private val lockTimeSetter: LockTimeSetter
) {

    fun buildTransaction(toAddress: String, value: Long, feeRate: Int, senderPay: Boolean, sortType: TransactionDataSortType, pluginData: Map<Byte, IPluginData>): FullTransaction {
        val mutableTransaction = MutableTransaction()

        recipientSetter.setRecipient(mutableTransaction, toAddress, value, pluginData, false)
        inputSetter.setInputs(mutableTransaction, feeRate, senderPay, sortType)
        lockTimeSetter.setLockTime(mutableTransaction)
        outputSetter.setOutputs(mutableTransaction, sortType)
        signer.sign(mutableTransaction)

        return mutableTransaction.build()
    }

    fun buildTransaction(unspentOutputs: List<Pair<Long, String>>, value: Long, toAddress: String, feeRate: Int, sortType: TransactionDataSortType): FullTransaction {
        val mutableTransaction = MutableTransaction(false)

        recipientSetter.setRecipient(mutableTransaction, toAddress, value, mapOf(), false)
        inputSetter.setInputs(mutableTransaction, unspentOutputs, feeRate, true, sortType)
        lockTimeSetter.setLockTime(mutableTransaction)
        outputSetter.setOutputs(mutableTransaction, sortType)
        signer.sign(mutableTransaction)

        return mutableTransaction.build()
    }

    open class BuilderException : Exception() {
        class FeeMoreThanValue : BuilderException()
        class NotSupportedScriptType : BuilderException()
    }
}
