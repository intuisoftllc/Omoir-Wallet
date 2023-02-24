package com.intuisoft.plaid.common

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.local.AppPrefs
import com.intuisoft.plaid.common.local.UserData
import com.intuisoft.plaid.common.local.db.*
import com.intuisoft.plaid.common.local.memorycache.MemoryCache
import com.intuisoft.plaid.common.network.adapters.LocalDateAdapter
import com.intuisoft.plaid.common.network.blockstreaminfo.api.BlockStreamInfoApi
import com.intuisoft.plaid.common.network.blockstreaminfo.repository.BlockstreamInfoRepository
import com.intuisoft.plaid.common.network.interceptors.ApiKeyInterceptor
import com.intuisoft.plaid.common.network.interceptors.AppConnectionMonitor
import com.intuisoft.plaid.common.network.interceptors.ConnectivityInterceptor
import com.intuisoft.plaid.common.network.blockchair.api.*
import com.intuisoft.plaid.common.network.blockchair.repository.*
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.ApiRepository_Impl
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository_Impl
import com.intuisoft.plaid.common.repositories.db.DatabaseRepository
import com.intuisoft.plaid.common.repositories.db.DatabaseRepository_Impl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object CommonService {

    private var userData: UserData? = null
    private var appPrefs: AppPrefs? = null
    private var localStoreRepository: LocalStoreRepository? = null
    private var apiRepository: ApiRepository? = null
    private var blockchairRepository: BlockchairRepository? = null
    private var blockstreamInfoRepository: BlockstreamInfoRepository? = null
    private var blockstreamInfoTestNetRepository: BlockstreamInfoRepository? = null
    private var blockchainInfoRepository: BlockchainInfoRepository? = null
    private var coingeckoRepository: CoingeckoRepository? = null
    private var testNetBlockchairRepository: BlockchairRepository? = null
    private var databaseRepository: DatabaseRepository? = null
    private var changeNowRepository: ChangeNowRepository? = null
    private var memoryCache: MemoryCache? = null
    private var eventTracker: EventTracker? = null
    private var application: Application? = null
    private var blockchairClientSecret: String? = ""
    private var blockchairApiUrl: String? = ""
    private var blockchairTestNetNodeApiUrl: String? = ""
    private var blockchainInfoApiUrl: String? = ""
    private var blockstreamInfoApiUrl: String? = ""
    private var blockstreamInfoTestNetApiUrl: String? = ""
    private var coingeckoApiUrl: String? = ""
    private var changeNowApiUrl: String? = ""
    private var changeNowSecret: String? = ""
    private var coingeckoSecret: String? = null
    private var localPin: String = ""
    private var walletSecret: String = ""
    private var premiumOverride = false
    private var developerAccess = false

    fun getUserData(): UserData? {
        return userData
    }

    fun getWalletSecret(): String {
        return walletSecret
    }

    fun getAppPrefs(): AppPrefs {
        if(appPrefs == null) {
            appPrefs = provideAppPrefs(
                provideSharedPrefs(
                    getApplication()
                )
            )
        }

        return appPrefs!!
    }

    fun getEventTrackerInstance(): EventTracker {
        if(eventTracker == null) {
            eventTracker = provideEventTracker(
                getApplication()
            )
        }

        return eventTracker!!
    }

    fun getLocalStoreInstance(): LocalStoreRepository {
        if(localStoreRepository == null) {
            localStoreRepository = provideLocalRepository(
                getAppPrefs(),
                getDatabaseRepositoryInstance(),
                getMemoryCacheInstance()
            )
        }

        return localStoreRepository!!
    }

    fun getApiRepositoryInstance(): ApiRepository {
        if(apiRepository == null) {
            apiRepository = provideApiRepository(
                getLocalStoreInstance(),
                getBlockchairRepositoryInstance(),
                getTestBlockchairRepositoryInstance(),
                getBlockchainInfoRepositoryInstance(),
                getBlockstreamInfoRepositoryInstance(),
                getBlockstreamInfoTestNetRepositoryInstance(),
                getCoingeckoRepositoryInstance(),
                getChangeNowRepositoryInstance(),
                getMemoryCacheInstance()
            )
        }

        return apiRepository!!
    }

    fun getBlockchairRepositoryInstance(): BlockchairRepository {
        if(blockchairRepository == null) {
            blockchairRepository = BlockchairRepository.create(
                provideBlockchairApi(
                    provideNowNodesNodeRetrofit(
                        provideBaseHttpClient(
                            provideConnectivityInterceptor()
                        ),
                        getGsonInstance()
                    )
                ),
                blockchairClientSecret
            )
        }

        return blockchairRepository!!
    }

    fun getTestBlockchairRepositoryInstance(): BlockchairRepository {
        if(testNetBlockchairRepository == null) {
            testNetBlockchairRepository = BlockchairRepository.create(
                provideBlockchairApi(
                    provideNowNodesTestNetNodeRetrofit(
                        provideBaseHttpClient(
                            provideConnectivityInterceptor()
                        ),
                        getGsonInstance()
                    )
                ),
                null
            )
        }

        return testNetBlockchairRepository!!
    }

    fun getBlockchainInfoRepositoryInstance(): BlockchainInfoRepository {
        if(blockchainInfoRepository == null) {
            blockchainInfoRepository = BlockchainInfoRepository.create(
                provideBlockchainInfoApi(
                    provideBlockchainInfoRetrofit(
                        provideBaseHttpClient(
                            provideConnectivityInterceptor()
                        ),
                        getGsonInstance()
                    )
                )
            )
        }

        return blockchainInfoRepository!!
    }

    fun getBlockstreamInfoRepositoryInstance(): BlockstreamInfoRepository {
        if(blockstreamInfoRepository == null) {
            blockstreamInfoRepository = BlockstreamInfoRepository.create(
                provideBlockstreamInfoApi(
                    provideBlockstreamInfoRetrofit(
                        provideBaseHttpClient(
                            provideConnectivityInterceptor()
                        ),
                        getGsonInstance()
                    )
                )
            )
        }

        return blockstreamInfoRepository!!
    }

    fun getBlockstreamInfoTestNetRepositoryInstance(): BlockstreamInfoRepository {
        if(blockstreamInfoTestNetRepository == null) {
            blockstreamInfoTestNetRepository = BlockstreamInfoRepository.create(
                provideBlockstreamInfoApi(
                    provideBlockstreamInfoTestNetRetrofit(
                        provideBaseHttpClient(
                            provideConnectivityInterceptor()
                        ),
                        getGsonInstance()
                    )
                )
            )
        }

        return blockstreamInfoTestNetRepository!!
    }

    fun getChangeNowRepositoryInstance(): ChangeNowRepository {
        if(changeNowRepository == null) {
            changeNowRepository = ChangeNowRepository.create(
                provideChangeNowApi(
                    provideChangeNowRetrofit(
                        provideAuthenticatedHttpClient(
                            provideBaseHttpClient(
                                provideConnectivityInterceptor()
                            ),
                            provideApiKeyInterceptor(
                                changeNowSecret!!,
                                "x-changenow-api-key"
                            )
                        ),
                        getGsonInstance()
                    )
                ),
                getLocalStoreInstance()
            )
        }

        return changeNowRepository!!
    }

    fun getCoingeckoRepositoryInstance(): CoingeckoRepository {
        if(coingeckoRepository == null) {
            coingeckoRepository = CoingeckoRepository.create(
                provideCoingeckoApi(
                    provideCoingeckoRetrofit(
                        provideBaseHttpClient(
                            provideConnectivityInterceptor()
                        ),
                        getGsonInstance()
                    )
                ),
                coingeckoSecret
            )
        }

        return coingeckoRepository!!
    }

    fun getDatabaseRepositoryInstance(): DatabaseRepository {
        if(databaseRepository == null) {
            databaseRepository = provideDatabaseRepository(
                provideDatabase(application!!),
                provideSuggestedFeeRateDao(
                    application!!
                ),
                provideLocalCurrencyRateDao(
                    application!!
                ),
                provideBasicMarketDataDao(
                    application!!
                ),
                provideExtendedMarketDataDao(
                    application!!
                ),
                provideTickerPriceDataDao(
                    application!!
                ),
                provideSupportedCurrencyDao(
                    application!!
                ),
                provideTransactionMemoDao(
                    application!!
                ),
                provideExchangeInfoDao(
                    application!!
                ),
                provideAssetTransferDao(
                    application!!
                ),
                provideBatchDao(
                    application!!
                ),
                provideTransactionBlacklistDao(
                    application!!
                ),
                provideAddressBlacklistDao(
                    application!!
                )
            )
        }

        return databaseRepository!!
    }

    fun getGsonInstance() = provideGson()

    fun getApplication() = application!!

    fun getUserPin() = localPin

    fun getPremiumOverride() = premiumOverride

    fun getDeveloperAccess() = developerAccess

    fun getMemoryCacheInstance(): MemoryCache {
        if(memoryCache == null) {
            memoryCache = provideMemoryCache()
        }

        return memoryCache!!
    }

    fun create(
        application: Application,
        blockchairSecret: String?,
        changeNowSecret: String,
        blockstreamInfoURL: String,
        blockstreamInfoTestNetURL: String,
        blockchairApiUrl: String,
        testNetBlockchairApiUrl: String,
        blockchainInfoApiUrl: String,
        coingeckoApiUrl: String,
        changeNowApiUrl: String,
        walletSecret: String,
        premiumOverride: Boolean,
        developerAccess: Boolean,
        coinceckoSecret: String?
    ) {
        provideApplication(application)
        provideBlockchairSecret(blockchairSecret)
        provideChangeNowSecret(changeNowSecret)
        provideBlockstreamInfoApiApiUrl(blockstreamInfoURL)
        provideBlockstreamInfoTestNetApiUrl(blockstreamInfoTestNetURL)
        provideNowNodesNodeApiUrl(blockchairApiUrl)
        provideNowNodesTestNetNodeApiUrl(testNetBlockchairApiUrl)
        provideBlockchainInfoApiUrl(blockchainInfoApiUrl)
        provideCoingeckoApiUrl(coingeckoApiUrl)
        provideChangeNowApiUrl(changeNowApiUrl)
        provideWalletSecret(walletSecret)
        providePremiumOverride(premiumOverride)
        provideDeveloperAccess(developerAccess)
        provideCoingeckoSecret(coinceckoSecret)

        // create singleton instance of all classes
        getAppPrefs()
        getLocalStoreInstance()
        getBlockchairRepositoryInstance()
        getBlockstreamInfoRepositoryInstance()
        getBlockstreamInfoTestNetRepositoryInstance()
        getTestBlockchairRepositoryInstance()
        getDatabaseRepositoryInstance()
        getBlockchainInfoRepositoryInstance()
        getCoingeckoRepositoryInstance()
        getChangeNowRepositoryInstance()
        getEventTrackerInstance()
    }

    // loaders
    fun loadOrSaveUserData(): Boolean {
        if(userData == null)
            userData = UserData.load(localPin)
        else userData?.save()
        return userData != null
    }

    // providers
    fun provideLocalPin(pin: String) {
        this.localPin = pin
    }

    private fun provideApplication(app: Application) {
        this.application = app
    }

    private fun provideBlockchairSecret(secret: String?) {
        this.blockchairClientSecret = secret
    }

    private fun provideChangeNowSecret(secret: String) {
        this.changeNowSecret = secret
    }

    private fun provideBlockstreamInfoApiApiUrl(url: String) {
        this.blockstreamInfoApiUrl = url
    }

    private fun provideBlockstreamInfoTestNetApiUrl(url: String) {
        this.blockstreamInfoTestNetApiUrl = url
    }

    private fun provideNowNodesNodeApiUrl(url: String) {
        this.blockchairApiUrl = url
    }

    private fun provideWalletSecret(secret: String) {
        this.walletSecret = secret
    }

    private fun providePremiumOverride(override: Boolean) {
        this.premiumOverride = override
    }

    private fun provideDeveloperAccess(devAccess: Boolean) {
        this.developerAccess = devAccess
    }

    private fun provideCoingeckoSecret(secret: String?) {
        this.coingeckoSecret = secret
    }

    private fun provideNowNodesTestNetNodeApiUrl(url: String) {
        this.blockchairTestNetNodeApiUrl = url
    }

    private fun provideBlockchainInfoApiUrl(url: String) {
        this.blockchainInfoApiUrl = url
    }

    private fun provideCoingeckoApiUrl(url: String) {
        this.coingeckoApiUrl = url
    }

    private fun provideChangeNowApiUrl(url: String) {
        this.changeNowApiUrl = url
    }

    private fun provideDatabase(
        context: Context
    ): PlaidDatabase {
        return PlaidDatabase.getInstance(context)
    }

    private fun provideSharedPrefs(
        context: Context
    ): SharedPreferences {
        return  context.getSharedPreferences(AppPrefs.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun provideSuggestedFeeRateDao(
        context: Context
    ): SuggestedFeeRateDao {
        return PlaidDatabase.getInstance(context).suggestedFeeRateDao()
    }

    private fun provideLocalCurrencyRateDao(
        context: Context
    ): BasicPriceDataDao {
        return PlaidDatabase.getInstance(context).localCurrencyRateDao()
    }

    private fun provideBasicMarketDataDao(
        context: Context
    ): BaseMarketDataDao {
        return PlaidDatabase.getInstance(context).baseMarketDataDao()
    }

    private fun provideExtendedMarketDataDao(
        context: Context
    ): ExtendedNetworkDataDao {
        return PlaidDatabase.getInstance(context).extendedMarketDataDao()
    }

    private fun provideTickerPriceDataDao(
        context: Context
    ): TickerPriceChartDataDao {
        return PlaidDatabase.getInstance(context).tickerPriceChartDataDao()
    }

    private fun provideSupportedCurrencyDao(
        context: Context
    ): SupportedCurrencyDao {
        return PlaidDatabase.getInstance(context).supportedCurrencyDao()
    }

    private fun provideTransactionMemoDao(
        context: Context
    ): TransactionMemoDao {
        return PlaidDatabase.getInstance(context).transactionMemoDao()
    }

    private fun provideExchangeInfoDao(
        context: Context
    ): ExchangeInfoDao {
        return PlaidDatabase.getInstance(context).exchangeInfoDao()
    }

    private fun provideAssetTransferDao(
        context: Context
    ): AssetTransferDao {
        return PlaidDatabase.getInstance(context).assetTransfersDao()
    }

    private fun provideBatchDao(
        context: Context
    ): BatchDao {
        return PlaidDatabase.getInstance(context).batchDataDao()
    }

    private fun provideTransactionBlacklistDao(
        context: Context
    ): TransactionBlacklistDao {
        return PlaidDatabase.getInstance(context).transactionBlacklistDao()
    }

    private fun provideAddressBlacklistDao(
        context: Context
    ): AddressBlacklistDao {
        return PlaidDatabase.getInstance(context).addressBlacklistDao()
    }

    private fun provideDatabaseRepository(
        database: PlaidDatabase,
        suggestedFeeRateDao: SuggestedFeeRateDao,
        basicPriceDataDao: BasicPriceDataDao,
        baseMarketDataDao: BaseMarketDataDao,
        extendedNetworkDataDao: ExtendedNetworkDataDao,
        tickerPriceChartDataDao: TickerPriceChartDataDao,
        supportedCurrencyDao: SupportedCurrencyDao,
        transactionMemoDao: TransactionMemoDao,
        exchangeInfoDao: ExchangeInfoDao,
        transferDao: AssetTransferDao,
        batchDao: BatchDao,
        transactionBlacklistDao: TransactionBlacklistDao,
        addressBlacklistDao: AddressBlacklistDao
    ): DatabaseRepository {
        return DatabaseRepository_Impl(
            database,
            suggestedFeeRateDao,
            basicPriceDataDao,
            baseMarketDataDao,
            extendedNetworkDataDao,
            tickerPriceChartDataDao,
            supportedCurrencyDao,
            transactionMemoDao,
            exchangeInfoDao,
            transferDao,
            batchDao,
            transactionBlacklistDao,
            addressBlacklistDao
        )
    }

    private fun provideApiKeyInterceptor(
        apiKey: String,
        apiKeyHeaderName: String = "api-key"
    ) = ApiKeyInterceptor(apiKey, apiKeyHeaderName)

    private fun provideConnectivityInterceptor() =
        ConnectivityInterceptor(provideConnectionMonitor(application!!), application!!)

    private fun provideLocalRepository(
        appPrefs: AppPrefs,
        databaseRepository: DatabaseRepository,
        memoryCache: MemoryCache
    ): LocalStoreRepository {
        return LocalStoreRepository_Impl(appPrefs, databaseRepository, memoryCache)
    }

    private fun provideAppPrefs(
        sharedPreferences: SharedPreferences
    ): AppPrefs {
        return AppPrefs(sharedPreferences)
    }

    private fun provideEventTracker(
        application: Application
    ): EventTracker {
        return EventTracker(application)
    }

    private fun provideApiRepository(
        localStoreRepository: LocalStoreRepository,
        blockchairRepository: BlockchairRepository,
        testNetBlockchairRepository: BlockchairRepository,
        blockchainInfoRepository: BlockchainInfoRepository,
        blockstreamInfoRepository: BlockstreamInfoRepository,
        blockstreamInfoTestNetRepository: BlockstreamInfoRepository,
        coingeckoRepository: CoingeckoRepository,
        changeNowRepository: ChangeNowRepository,
        memoryCache: MemoryCache
    ): ApiRepository {
        return ApiRepository_Impl(
            localStoreRepository,
            blockchairRepository,
            testNetBlockchairRepository,
            blockchainInfoRepository,
            coingeckoRepository,
            changeNowRepository,
            blockstreamInfoRepository,
            blockstreamInfoTestNetRepository,
            memoryCache
        )
    }

    private fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()
    }

    private fun provideMemoryCache(): MemoryCache {
        return MemoryCache()
    }

    private fun provideNowNodesNodeRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(blockchairApiUrl!!)
//        .addConverterFactory(NullOnEmptyBodyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun provideNowNodesTestNetNodeRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(blockchairTestNetNodeApiUrl!!)
//        .addConverterFactory(NullOnEmptyBodyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun provideBlockchainInfoRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(blockchainInfoApiUrl!!)
//        .addConverterFactory(NullOnEmptyBodyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun provideBlockstreamInfoRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(blockstreamInfoApiUrl!!)
//        .addConverterFactory(NullOnEmptyBodyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun provideBlockstreamInfoTestNetRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(blockstreamInfoTestNetApiUrl!!)
//        .addConverterFactory(NullOnEmptyBodyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun provideChangeNowRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(changeNowApiUrl!!)
//        .addConverterFactory(NullOnEmptyBodyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun provideCoingeckoRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(coingeckoApiUrl!!)
//        .addConverterFactory(NullOnEmptyBodyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun provideAuthenticatedHttpClient(
        baseHttpClient: OkHttpClient,
        apiKeyInterceptor: ApiKeyInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClient = baseHttpClient.newBuilder()
        httpClient.addInterceptor(logging)
        httpClient.addInterceptor(apiKeyInterceptor)

        return httpClient
            .build()
    }

    private fun provideBaseHttpClient(
        connectivityInterceptor: ConnectivityInterceptor
    ): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(connectivityInterceptor)

        if (BuildConfig.LOGGING_ENABLED) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(loggingInterceptor)
            clientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        }

        return clientBuilder.build()
    }

    private fun provideConnectionMonitor(context: Context): ConnectivityInterceptor.ConnectionMonitor =
        AppConnectionMonitor(context)

    private fun provideBlockchairApi(manager: Retrofit): BlockchairApi =
        manager.create(BlockchairApi::class.java)

    private fun provideBlockchainInfoApi(manager: Retrofit): BlockchainInfoApi =
        manager.create(BlockchainInfoApi::class.java)

    private fun provideBlockstreamInfoApi(manager: Retrofit): BlockStreamInfoApi =
        manager.create(BlockStreamInfoApi::class.java)

    private fun provideChangeNowApi(manager: Retrofit): ChangeNowApi =
        manager.create(ChangeNowApi::class.java)

    private fun provideCoingeckoApi(manager: Retrofit): CoingeckoApi =
        manager.create(CoingeckoApi::class.java)
}