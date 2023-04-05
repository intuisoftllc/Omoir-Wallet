package com.intuisoft.plaid.common.repositories.db

import com.intuisoft.plaid.common.delegates.market.MarketDataDelegate
import com.intuisoft.plaid.common.local.db.BitcoinStatsData
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.response.BlockStatsData
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel


interface DatabaseRepository {

    suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean)

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?

    suspend fun setBlockStatsData(data: BlockStatsDataModel, testNet: Boolean, coin: String)

    suspend fun getBlockStatsData(testNet: Boolean, coin: String): BlockStatsDataModel?

    suspend fun getBitcoinStatsData(): BitcoinStatsDataModel?

    suspend fun setBitcoinStatsData(data: BitcoinStatsDataModel)

    suspend fun getTickerPriceChartData(currencyCode: String, intervalType: ChartIntervalType, del: MarketDataDelegate): List<ChartDataModel>?

    suspend fun setTickerPriceChartData(data: List<ChartDataModel>, currencyCode: String, intervalType: ChartIntervalType, del: MarketDataDelegate)

    suspend fun setBatchData(data: BatchDataModel)

    suspend fun getBatchDataForTransfer(id: String): List<BatchDataModel>

    suspend fun saveAssetTransfer(data: AssetTransferModel)

    suspend fun getAllAssetTransfers(walletId: String): List<AssetTransferModel>

    suspend fun blacklistTransaction(transaction: BlacklistedTransactionModel, blacklist: Boolean)

    suspend fun getAllBlacklistedTransactions(walletId: String): List<BlacklistedTransactionModel>

    suspend fun getAllBlacklistedTransactions(): List<BlacklistedTransactionModel>

    suspend fun blacklistAddress(address: BlacklistedAddressModel, blacklist: Boolean)

    suspend fun getAllBlacklistedAddresses(): List<BlacklistedAddressModel>

    suspend fun setMemoForTransaction(txId: String, memo: String)

    suspend fun getBasicCoinInfo(id: String): CoinInfoDataModel?

    suspend fun getMemoForTransaction(txid: String): TransactionMemoModel?

    suspend fun setSupportedCurrenciesData(data: List<SupportedCurrencyModel>)

    suspend fun saveExchangeData(data: ExchangeInfoDataModel, walletId: String)

    suspend fun getAllExchanges(walletId: String): List<ExchangeInfoDataModel>

    suspend fun getExchangeById(exchangeId: String): ExchangeInfoDataModel?

    suspend fun getSupportedCurrencies(): List<SupportedCurrencyModel>

    suspend fun setBasicCoinInfo(info: CoinInfoDataModel)

    suspend fun deleteAllData()

    fun setDatabaseListener(databaseListener: DatabaseListener)
}
