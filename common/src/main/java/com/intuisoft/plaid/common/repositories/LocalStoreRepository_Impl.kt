package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.coroutines.PlaidScope
import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.common.listeners.WipeDataListener
import com.intuisoft.plaid.common.local.AppPrefs
import com.intuisoft.plaid.common.local.UserData
import com.intuisoft.plaid.common.local.db.AddressBlacklistDao
import com.intuisoft.plaid.common.local.db.BasicPriceDataDao
import com.intuisoft.plaid.common.local.db.TransactionBlacklistDao
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.local.memorycache.MemoryCache
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.repositories.db.DatabaseRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.errors.EmptyUsrDataErr
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
        return getUserData().minConfirmations
    }

    override fun setMinConfirmations(minConfirmations: Int) {
        getUserData().minConfirmations = minConfirmations
    }

    override fun getLocalCurrency(): String {
        return getUserData().localCurrency
    }

    override fun getReportHistoryFilter(): ReportHistoryTimeFilter {
        return getUserData().reportHistoryTimeFilter
    }

    override fun setReportHistoryFilter(filter: ReportHistoryTimeFilter) {
        getUserData().reportHistoryTimeFilter = filter
    }

    override fun setLocalCurrency(localCurrency: String) {
        getUserData().localCurrency = localCurrency
    }

    override fun getBatchGap(): Int {
        return getUserData().batchGap
    }

    override fun setBatchGap(gap: Int) {
        getUserData().batchGap = gap
    }

    override fun getBatchSize(): Int {
        return getUserData().batchSize
    }

    override fun setBatchSize(size: Int) {
        getUserData().batchSize = size
    }

    override fun getFeeSpread(): IntRange {
        return getUserData().feeSpreadLow .. getUserData().feeSpreadHigh
    }

    override fun setFeeSpread(spread: IntRange) {
        getUserData().feeSpreadLow = spread.first
        getUserData().feeSpreadHigh = spread.last
    }

    override fun isUsingDynamicBatchNetworkFee(): Boolean {
        return getUserData().dynamicBatchNetworkFee
    }

    override fun setUseDynamicBatchNetworkFee(use: Boolean) {
        getUserData().dynamicBatchNetworkFee = use
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

    override fun isPremiumUser(): Boolean {
        return appPrefs.isPremiumUser || CommonService.getPremiumOverride()
    }

    override fun setMaxPinEntryLimit(limit: Int) {
        appPrefs.maxPinAttempts = limit
    }

    override fun resetPinEntries() {
        appPrefs.incorrectPinAttempts = 0
    }

    override fun getDefaultFeeType(): FeeType {
        return getUserData().defaultFeeType
    }

    override fun setDefaultFeeType(type: FeeType) {
        getUserData().defaultFeeType = type
    }

    override fun setOnWipeDataListener(listener: WipeDataListener) {
        wipeDataListener = listener
    }

    override fun updateBitcoinDisplayUnit(displayUnit: BitcoinDisplayUnit) {
        getUserData().bitcoinDisplayUnit = displayUnit
    }

    override fun getSavedAddresses(): List<SavedAddressModel> {
        return getUserData().savedAddressInfo.savedAddresses
    }

    override fun deleteSavedAddress(name: String) {
        val addressess = getUserData().savedAddressInfo.savedAddresses
        addressess.remove { it.addressName == name }

        getUserData().savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun updateSavedAddress(oldName: String, name: String, address: String) {
        val addressess = getUserData().savedAddressInfo.savedAddresses
        addressess.find { it.addressName == oldName }?.let {
            it.addressName = name
            it.address = address
        }

        getUserData().savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun getSavedAccounts(): List<SavedAccountModel> {
        return getUserData().savedAccountInfo.savedAccounts
    }

    override fun deleteSavedAccount(name: String) {
        val accounts = getUserData().savedAccountInfo.savedAccounts
        accounts.remove { it.accountName == name }

        getUserData().savedAccountInfo = SavedAccountInfo(accounts)
    }

    override fun updateSavedAccount(oldName: String, account: Int, name: String) {
        val accounts = getUserData().savedAccountInfo.savedAccounts
        accounts.find { it.accountName == oldName }?.let {
            it.account = account
            it.accountName = name
        }

        getUserData().savedAccountInfo = SavedAccountInfo(accounts)
    }

    override fun saveAccount(name: String, account: Int) {
        val accounts = getUserData().savedAccountInfo.savedAccounts
        accounts.add(SavedAccountModel(name, account, true))

        getUserData().savedAccountInfo = SavedAccountInfo(accounts)
    }

    override fun saveBaseWalletSeed(words: List<String>) {
        getUserData().baseWalletSeed = words.joinToString(" ")
    }

    override fun getBaseWalletSeed(): List<String> {
        return getUserData().baseWalletSeed?.split(" ") ?: listOf()
    }

    override fun saveAddress(name: String, address: String) {
        val addressess = getUserData().savedAddressInfo.savedAddresses
        addressess.add(SavedAddressModel(name, address))

        getUserData().savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun getBitcoinDisplayUnit(): BitcoinDisplayUnit {
        return getUserData().bitcoinDisplayUnit
    }

    override fun updateAppTheme(theme: AppTheme) {
        CommonService.getAppPrefs().appTheme = theme
    }

    override fun getAppTheme(): AppTheme {
        return CommonService.getAppPrefs().appTheme
    }

    override fun updatePinCheckedTime() {
        getUserData().lastCheckPin =
            System.currentTimeMillis() / Constants.Time.MILLS_PER_SEC
    }

    override fun resetPinCheckedTime() {
        getUserData().lastCheckPin = 0
    }

    override fun updatePinTimeout(timeout: Int) {
        if(timeout == Constants.Time.INSTANT) {
            getUserData().pinTimeout = Constants.Time.INSTANT_TIME_OFFSET
        } else {
            getUserData().pinTimeout = timeout
        }
    }

    private fun getUserData(): UserData {
        return CommonService.getUserData() ?: throw EmptyUsrDataErr("")
    }

    override fun setLastFeeRateUpdate(time: Long) {
        getUserData().lastFeeRateUpdateTime = time
    }

    override fun getLastFeeRateUpdateTime(): Long {
        return getUserData().lastFeeRateUpdateTime
    }

    override fun setLastCurrencyRateUpdate(time: Long) {
        getUserData().lastCurrencyRateUpdateTime = time
    }

    override fun getLastCurrencyRateUpdateTime(): Long {
        return getUserData().lastCurrencyRateUpdateTime
    }

    override fun setLastSupportedCurrenciesUpdate(time: Long) {
        getUserData().lastSupportedCurrenciesUpdateTime = time
    }

    override fun setLastChartPriceUpdate(time: Long, type: ChartIntervalType) {
        getUserData().lastChartPriceUpdateTime.put(type.ordinal, time)
    }

    override fun getLastSupportedCurrenciesUpdateTime(): Long {
        return getUserData().lastSupportedCurrenciesUpdateTime
    }

    override fun setLastBasicNetworkDataUpdate(time: Long) {
        getUserData().lastBaseMarketDataUpdateTime = time
    }

    override fun setIsSendingBTC(sending: Boolean) {
        getUserData().exchangeSendBTC = sending
    }

    override fun isSendingBTC(): Boolean {
        return getUserData().exchangeSendBTC
    }

    override fun getLastCheckedPinTime(): Long {
        return getUserData().lastCheckPin
    }

    override fun setLastExchangeCurrency(id: String) {
        getUserData().lastExchangeCurrency = id
    }

    override fun getLastExchangeCurrency(): String {
        return getUserData().lastExchangeCurrency
    }

    override fun getLastBasicNetworkDataUpdateTime(): Long {
        return getUserData().lastBaseMarketDataUpdateTime
    }

    override fun setLastExtendedMarketDataUpdate(time: Long) {
        getUserData().lastExtendedMarketDataUpdateTime = time
    }

    override fun getLastExtendedMarketDataUpdateTime(): Long {
        return getUserData().lastExtendedMarketDataUpdateTime
    }

    override fun getPinTimeout(): Int {
        return getUserData().pinTimeout
    }

    override fun updateStepsLeftToDeveloper() {
        if(!isDeveloper()) {
            getUserData().stepsToDeveloper = getUserData().stepsToDeveloper - 1
        }
    }

    override fun isDeveloper(): Boolean {
        return getUserData().stepsToDeveloper == 0
    }

    override fun hasDeveloperAccess(): Boolean = CommonService.getDeveloperAccess()

    override fun stepsLeftToDeveloper() = getUserData().stepsToDeveloper

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
            memoryCache.setStoredWalletInfo(getUserData().storedWalletInfo)

        return memoryCache.getStoredWalletInfo()!!
    }

    override fun setStoredWalletInfo(storedWalletInfo: StoredWalletInfo?) {
        memoryCache.setStoredWalletInfo(storedWalletInfo)
        getUserData().storedWalletInfo =
            storedWalletInfo ?: StoredWalletInfo(mutableListOf())
    }

    override fun hideHiddenWalletsCount(hide: Boolean) {
        appPrefs.hideHiddenWalletsCount = hide
    }

    override fun isHidingHiddenWalletsCount(): Boolean {
        return appPrefs.hideHiddenWalletsCount
    }

    override fun isTrackingUsageData(): Boolean {
        return appPrefs.trackingConsent
    }

    override fun setUsageDataTrackingEnabled(enabled: Boolean) {
        appPrefs.trackingConsent = enabled
    }

    override fun setFingerprintEnabled(enabled: Boolean) {
        appPrefs.fingerprintSecurity = enabled
    }

    override fun showDerivationPathChangeWarning(): Boolean {
        return appPrefs.showDerivationPathChangeWarning
    }

    override fun setShowDerivationPathChangeWarning(show: Boolean) {
        appPrefs.showDerivationPathChangeWarning = show
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

    override fun getSupportedCurrenciesData(): List<SupportedCurrencyModel> {
        return runBlocking {
            return@runBlocking databaseRepository.getSupportedCurrencies()
        }
    }

    override suspend fun setSupportedCurrenciesData(
        data: List<SupportedCurrencyModel>
    ) {
        databaseRepository.setSupportedCurrenciesData(data)
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

    override fun getLastChartPriceUpdateTime(type: ChartIntervalType): Long {
        return CommonService.getUserData()?.lastChartPriceUpdateTime?.get(type.ordinal) ?: 0
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
            return@runBlocking databaseRepository.getAllExchanges(walletId).sortedBy { it.timestamp.epochSecond }
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
        try {
            setLastCurrencyRateUpdate(0)
            setLastFeeRateUpdate(0)
            setLastSupportedCurrenciesUpdate(0)
            setLastSupportedCurrenciesUpdate(0)
            CommonService.getUserData()!!.lastChartPriceUpdateTime = hashMapOf()
        } catch(_: EmptyUsrDataErr) { }
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
        PlaidScope.applicationScope.launch(Dispatchers.IO) {
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