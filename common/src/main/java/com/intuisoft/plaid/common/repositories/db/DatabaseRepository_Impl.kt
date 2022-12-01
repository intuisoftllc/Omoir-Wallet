package com.intuisoft.plaid.common.repositories.db

import com.intuisoft.plaid.common.local.db.*
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel

class DatabaseRepository_Impl(
    private val database: PlaidDatabase,
    private val suggestedFeeRateDao: SuggestedFeeRateDao,
    private val basicPriceDataDao: BasicPriceDataDao,
    private val baseNetworkDataDao: BaseMarketDataDao,
    private val extendedNetworkDataDao: ExtendedNetworkDataDao,
    private val tickerCharPriceChartDataDao: TickerPriceChartDataDao,
    private val supportedCurrencyDao: SupportedCurrencyDao,
    private val transactionMemoDao: TransactionMemoDao,
    private val exchangeInfoDao: ExchangeInfoDao
) : DatabaseRepository {

    override suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate? =
        suggestedFeeRateDao.getFeeRate(testNetWallet)?.from()

    override suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean) {
        suggestedFeeRateDao.insert(SuggestedFeeRate.consume(networkFeeRate, testNetWallet))
        database.onUpdate()
    }

    override suspend fun getExtendedNetworkData(testNetWallet: Boolean): ExtendedNetworkDataModel? =
        extendedNetworkDataDao.getExtendedNetworkData(testNetWallet)?.from()

    override suspend fun setExtendedNetworkData(extendedData: ExtendedNetworkDataModel, testNetWallet: Boolean) {
        extendedNetworkDataDao.insert(ExtendedNetworkData.consume(testNetWallet, extendedData))
        database.onUpdate()
    }

    override suspend fun getTickerPriceChartData(currencyCode: String, intervalType: ChartIntervalType): List<ChartDataModel>? =
        tickerCharPriceChartDataDao.getChartDataFor(intervalType.ordinal, currencyCode)?.from()

    override suspend fun setTickerPriceChartData(data: List<ChartDataModel>, currencyCode: String, intervalType: ChartIntervalType) {
        tickerCharPriceChartDataDao.insert(TickerPriceChartData.consume(intervalType, data, currencyCode))
        database.onUpdate()
    }

    override suspend fun getAllRates(): List<BasicPriceDataModel> {
        return basicPriceDataDao.getAllRates().map { it.from() }
    }

    override suspend fun getMemoForTransaction(txid: String): TransactionMemoModel? {
        return transactionMemoDao.getMemoFor(txid)?.from()
    }

    override suspend fun setMemoForTransaction(txId: String, memo: String) {
        transactionMemoDao.insert(TransactionMemo.consume(txId, memo))
        database.onUpdate()
    }

    override suspend fun saveExchangeData(data: ExchangeInfoDataModel, walletId: String) {
        exchangeInfoDao.insert(ExchangeInfoData.consume(data, walletId))
        database.onUpdate()
    }

    override suspend fun getAllExchanges(walletId: String): List<ExchangeInfoDataModel> {
        return exchangeInfoDao.getAllExchanges(walletId).map { it.from() }
    }

    override suspend fun getExchangeById(exchangeId: String): ExchangeInfoDataModel? {
        return exchangeInfoDao.getExchangeById(exchangeId)?.from()
    }

    override suspend fun setSupportedCurrenciesData(data: List<SupportedCurrencyModel>, fixed: Boolean) {
        supportedCurrencyDao.insert(data.map { SupportedCurrency.consume(it.ticker, it.name, it.image, fixed, it.validAddressRegex, it.validMemoRegex) })
        database.onUpdate()
    }

    override suspend fun getSupportedCurrencies(fixed: Boolean): List<SupportedCurrencyModel> {
        return supportedCurrencyDao.getAllSupportedCurrencies(fixed).map { it.from() }
    }

    override suspend fun getRateFor(currencyCode: String): BasicPriceDataModel? {
        return basicPriceDataDao.getRateFor(currencyCode)?.from()
    }

    override suspend fun getBasicNetworkData(): BasicNetworkDataModel? {
        return baseNetworkDataDao.getNetworkData()?.from()
    }

    override suspend fun setBasicNetworkData(
        circulatingSupply: Long
    ) {
        baseNetworkDataDao.insert(BasicNetworkData.consume(circulatingSupply))
        database.onUpdate()
    }

    override suspend fun setRates(rates: List<BasicPriceDataModel>) {
        basicPriceDataDao.insert(rates.map { BasicPriceData.consume(it.marketCap, it.volume24Hr, it.currencyCode, it.currentPrice) })
        database.onUpdate()
    }

    override suspend fun deleteAllData() {
        suggestedFeeRateDao.deleteTable()
        basicPriceDataDao.deleteTable()
        baseNetworkDataDao.deleteTable()
        extendedNetworkDataDao.deleteTable()
        tickerCharPriceChartDataDao.deleteTable()
        supportedCurrencyDao.deleteTable()
        database.onUpdate()
    }

    override fun setDatabaseListener(databaseListener: DatabaseListener) {
        database.setDatabaseListener(databaseListener)
    }
}
