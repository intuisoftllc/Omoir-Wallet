package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.delegates.market.MarketDataDelegate
import com.intuisoft.plaid.common.delegates.network.NetworkDataDelegate
import com.intuisoft.plaid.common.listeners.WipeDataListener
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.response.BlockStatsData
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel

interface LocalStoreRepository {

    fun increaseIncorrectPinAttempts()

    fun maxPinEntryLimitReached(): Boolean

    fun getPinEntryLimit(): Int

    fun getMinimumConfirmations(): Int

    fun setMinConfirmations(minConfirmations: Int)

    fun getLocalCurrency(): String

    fun getReportHistoryFilter(): ReportHistoryTimeFilter

    fun setReportHistoryFilter(filter: ReportHistoryTimeFilter)

    fun setLocalCurrency(localCurrency: String)

    fun getBatchGap(): Int

    fun setBatchGap(gap: Int)

    fun getBatchSize(): Int

    fun setBatchSize(size: Int)

    fun getFeeSpread(): IntRange

    fun setFeeSpread(spread: IntRange)

    fun isUsingDynamicBatchNetworkFee(): Boolean

    fun setUseDynamicBatchNetworkFee(use: Boolean)

    fun isPremiumUser(): Boolean

    fun setMaxPinEntryLimit(limit: Int)

    fun resetPinEntries()

    fun setDefaultFeeType(type: FeeType)

    fun getDefaultFeeType(): FeeType

    fun setOnWipeDataListener(databaseListener: WipeDataListener)

    fun updateBitcoinDisplayUnit(displayUnit: BitcoinDisplayUnit)

    fun getBitcoinDisplayUnit(): BitcoinDisplayUnit

    fun getSavedAddresses(): List<SavedAddressModel>

    fun saveAddress(name: String, address: String)

    fun updateSavedAddress(oldName: String, name: String, address: String)

    fun saveBaseWalletSeed(words: List<String>)

    fun updateSavedAccount(oldName: String, account: Int, name: String)

    fun deleteSavedAccount(name: String)

    fun getSavedAccounts(): List<SavedAccountModel>

    fun saveAccount(name: String, account: Int)

    fun getBaseWalletSeed(): List<String>

    fun deleteSavedAddress(name: String)

    fun updateAppTheme(theme: AppTheme)

    fun getAppTheme(): AppTheme

    fun getLastCheckedPinTime(): Long

    fun updatePinCheckedTime()

     fun resetPinCheckedTime()

    fun updatePinTimeout(timeout: Int)

    fun getPinTimeout(): Int

    fun setLastFeeRateUpdate(time: Long)

    fun getLastFeeRateUpdateTime(): Long

    fun setLastSupportedCurrenciesUpdate(time: Long)
    fun setLastBTCChartPriceUpdate(time: Long, type: ChartIntervalType)

    fun getLastSupportedCurrenciesUpdateTime(): Long

    fun setLastExchangeCurrency(id: String)

    fun getLastExchangeCurrency(): String

    fun isSendingBTC(): Boolean

    fun setIsSendingBTC(sending: Boolean)

    fun setLastBTCBlockStatsUpdate(time: Long, testnet: Boolean)

    fun getLastBTCBlockStatsUpdateTime(testnet: Boolean): Long

    fun updateStepsLeftToDeveloper()

    fun isDeveloper(): Boolean

    fun hasDeveloperAccess(): Boolean

    fun stepsLeftToDeveloper(): Int

    fun updateUserAlias(alias: String)

    fun getUserAlias(): String?

    fun hasCompletedOnboarding(): Boolean

    fun setOnboardingComplete(onboardingComplete: Boolean)

    fun setFingerprintEnabled(enabled: Boolean)

    fun getStoredWalletInfo(): StoredWalletInfo

    fun setShowDerivationPathChangeWarning(show: Boolean)

    fun showDerivationPathChangeWarning(): Boolean

    fun hideHiddenWalletsCount(enabled: Boolean)

    fun isHidingHiddenWalletsCount(): Boolean

    fun setUsageDataTrackingEnabled(enabled: Boolean)

    fun isTrackingUsageData(): Boolean

    fun setStoredWalletInfo(storedWalletInfo: StoredWalletInfo?)

    fun isFingerprintEnabled(): Boolean

    fun getBasicCoinInfo(id: String): CoinInfoDataModel?

    suspend fun setBasicCoinInfo(info: CoinInfoDataModel)

    fun getBlockStatsData(testnet: Boolean, del: NetworkDataDelegate): BlockStatsDataModel?

    suspend fun setBlockStatsData(testnet: Boolean, data: BlockStatsDataModel, del: NetworkDataDelegate)

    fun getSupportedCurrenciesData(): List<SupportedCurrencyModel>

    fun getBitcoinStatsData(): BitcoinStatsDataModel?
    suspend fun setBitcoinStatsData(data: BitcoinStatsDataModel)

    suspend fun setSupportedCurrenciesData(data: List<SupportedCurrencyModel>)

    suspend fun setTransactionMemo(txId: String, memo: String)

    suspend fun getTransactionMemo(txId: String): TransactionMemoModel?

    fun getLastBTCStatsUpdateTime(): Long

    fun setLastBTCStatsUpdate(time: Long)

    suspend fun setTickerPriceChartData(data: List<ChartDataModel>, currencyCode: String, intervalType: ChartIntervalType, del: MarketDataDelegate)

    fun getLastBTCChartPriceUpdateTime(type: ChartIntervalType): Long

    fun getTickerPriceChartData(currencyCode: String, intervalType: ChartIntervalType, del: MarketDataDelegate): List<ChartDataModel>?

    suspend fun wipeAllData(onWipeFinished: suspend () -> Unit)

    suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean)

    suspend fun saveExchangeData(data: ExchangeInfoDataModel, walletId: String)

    suspend fun setBatchData(data: BatchDataModel)

    fun clearCache()

    fun getBatchDataForTransfer(id: String): List<BatchDataModel>

    suspend fun saveAssetTransfer(data: AssetTransferModel)

    fun getAllAssetTransfers(walletId: String): List<AssetTransferModel>

    suspend fun blacklistTransaction(transaction: BlacklistedTransactionModel, blacklist: Boolean)

    fun getAllBlacklistedTransactions(walletId: String): List<BlacklistedTransactionModel>

    suspend fun blacklistAddress(address: BlacklistedAddressModel, blacklist: Boolean)

    fun getAllBlacklistedAddresses(): List<BlacklistedAddressModel>

    fun getAllExchanges(walletId: String): List<ExchangeInfoDataModel>

    fun getExchangeById(exchangeId: String): ExchangeInfoDataModel?

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?

    fun setDatabaseListener(databaseListener: DatabaseListener)
}