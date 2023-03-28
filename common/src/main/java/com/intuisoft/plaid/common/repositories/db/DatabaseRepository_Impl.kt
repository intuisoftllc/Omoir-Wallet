package com.intuisoft.plaid.common.repositories.db

import com.intuisoft.plaid.common.local.db.*
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel

class DatabaseRepository_Impl(
    private val database: PlaidDatabase,
    private val suggestedFeeRateDao: SuggestedFeeRateDao,
    private val basicCoinInfoDao: BasicCoinInfoDao,
    private val extendedNetworkDataDao: ExtendedNetworkDataDao,
    private val tickerCharPriceChartDataDao: TickerPriceChartDataDao,
    private val supportedCurrencyDao: SupportedCurrencyDao,
    private val transactionMemoDao: TransactionMemoDao,
    private val exchangeInfoDao: ExchangeInfoDao,
    private val transferDao: AssetTransferDao,
    private val batchDao: BatchDao,
    private val transactionBlacklistDao: TransactionBlacklistDao,
    private val addressBlacklistDao: AddressBlacklistDao
) : DatabaseRepository {

    override suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate? =
        suggestedFeeRateDao.getFeeRate(testNetWallet)?.from()

    override suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean) {
        suggestedFeeRateDao.insert(SuggestedFeeRate.consume(networkFeeRate, testNetWallet))
        database.onUpdate(suggestedFeeRateDao)
    }

    override suspend fun getExtendedNetworkData(testNetWallet: Boolean): ExtendedNetworkDataModel? =
        extendedNetworkDataDao.getExtendedNetworkData(testNetWallet)?.from()

    override suspend fun setExtendedNetworkData(extendedData: ExtendedNetworkDataModel, testNetWallet: Boolean) {
        extendedNetworkDataDao.insert(ExtendedNetworkData.consume(testNetWallet, extendedData))
        database.onUpdate(extendedNetworkDataDao)
    }

    override suspend fun getTickerPriceChartData(currencyCode: String, intervalType: ChartIntervalType): List<ChartDataModel>? =
        tickerCharPriceChartDataDao.getChartDataFor(intervalType.ordinal, currencyCode)?.from()

    override suspend fun setTickerPriceChartData(data: List<ChartDataModel>, currencyCode: String, intervalType: ChartIntervalType) {
        tickerCharPriceChartDataDao.insert(TickerPriceChartData.consume(intervalType, data, currencyCode))
        database.onUpdate(tickerCharPriceChartDataDao)
    }

    override suspend fun setBatchData(data: BatchDataModel) {
        batchDao.insert(BatchData.consume(data.id, data.transferId, data.batchNumber, data.completionHeight, data.blocksRemaining, data.utxos, data.status))
        database.onUpdate(batchDao)
    }

    override suspend fun getBatchDataForTransfer(id: String): List<BatchDataModel> =
        batchDao.getBatchesForTransfer(id).map { it.from() }

    override suspend fun saveAssetTransfer(data: AssetTransferModel) {
        transferDao.insert(
            AssetTransfer.consume(
                data.id,
                data.walletId,
                data.recipientWallet,
                data.createdAt,
                data.batchGap,
                data.batchSize,
                data.expectedAmount,
                data.retries,
                data.sent,
                data.feesPaid,
                data.feeRangeLow,
                data.feeRangeHigh,
                data.dynamicFees,
                data.status,
                data.batches,
                data.processing
            )
        )
        database.onUpdate(transferDao)
    }

    override suspend fun getAllAssetTransfers(walletId: String): List<AssetTransferModel> =
        transferDao.getAllAssetTransfers(walletId).map { it.from() }

    override suspend fun blacklistTransaction(
        transaction: BlacklistedTransactionModel,
        blacklist: Boolean
    ) {
        if(blacklist)
            transactionBlacklistDao.insert(TransactionBlacklist.consume(transaction.txId, transaction.walletId))
        else transactionBlacklistDao.removeFromBlacklist(transaction.txId)
        database.onUpdate(transactionBlacklistDao)
    }

    override suspend fun getAllBlacklistedTransactions(walletId: String): List<BlacklistedTransactionModel> =
        transactionBlacklistDao.getBlacklistedTransaction(walletId).map { it.from() }

    override suspend fun getAllBlacklistedTransactions(): List<BlacklistedTransactionModel> =
        transactionBlacklistDao.getBlacklistedTransaction().map { it.from() }

    override suspend fun blacklistAddress(
        address: BlacklistedAddressModel,
        blacklist: Boolean
    ) {
        if(blacklist)
            addressBlacklistDao.insert(AddressBlacklist.consume(address.address))
        else addressBlacklistDao.removeFromBlacklist(address.address)
        database.onUpdate(addressBlacklistDao)
    }

    override suspend fun getAllBlacklistedAddresses(): List<BlacklistedAddressModel> =
        addressBlacklistDao.getBlacklistedAddresses().map { it.from() }

    override suspend fun getMemoForTransaction(txid: String): TransactionMemoModel? {
        return transactionMemoDao.getMemoFor(txid)?.from()
    }

    override suspend fun setMemoForTransaction(txId: String, memo: String) {
        transactionMemoDao.insert(TransactionMemo.consume(txId, memo))
        database.onUpdate(transactionMemoDao)
    }

    override suspend fun saveExchangeData(data: ExchangeInfoDataModel, walletId: String) {
        exchangeInfoDao.insert(ExchangeInfoData.consume(data, walletId))
        database.onUpdate(exchangeInfoDao)
    }

    override suspend fun getAllExchanges(walletId: String): List<ExchangeInfoDataModel> {
        return exchangeInfoDao.getAllExchanges(walletId).map { it.from() }
    }

    override suspend fun getExchangeById(exchangeId: String): ExchangeInfoDataModel? {
        return exchangeInfoDao.getExchangeById(exchangeId)?.from()
    }

    override suspend fun setSupportedCurrenciesData(data: List<SupportedCurrencyModel>) {
        supportedCurrencyDao.insert(data.map { SupportedCurrency.consume(it.ticker, it.name, it.image, it.network, it.needsMemo) })
        database.onUpdate(supportedCurrencyDao)
    }

    override suspend fun getSupportedCurrencies(): List<SupportedCurrencyModel> {
        return supportedCurrencyDao.getAllSupportedCurrencies().map { it.from() }
    }

    override suspend fun getBasicCoinInfo(id: String): CoinInfoDataModel? {
        return basicCoinInfoDao.getBasicCoinInfo(id)?.from()
    }

    override suspend fun setBasicCoinInfo(info: CoinInfoDataModel) {
        basicCoinInfoDao.insert(BasicCoinInfo.consume(info))
        database.onUpdate(basicCoinInfoDao)
    }

    override suspend fun deleteAllData() {
        suggestedFeeRateDao.deleteTable() // todo: delete any data realated to wallets when theyre deleted
        basicCoinInfoDao.deleteTable()
        extendedNetworkDataDao.deleteTable()
        tickerCharPriceChartDataDao.deleteTable()
        supportedCurrencyDao.deleteTable()
        exchangeInfoDao.deleteTable()
        batchDao.deleteTable()
        transferDao.deleteTable()
        transactionBlacklistDao.deleteTable()
        addressBlacklistDao.deleteTable()
        database.onUpdate(null)
    }

    override fun setDatabaseListener(databaseListener: DatabaseListener) {
        database.setDatabaseListener(databaseListener)
    }
}
