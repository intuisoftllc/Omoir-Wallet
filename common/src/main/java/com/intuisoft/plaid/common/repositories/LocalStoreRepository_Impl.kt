package com.intuisoft.plaid.common.repositories

import androidx.room.Database
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.common.listeners.WipeDataListener
import com.intuisoft.plaid.common.local.AppPrefs
import com.intuisoft.plaid.common.local.UserData
import com.intuisoft.plaid.common.local.db.AddressBlacklistDao
import com.intuisoft.plaid.common.local.db.BasicPriceDataDao
import com.intuisoft.plaid.common.local.db.BatchData
import com.intuisoft.plaid.common.local.db.TransactionBlacklistDao
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.local.memorycache.MemoryCache
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.repositories.db.DatabaseRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.safeWalletScope
import kotlinx.coroutines.*

class LocalStoreRepository_Impl(
    private val appPrefs: AppPrefs,
    private val databaseRepository: DatabaseRepository,
    private val memoryCache: MemoryCache
): LocalStoreRepository, DatabaseListener {

    private var wipeDataListener: WipeDataListener? = null
    private var databaseListener: DatabaseListener? = null

    init {
        databaseRepository.setDatabaseListener(this)
    }

    override fun increaseIncorrectPinAttempts() {
        appPrefs.incorrectPinAttempts = appPrefs.incorrectPinAttempts + 1
    }

    override fun maxPinEntryLimitReached(): Boolean {
        return appPrefs.incorrectPinAttempts >= appPrefs.maxPinAttempts
    }

    override fun getPinEntryLimit(): Int {
        return appPrefs.maxPinAttempts
    }

    override fun getMinimumConfirmations(): Int {
        return CommonService.getUserData()!!.minConfirmations
    }

    override fun getDevicePerformanceLevel(): DevicePerformanceLevel? {
        return CommonService.getAppPrefs().devicePerformanceLevel
    }

    override fun setDevicePerformanceLevel(performanceLevel: DevicePerformanceLevel) {
        CommonService.getAppPrefs().devicePerformanceLevel = performanceLevel
    }

    override fun setMinConfirmations(minConfirmations: Int) {
        CommonService.getUserData()!!.minConfirmations = minConfirmations
    }

    override fun getLocalCurrency(): String {
        return CommonService.getUserData()!!.localCurrency
    }

    override fun setLocalCurrency(localCurrency: String) {
        CommonService.getUserData()!!.localCurrency = localCurrency
    }

    override fun getBatchGap(): Int {
        return CommonService.getUserData()!!.batchGap
    }

    override fun setBatchGap(gap: Int) {
        CommonService.getUserData()!!.batchGap = gap
    }

    override fun getBatchSize(): Int {
        return CommonService.getUserData()!!.batchSize
    }

    override fun setBatchSize(size: Int) {
        CommonService.getUserData()!!.batchSize = size
    }

    override fun getFeeSpread(): IntRange {
        return CommonService.getUserData()!!.feeSpreadLow .. CommonService.getUserData()!!.feeSpreadHigh
    }

    override fun setFeeSpread(spread: IntRange) {
        CommonService.getUserData()!!.feeSpreadLow = spread.first
        CommonService.getUserData()!!.feeSpreadHigh = spread.last
    }

    override fun isUsingDynamicBatchNetworkFee(): Boolean {
        return CommonService.getUserData()!!.dynamicBatchNetworkFee
    }

    override fun setUseDynamicBatchNetworkFee(use: Boolean) {
        CommonService.getUserData()!!.dynamicBatchNetworkFee = use
    }

    override suspend fun blacklistAddress(
        address: BlacklistedAddressModel,
        blacklist: Boolean
    ) {
        databaseRepository.blacklistAddress(address, blacklist)
    }

    override suspend fun blacklistTransaction(
        transaction: BlacklistedTransactionModel,
        blacklist: Boolean
    ) {
        databaseRepository.blacklistTransaction(transaction, blacklist)
    }

    override fun isProEnabled(): Boolean {
        return true//CommonService.getUserData()!!.isProEnabled
    }

    override fun setProEnabled(enable: Boolean) {
        CommonService.getUserData()!!.isProEnabled = enable
    }

    override fun setMaxPinEntryLimit(limit: Int) {
        appPrefs.maxPinAttempts = limit
    }

    override fun resetPinEntries() {
        appPrefs.incorrectPinAttempts = 0
    }

    override fun getDefaultFeeType(): FeeType {
        return CommonService.getUserData()!!.defaultFeeType
    }

    override fun setDefaultFeeType(type: FeeType) {
        CommonService.getUserData()!!.defaultFeeType = type
    }

    override fun setOnWipeDataListener(listener: WipeDataListener) {
        wipeDataListener = listener
    }

    override fun updateBitcoinDisplayUnit(displayUnit: BitcoinDisplayUnit) {
        CommonService.getUserData()!!.bitcoinDisplayUnit = displayUnit
    }

    override fun getSavedAddresses(): List<SavedAddressModel> {
        return CommonService.getUserData()!!.savedAddressInfo.savedAddresses
    }

    override fun deleteSavedAddress(name: String) {
        val addressess = CommonService.getUserData()!!.savedAddressInfo.savedAddresses
        addressess.remove { it.addressName == name }

        CommonService.getUserData()!!.savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun updateSavedAddress(oldName: String, name: String, address: String) {
        val addressess = CommonService.getUserData()!!.savedAddressInfo.savedAddresses
        addressess.find { it.addressName == oldName }?.let {
            it.addressName = name
            it.address = address
        }

        CommonService.getUserData()!!.savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun saveBaseWalletSeed(words: List<String>) {
        CommonService.getUserData()!!.baseWalletSeed = words.joinToString(" ")
    }

    override fun getBaseWalletSeed(): List<String> {
        return CommonService.getUserData()!!.baseWalletSeed?.split(" ") ?: listOf()
    }

    override fun saveAddress(name: String, address: String) {
        val addressess = CommonService.getUserData()!!.savedAddressInfo.savedAddresses
        addressess.add(SavedAddressModel(name, address))

        CommonService.getUserData()!!.savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun getBitcoinDisplayUnit(): BitcoinDisplayUnit {
        return CommonService.getUserData()!!.bitcoinDisplayUnit
    }

    override fun updateAppTheme(theme: AppTheme) {
        CommonService.getAppPrefs().appTheme = theme
    }

    override fun getAppTheme(): AppTheme {
        return CommonService.getAppPrefs().appTheme
    }

    override fun updatePinCheckedTime() {
        CommonService.getUserData()!!.lastCheckPin =
            System.currentTimeMillis()
    }

    override fun resetPinCheckedTime() {
        CommonService.getUserData()!!.lastCheckPin = 0
    }

    override fun updatePinTimeout(timeout: Int) {
        if(timeout == Constants.Time.INSTANT) {
            CommonService.getUserData()!!.pinTimeout = Constants.Time.INSTANT_TIME_OFFSET
        } else {
            CommonService.getUserData()!!.pinTimeout = timeout
        }
    }

    override fun setLastFeeRateUpdate(time: Long) {
        CommonService.getUserData()!!.lastFeeRateUpdateTime = time
    }

    override fun getLastFeeRateUpdateTime(): Long {
        return CommonService.getUserData()!!.lastFeeRateUpdateTime
    }

    override fun setLastCurrencyRateUpdate(time: Long) {
        CommonService.getUserData()!!.lastCurrencyRateUpdateTime = time
    }

    override fun getLastCurrencyRateUpdateTime(): Long {
        return CommonService.getUserData()!!.lastCurrencyRateUpdateTime
    }

    override fun setLastSupportedCurrenciesUpdate(time: Long) {
        CommonService.getUserData()!!.lastSupportedCurrenciesUpdateTime = time
    }

    override fun getLastSupportedCurrenciesUpdateTime(): Long {
        return CommonService.getUserData()!!.lastSupportedCurrenciesUpdateTime
    }

    override fun setLastBasicNetworkDataUpdate(time: Long) {
        CommonService.getUserData()!!.lastBaseMarketDataUpdateTime = time
    }

    override fun getLastExchangeTicker(): String {
        return CommonService.getUserData()!!.lastExchangeTicker
    }

    override fun setIsSendingBTC(sending: Boolean) {
        CommonService.getUserData()!!.exchangeSendBTC = sending
    }

    override fun isSendingBTC(): Boolean {
        return CommonService.getUserData()!!.exchangeSendBTC
    }

    override fun getLastCheckedPinTime(): Long {
        return CommonService.getUserData()!!.lastCheckPin
    }

    override fun setLastExchangeTicker(ticker: String) {
        CommonService.getUserData()!!.lastExchangeTicker = ticker
    }

    override fun getLastBasicNetworkDataUpdateTime(): Long {
        return CommonService.getUserData()!!.lastBaseMarketDataUpdateTime
    }

    override fun setLastExtendedMarketDataUpdate(time: Long) {
        CommonService.getUserData()!!.lastExtendedMarketDataUpdateTime = time
    }

    override fun getLastExtendedMarketDataUpdateTime(): Long {
        return CommonService.getUserData()!!.lastExtendedMarketDataUpdateTime
    }

    override fun getPinTimeout(): Int {
        return CommonService.getUserData()!!.pinTimeout
    }

    override fun updateVersionTappedCount() {
        if(CommonService.getUserData()!!.versionTappedCount < Constants.Limit.VERSION_CODE_TAPPED_LIMIT) {
            CommonService.getUserData()!!.versionTappedCount = CommonService.getUserData()!!.versionTappedCount + 1
        }
    }

    override fun versionTapLimitReached(): Boolean {
        return CommonService.getUserData()!!.versionTappedCount == Constants.Limit.VERSION_CODE_TAPPED_LIMIT
    }

    override fun updateUserAlias(alias: String) {
        appPrefs.alias = alias
    }

    override fun getUserAlias(): String? {
        return appPrefs.alias
    }

    override fun hasCompletedOnboarding(): Boolean {
        return appPrefs.onboardingFinished
    }

    override fun setOnboardingComplete(onboardingComplete: Boolean) {
        appPrefs.onboardingFinished = onboardingComplete
    }

    override fun getStoredWalletInfo(): StoredWalletInfo {
        if(memoryCache.getStoredWalletInfo() == null)
            memoryCache.setStoredWalletInfo(CommonService.getUserData()!!.storedWalletInfo)

        return memoryCache.getStoredWalletInfo()!!
    }

    override fun setStoredWalletInfo(storedWalletInfo: StoredWalletInfo?) {
        memoryCache.setStoredWalletInfo(storedWalletInfo)
        CommonService.getUserData()!!.storedWalletInfo =
            storedWalletInfo ?: StoredWalletInfo(mutableListOf())
    }

    override fun hideHiddenWalletsCount(hide: Boolean) {
        appPrefs.hideHiddenWalletsCount = hide
    }

    override fun isHidingHiddenWalletsCount(): Boolean {
        return appPrefs.hideHiddenWalletsCount
    }

    override fun setFingerprintEnabled(enabled: Boolean) {
        appPrefs.fingerprintSecurity = enabled
    }

    override fun isFingerprintEnabled(): Boolean {
        return appPrefs.fingerprintSecurity
    }

    override fun getRateFor(currencyCode: String): BasicPriceDataModel? {
        return runBlocking {
            if(memoryCache.getRateFor(currencyCode) == null) {
                databaseRepository.getRateFor(currencyCode)?.let {
                    memoryCache.setRateFor(it.currencyCode, it)
                }
            }

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
        circulatingSypply: Long
    ) {
        databaseRepository.setBasicNetworkData(circulatingSypply)
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

    override suspend fun setTransactionMemo(txId: String, memo: String) {
        databaseRepository.setMemoForTransaction(txId, memo)
    }

    override suspend fun getTransactionMemo(txId: String): TransactionMemoModel? {
        return databaseRepository.getMemoForTransaction(txId)
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
        UserData.wipeData()
        CommonService.getAppPrefs().wipeData()
        wipeDataListener?.onWipeData()
        databaseRepository.deleteAllData()
        memoryCache.clear()
        onWipeFinished()
    }

    override suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean) {
        databaseRepository.setSuggestedFeeRate(networkFeeRate, testNetWallet)
    }

    override suspend fun saveExchangeData(data: ExchangeInfoDataModel, walletId: String) {
        databaseRepository.saveExchangeData(data, walletId)
    }

    override fun getAllExchanges(walletId: String): List<ExchangeInfoDataModel> {
        return runBlocking {
            return@runBlocking databaseRepository.getAllExchanges(walletId)
        }
    }

    override fun getAllBlacklistedAddresses(): List<BlacklistedAddressModel> {
        return runBlocking {
            if(memoryCache.getBlacklistedAddresses() == null) {
                memoryCache.setBlacklistedAddresses(databaseRepository.getAllBlacklistedAddresses())
            }

            return@runBlocking memoryCache.getBlacklistedAddresses()!!
        }
    }

    override fun getAllBlacklistedTransactions(walletId: String): List<BlacklistedTransactionModel> {
        return runBlocking {
            if(memoryCache.getBlacklistedTransactions(walletId) == null) {
                memoryCache.setBlacklistedTransactions(databaseRepository.getAllBlacklistedTransactions(walletId))
            }

            return@runBlocking memoryCache.getBlacklistedTransactions(walletId)!!
        }
    }

    override fun getAllAssetTransfers(walletId: String): List<AssetTransferModel> {
        return runBlocking {
            return@runBlocking databaseRepository.getAllAssetTransfers(walletId)
        }
    }

    override fun getBatchDataForTransfer(id: String): List<BatchDataModel> {
        return runBlocking {
            return@runBlocking databaseRepository.getBatchDataForTransfer(id)
        }
    }

    override suspend fun saveAssetTransfer(data: AssetTransferModel) {
        databaseRepository.saveAssetTransfer(data)
    }

    override fun clearCache() {
        memoryCache.clear()
    }

    override suspend fun setBatchData(data: BatchDataModel) {
        databaseRepository.setBatchData(data)
    }

    override fun getExchangeById(exchangeId: String): ExchangeInfoDataModel? {
        return runBlocking {
            return@runBlocking databaseRepository.getExchangeById(exchangeId)
        }
    }

    override suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate? {
        return databaseRepository.getSuggestedFeeRate(testNetWallet)
    }

    override fun onDatabaseUpdated(dao: Any?) {
        CoroutineScope(Dispatchers.IO).launch {
            when(dao) {
                is AddressBlacklistDao -> {
                    memoryCache.setBlacklistedAddresses(databaseRepository.getAllBlacklistedAddresses())
                }

                is TransactionBlacklistDao -> {
                    memoryCache.setBlacklistedTransactions(databaseRepository.getAllBlacklistedTransactions())
                }

                is BasicPriceDataDao -> {
                    databaseRepository.getAllRates().forEach {
                        memoryCache.setRateFor(it.currencyCode, it)
                    }
                }
            }

            withContext(Dispatchers.IO) {
                safeWalletScope {
                    databaseListener?.onDatabaseUpdated(dao)
                }
            }
        }
    }

    override fun setDatabaseListener(databaseListener: DatabaseListener) {
        this.databaseListener = databaseListener
    }
}