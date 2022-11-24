package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.listeners.WipeDataListener
import com.intuisoft.plaid.common.local.db.SupportedCurrency
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.nownodes.response.SupportedCurrencyModel

interface LocalStoreRepository {

    fun increaseIncorrectPinAttempts()

    fun maxPinEntryLimitReached(): Boolean

    fun getPinEntryLimit(): Int

    fun getMinimumConfirmations(): Int

    fun getDevicePerformanceLevel(): DevicePerformanceLevel

    fun setMinConfirmations(minConfirmations: Int)

    fun getLocalCurrency(): String

    fun setLocalCurrency(localCurrency: String)

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

    fun updatePinCheckedTime()

     fun resetPinCheckedTime()

    fun hasPinTimedOut(): Boolean

    fun updatePinTimeout(timeout: Int)

    fun getPinTimeout(): Int

    fun setLastFeeRateUpdate(time: Long)

    fun getLastFeeRateUpdateTime(): Long

    fun setLastCurrencyRateUpdate(time: Long)

    fun getLastCurrencyRateUpdateTime(): Long

    fun setLastSupportedCurrenciesUpdate(time: Long)

    fun getLastSupportedCurrenciesUpdateTime(): Long

    fun setLastBasicNetworkDataUpdate(time: Long)

    fun getLastBasicNetworkDataUpdateTime(): Long

    fun setLastExtendedMarketDataUpdate(time: Long)

    fun getLastExtendedMarketDataUpdateTime(): Long

    fun getLastTickerPriceChartDataUpdateTime(): Long

    fun setLastTickerPriceChartDataUpdate(time: Long)

    fun updateVersionTappedCount()

    fun versionTapLimitReached(): Boolean

    fun updateUserAlias(alias: String)

    fun getUserAlias(): String?

    fun updateUserPin(pin: String)

    fun getUserPin(): String?

    fun setFingerprintEnabled(enabled: Boolean)

    fun getStoredWalletInfo(): StoredWalletInfo

    fun setStoredWalletInfo(storedWalletInfo: StoredWalletInfo?)

    fun isFingerprintEnabled(): Boolean

    fun getRateFor(currencyCode: String): BasicPriceDataModel?

    suspend fun setRates(rates: List<BasicPriceDataModel>)

    fun getBasicNetworkData(): BasicNetworkDataModel?

    suspend fun setBasicNetworkData(circulatingSupply: Long, memPoolTxCount: Int)

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

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?

    fun setDatabaseListener(databaseListener: DatabaseListener)
}