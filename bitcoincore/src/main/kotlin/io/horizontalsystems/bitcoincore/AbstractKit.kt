package io.horizontalsystems.bitcoincore

import io.horizontalsystems.bitcoincore.core.IPluginData
import io.horizontalsystems.bitcoincore.models.*
import io.horizontalsystems.bitcoincore.network.Network
import io.horizontalsystems.bitcoincore.storage.FullTransaction
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import io.horizontalsystems.bitcoincore.transactions.scripts.ScriptType
import io.reactivex.Single

abstract class AbstractKit {

    protected abstract var bitcoinCore: BitcoinCore
    protected abstract var network: Network

    val balance
        get() = bitcoinCore.balance

    val lastBlockInfo
        get() = bitcoinCore.lastBlockInfo

    val networkName: String
        get() = network.javaClass.simpleName

    val syncState get() = bitcoinCore.syncState

    val isRestored get() = bitcoinCore.isRestored

    val watchAccount: Boolean
        get() = bitcoinCore.watchAccount

    fun start() {
        bitcoinCore.start()
    }

    fun stop() {
        bitcoinCore.stop()
    }

    fun restart() {
        stop()
        start()
    }

    fun refresh() {
        bitcoinCore.refresh()
    }

    fun restartIfNoPeersFound() : Boolean {
        return bitcoinCore.restartIfNoPeersFound()
    }

    fun onEnterForeground() {
        bitcoinCore.onEnterForeground()
    }

    fun onEnterBackground() {
        bitcoinCore.onEnterBackground()
    }

    fun getPurpose() =
        bitcoinCore.getPurpose()

    fun getUnspentOutputs() =
        bitcoinCore.getUnspentOutputs()

    fun transactions(fromUid: String? = null, type: TransactionFilterType? = null, limit: Int? = null): Single<List<TransactionInfo>> {
        return bitcoinCore.transactions(fromUid, type, limit)
    }

    fun getAllTransactions(): List<TransactionInfo> {
        return bitcoinCore.getAllTransactions()
    }

    fun getTransaction(hash: String): TransactionInfo? {
        return bitcoinCore.getTransaction(hash)
    }

    fun fee(value: Long, address: String? = null, senderPay: Boolean = true, feeRate: Int, pluginData: Map<Byte, IPluginData> = mapOf()): Long {
        return bitcoinCore.fee(value, address, senderPay, feeRate, pluginData)
    }

    fun fee(unspentOutputs: List<UnspentOutput>, value: Long, address: String? = null, senderPay: Boolean = true, feeRate: Int, pluginData: Map<Byte, IPluginData> = mapOf()): Long {
        return bitcoinCore.fee(unspentOutputs, value, address, senderPay, feeRate, pluginData)
    }

    fun send(address: String, value: Long, senderPay: Boolean = true, feeRate: Int, sortType: TransactionDataSortType, pluginData: Map<Byte, IPluginData> = mapOf(), createOnly: Boolean = false): FullTransaction {
        return bitcoinCore.send(address, value, senderPay, feeRate, sortType, pluginData, createOnly)
    }

    fun send(hash: ByteArray, scriptType: ScriptType, value: Long, senderPay: Boolean = true, feeRate: Int, sortType: TransactionDataSortType, createOnly: Boolean = false): FullTransaction {
        return bitcoinCore.send(hash, scriptType, value, senderPay, feeRate, sortType, createOnly)
    }

    fun redeem(unspentOutput: UnspentOutput,
        value: Long,
        address: String,
        feeRate: Int,
        sortType: TransactionDataSortType,
        ghostBroadcast: Boolean
    , createOnly: Boolean = false): FullTransaction {
        return bitcoinCore.redeem(unspentOutput, value, address, feeRate, sortType, createOnly, ghostBroadcast)
    }

    fun redeem(unspentOutputs: List<UnspentOutput>, value: Long, address: String, feeRate: Int, sortType: TransactionDataSortType = TransactionDataSortType.Shuffle, createOnly: Boolean = false, ghostBroadcast: Boolean = false): FullTransaction {
        return bitcoinCore.redeem(unspentOutputs, value, address, feeRate, sortType, createOnly, ghostBroadcast)
    }

    fun broadcast(fullTransaction: FullTransaction) : FullTransaction {
       return bitcoinCore.broadcast(fullTransaction)
    }

    fun receiveAddress(): String {
        return bitcoinCore.receiveAddress()
    }

    fun receiveAddresses(): List<String> {
        return bitcoinCore.receiveAddresses()
    }

    fun fillGap() {
        bitcoinCore.fillGap()
    }

    fun getMasterPublicKey(mainNet: Boolean, passphraseWallet: Boolean) = bitcoinCore.getMasterPublicKey(mainNet, passphraseWallet)

    fun receivePublicKey(): PublicKey {
        return bitcoinCore.receivePublicKey()
    }

    fun changePublicKey(): PublicKey {
        return bitcoinCore.changePublicKey()
    }

    fun validateAddress(address: String, pluginData: Map<Byte, IPluginData>) {
        bitcoinCore.validateAddress(address, pluginData)
    }

    fun validateAddress(key: PublicKey): String {
        return bitcoinCore.getFullPublicKeyPath(key)
    }

    fun isAddressValid(address: String) : Boolean =
        bitcoinCore.isAddressValid(address)

    fun isPubKeyValid(key: String) : Boolean=
        bitcoinCore.isPubKeyValid(key)

    fun isPubPrivKeyValid(key: String) : Boolean=
        bitcoinCore.isPubPrivKeyValid(key)

    fun parsePaymentAddress(paymentAddress: String): BitcoinPaymentData {
        return bitcoinCore.parsePaymentAddress(paymentAddress)
    }

    fun canSendTransaction() =
        bitcoinCore.canSendTransaction()

    fun showDebugInfo() {
        bitcoinCore.showDebugInfo()
    }

    fun getConnectedPeersCount() = bitcoinCore.getConnectedPeersCount()

    fun statusInfo(): Map<String, Any> {
        return bitcoinCore.statusInfo()
    }

    fun getPublicKeyByPath(path: String): PublicKey {
        return bitcoinCore.getPublicKeyByPath(path)
    }

    fun getFullPublicKeyPath(key: PublicKey): String {
        return bitcoinCore.getFullPublicKeyPath(key)
    }

    fun watchTransaction(filter: TransactionFilter, listener: WatchedTransactionManager.Listener) {
        bitcoinCore.watchTransaction(filter, listener)
    }

    fun maximumSpendableValue(address: String?, feeRate: Int, pluginData: Map<Byte, IPluginData> = mapOf()): Long {
        return bitcoinCore.maximumSpendableValue(address, feeRate, pluginData)
    }

    fun maximumSpendableValue(unspentOutputs: List<UnspentOutput>, address: String?, feeRate: Int, pluginData: Map<Byte, IPluginData> = mapOf()): Long {
        return bitcoinCore.maximumSpendableValue(unspentOutputs, address, feeRate, pluginData)
    }

    fun minimumSpendableValue(address: String?): Int {
        return bitcoinCore.minimumSpendableValue(address)
    }

    fun getRawTransaction(transactionHash: String): String? {
        return bitcoinCore.getRawTransaction(transactionHash)
    }
}
