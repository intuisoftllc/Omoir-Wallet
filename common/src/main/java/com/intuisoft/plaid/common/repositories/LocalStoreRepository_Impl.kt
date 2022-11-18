package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.common.local.UserPreferences
import com.intuisoft.plaid.common.listeners.WipeDataListener
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.nownodes.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.repositories.db.DatabaseRepository
import kotlinx.coroutines.runBlocking

class LocalStoreRepository_Impl(
    private val userPreferences: UserPreferences,
    private val databaseRepository: DatabaseRepository
): LocalStoreRepository {

    private var wipeDataListener: WipeDataListener? = null
    private var cachedStoredWalletInfo: StoredWalletInfo? = null
    private var cachedLocalCurrency: StoredWalletInfo? = null

    override fun increaseIncorrectPinAttempts() {
        userPreferences.incorrectPinAttempts = userPreferences.incorrectPinAttempts + 1
    }

    override fun maxPinEntryLimitReached(): Boolean {
        return userPreferences.incorrectPinAttempts >= userPreferences.maxPinAttempts
    }

    override fun getPinEntryLimit(): Int {
        return userPreferences.maxPinAttempts
    }

    override fun getMinimumConfirmations(): Int {
        return userPreferences.minConfirmations
    }

    override fun getDevicePerformanceLevel(): DevicePerformanceLevel {
        return userPreferences.devicePerformanceLevel ?: DevicePerformanceLevel.DEFAULT
    }

    override fun setMinConfirmations(minConfirmations: Int) {
        userPreferences.minConfirmations = minConfirmations
    }

    override fun getLocalCurrency(): String {
        return userPreferences.localCurrency
    }

    override fun setLocalCurrency(localCurrency: String) {
        userPreferences.localCurrency = localCurrency
    }

    override fun isProEnabled(): Boolean {
        return userPreferences.isProEnabled
    }

    override fun setProEnabled(enable: Boolean) {
        userPreferences.isProEnabled = enable
    }

    override fun setMaxPinEntryLimit(limit: Int) {
        userPreferences.maxPinAttempts = limit
    }

    override fun resetPinEntries() {
        userPreferences.incorrectPinAttempts = 0
    }

    override fun getDefaultFeeType(): FeeType {
        return userPreferences.defaultFeeType
    }

    override fun setDefaultFeeType(type: FeeType) {
        userPreferences.defaultFeeType = type
    }

    override fun setOnWipeDataListener(listener: WipeDataListener) {
        wipeDataListener = listener
    }

    override fun updateBitcoinDisplayUnit(displayUnit: BitcoinDisplayUnit) {
        userPreferences.bitcoinDisplayUnit = displayUnit
    }

    override fun getSavedAddresses(): List<SavedAddressModel> {
        return userPreferences.savedAddressInfo.savedAddresses
    }

    override fun deleteSavedAddress(name: String) {
        val addressess = userPreferences.savedAddressInfo.savedAddresses
        addressess.remove { it.addressName == name }

        userPreferences.savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun updateSavedAddress(oldName: String, name: String, address: String) {
        val addressess = userPreferences.savedAddressInfo.savedAddresses
        addressess.find { it.addressName == oldName }?.let {
            it.addressName = name
            it.address = address
        }

        userPreferences.savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun saveBaseWalletSeed(words: List<String>) {
        userPreferences.baseWalletSeed = words.joinToString(" ")
    }

    override fun getBaseWalletSeed(): List<String> {
        return userPreferences.baseWalletSeed?.split(" ") ?: listOf()
    }

    override fun saveAddress(name: String, address: String) {
        val addressess = userPreferences.savedAddressInfo.savedAddresses
        addressess.add(SavedAddressModel(name, address))

        userPreferences.savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun getBitcoinDisplayUnit(): BitcoinDisplayUnit {
        return userPreferences.bitcoinDisplayUnit
    }

    override fun updateAppTheme(theme: AppTheme) {
        userPreferences.appTheme = theme
    }

    override fun getAppTheme(): AppTheme {
        return userPreferences.appTheme
    }

    override fun updatePinCheckedTime() {
        userPreferences.lastCheckPin =
            (System.currentTimeMillis() / 1000).toInt()
    }

    override fun resetPinCheckedTime() {
        userPreferences.lastCheckPin = 0
    }

    override fun hasPinTimedOut(): Boolean {
        val time = System.currentTimeMillis() / 1000

        return userPreferences.lastCheckPin == 0 ||
                (time - userPreferences.lastCheckPin) > userPreferences.pinTimeout
    }

    override fun updatePinTimeout(timeout: Int) {
        if(timeout == com.intuisoft.plaid.common.util.Constants.Time.INSTANT) {
            userPreferences.pinTimeout = com.intuisoft.plaid.common.util.Constants.Time.INSTANT_TIME_OFFSET
        } else {
            userPreferences.pinTimeout = timeout
        }
    }

    override fun setLastFeeRateUpdate(time: Long) {
        userPreferences.lastFeeRateUpdateTime = time
    }

    override fun getLastFeeRateUpdateTime(): Long {
        return userPreferences.lastFeeRateUpdateTime
    }

    override fun setLastCurrencyRateUpdate(time: Long) {
        userPreferences.lastCurrencyRateUpdateTime = time
    }

    override fun getLastCurrencyRateUpdateTime(): Long {
        return userPreferences.lastCurrencyRateUpdateTime
    }

    override fun setLastSupportedCurrenciesUpdate(time: Long) {
        userPreferences.lastSupportedCurrenciesUpdateTime = time
    }

    override fun getLastSupportedCurrenciesUpdateTime(): Long {
        return userPreferences.lastSupportedCurrenciesUpdateTime
    }

    override fun setLastBasicNetworkDataUpdate(time: Long) {
        userPreferences.lastBaseMarketDataUpdateTime = time
    }

    override fun getLastBasicNetworkDataUpdateTime(): Long {
        return userPreferences.lastBaseMarketDataUpdateTime
    }

    override fun setLastExtendedMarketDataUpdate(time: Long) {
        userPreferences.lastExtendedMarketDataUpdateTime = time
    }

    override fun getLastExtendedMarketDataUpdateTime(): Long {
        return userPreferences.lastExtendedMarketDataUpdateTime
    }

    override fun setLastTickerPriceChartDataUpdate(time: Long) {
        userPreferences.lastTickerPriceChartDataUpdateTime = time
    }

    override fun getLastTickerPriceChartDataUpdateTime(): Long {
        return userPreferences.lastTickerPriceChartDataUpdateTime
    }

    override fun getPinTimeout(): Int {
        return userPreferences.pinTimeout
    }

    override fun updateVersionTappedCount() {
        if(userPreferences.versionTappedCount < com.intuisoft.plaid.common.util.Constants.Limit.VERSION_CODE_TAPPED_LIMIT) {
            userPreferences.versionTappedCount = userPreferences.versionTappedCount + 1
        }
    }

    override fun versionTapLimitReached(): Boolean {
        return userPreferences.versionTappedCount == com.intuisoft.plaid.common.util.Constants.Limit.VERSION_CODE_TAPPED_LIMIT
    }

    override fun updateUserAlias(alias: String) {
        userPreferences.alias = alias
    }

    override fun getUserAlias(): String? {
        return userPreferences.alias
    }

    override fun updateUserPin(pin: String) {
        userPreferences.pin = pin
    }

    override fun getUserPin(): String? {
        return userPreferences.pin
    }

    override fun getStoredWalletInfo(): StoredWalletInfo {
        if(cachedStoredWalletInfo != null)
            return cachedStoredWalletInfo!!

        cachedStoredWalletInfo = userPreferences.storedWalletInfo
        return cachedStoredWalletInfo!!
    }

    override fun setStoredWalletInfo(storedWalletInfo: StoredWalletInfo?) {
        cachedStoredWalletInfo = storedWalletInfo
        userPreferences.storedWalletInfo =
            if(storedWalletInfo == null)
                StoredWalletInfo(mutableListOf())
            else storedWalletInfo
    }

    override fun setFingerprintEnabled(enabled: Boolean) {
        userPreferences.fingerprintSecurity = enabled
    }

    override fun isFingerprintEnabled(): Boolean {
        return userPreferences.fingerprintSecurity
    }

    override fun getRateFor(currencyCode: String): BasicPriceDataModel? {
        return runBlocking {
            return@runBlocking databaseRepository.getRateFor(currencyCode)
        }
    }

    override suspend fun setRates(rates: List<BasicPriceDataModel>) {
        databaseRepository.setRates(rates)
    }

    override fun getBasicNetworkData(): BasicNetworkDataModel? {
        return runBlocking {
            return@runBlocking databaseRepository.getBasicNetworkData()
        }
    }

    override suspend fun setBasicNetworkData(
        circulatingSypply: Long,
        memPoolTxCount: Int
    ) {
        databaseRepository.setBasicNetworkData(circulatingSypply, memPoolTxCount)
    }

    override fun getExtendedNetworkData(testnetWallet: Boolean): ExtendedNetworkDataModel? {
        return runBlocking {
            return@runBlocking databaseRepository.getExtendedNetworkData(testnetWallet)
        }
    }

    override suspend fun setExtendedNetworkData(
        testnetWallet: Boolean,
        extendedData: ExtendedNetworkDataModel
    ) {
        databaseRepository.setExtendedNetworkData(extendedData, testnetWallet)
    }

    override fun getSupportedCurrenciesData(fixed: Boolean): List<SupportedCurrencyModel> {
        return runBlocking {
            return@runBlocking databaseRepository.getSupportedCurrencies(fixed)
        }
    }

    override suspend fun setSupportedCurrenciesData(
        data: List<SupportedCurrencyModel>,
        fixed: Boolean
    ) {
        databaseRepository.setSupportedCurrenciesData(data, fixed)
    }

    override fun getAllRates(): List<BasicPriceDataModel> {
        return runBlocking {
            return@runBlocking databaseRepository.getAllRates()
        }
    }

    override suspend fun setTickerPriceChartData(
        data: List<ChartDataModel>,
        currencyCode: String,
        intervalType: ChartIntervalType
    ) {
        databaseRepository.setTickerPriceChartData(data, currencyCode, intervalType)
    }

    override fun getTickerPriceChartData(
        currencyCode: String,
        intervalType: ChartIntervalType
    ): List<ChartDataModel>? {
        return runBlocking {
            return@runBlocking databaseRepository.getTickerPriceChartData(currencyCode, intervalType)
        }
    }

    override suspend fun wipeAllData(onWipeFinished: suspend () -> Unit) {
        userPreferences.wipeData()
        wipeDataListener?.onWipeData()
        databaseRepository.deleteAllData()
        onWipeFinished()
    }

    override suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean) {
        databaseRepository.setSuggestedFeeRate(networkFeeRate, testNetWallet)
    }

    override suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate? {
        return databaseRepository.getSuggestedFeeRate(testNetWallet)
    }

    override fun setDatabaseListener(databaseListener: DatabaseListener) {
        databaseRepository.setDatabaseListener(databaseListener)
    }
}