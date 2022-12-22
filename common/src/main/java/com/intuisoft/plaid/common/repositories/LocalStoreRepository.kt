package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.listeners.WipeDataListener
import com.intuisoft.plaid.common.local.db.BatchData
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel

interface LocalStoreRepository {

    fun increaseIncorrectPinAttempts()

    fun maxPinEntryLimitReached(): Boolean

    fun getPinEntryLimit(): Int

    fun getMinimumConfirmations(): Int

    fun setMinConfirmations(minConfirmations: Int)

    fun getLocalCurrency(): String

    fun setLocalCurrency(localCurrency: String)

    fun getBatchGap(): Int

    fun setBatchGap(gap: Int)

    fun getBatchSize(): Int

    fun setBatchSize(size: Int)

    fun getFeeSpread(): IntRange

    fun setFeeSpread(spread: IntRange)

    fun isUsingDynamicBatchNetworkFee(): Boolean

    fun setUseDynamicBatchNetworkFee(use: Boolean)

    fun isProEnabled(): Boolean

    fun setProEnabled(enable: Boolean)

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

    fun setLastCurrencyRateUpdate(time: Long)

    fun getLastCurrencyRateUpdateTime(): Long

    fun setLastSupportedCurrenciesUpdate(time: Long)

    fun getLastSupportedCurrenciesUpdateTime(): Long

    fun setLastExchangeTicker(ticker: String)

    fun getLastExchangeTicker(): String

    fun isSendingBTC(): Boolean

    fun setIsSendingBTC(sending: Boolean)

    fun setLastBasicNetworkDataUpdate(time: Long)

    fun getLastBasicNetworkDataUpdateTime(): Long

    fun setLastExtendedMarketDataUpdate(time: Long)

    fun getLastExtendedMarketDataUpdateTime(): Long

    fun updateVersionTappedCount()

    fun versionTapLimitReached(): Boolean

    fun updateUserAlias(alias: String)

    fun getUserAlias(): String?

    fun hasCompletedOnboarding(): Boolean

    fun setOnboardingComplete(onboardingComplete: Boolean)

    fun setFingerprintEnabled(enabled: Boolean)

    fun getStoredWalletInfo(): StoredWalletInfo

    fun hideHiddenWalletsCount(enabled: Boolean)

    fun isHidingHiddenWalletsCount(): Boolean

    fun setStoredWalletInfo(storedWalletInfo: StoredWalletInfo?)

    fun isFingerprintEnabled(): Boolean

    fun getRateFor(currencyCode: String): BasicPriceDataModel?

    suspend fun setRates(rates: List<BasicPriceDataModel>)

    fun getBasicNetworkData(): BasicNetworkDataModel?

    suspend fun setBasicNetworkData(circulatingSupply: Long)

    fun getExtendedNetworkData(testnetWallet: Boolean): ExtendedNetworkDataModel?

    suspend fun setExtendedNetworkData(testnetWallet: Boolean, extendedData: ExtendedNetworkDataModel)

    fun getSupportedCurrenciesData(fixed: Boolean): List<SupportedCurrencyModel>

    suspend fun setSupportedCurrenciesData(data: List<SupportedCurrencyModel>, fixed: Boolean)

    suspend fun setTransactionMemo(txId: String, memo: String)

    suspend fun getTransactionMemo(txId: String): TransactionMemoModel?

    fun getAllRates(): List<BasicPriceDataModel>

    suspend fun setTickerPriceChartData(data: List<ChartDataModel>, currencyCode: String, intervalType: ChartIntervalType)

    fun getTickerPriceChartData(currencyCode: String, intervalType: ChartIntervalType): List<ChartDataModel>?

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