package com.intuisoft.plaid.common

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intuisoft.plaid.common.local.UserPreferences
import com.intuisoft.plaid.common.network.adapters.LocalDateAdapter
import com.intuisoft.plaid.common.network.interceptors.ApiKeyInterceptor
import com.intuisoft.plaid.common.network.interceptors.AppConnectionMonitor
import com.intuisoft.plaid.common.network.interceptors.ConnectivityInterceptor
import com.intuisoft.plaid.common.network.nownodes.api.BlockBookApi
import com.intuisoft.plaid.common.network.nownodes.api.NodeApi
import com.intuisoft.plaid.common.network.nownodes.repository.BlockBookRepository
import com.intuisoft.plaid.common.network.nownodes.repository.NodeRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository_Impl
import com.intuisoft.plaid.common.local.db.PlaidDatabase
import com.intuisoft.plaid.common.local.db.SuggestedFeeRate
import com.intuisoft.plaid.common.local.db.SuggestedFeeRateDao
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.ApiRepository_Impl
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

    private var userPreferences: UserPreferences? = null
    private var localStoreRepository: LocalStoreRepository? = null
    private var apiRepository: ApiRepository? = null
    private var blockBookRepository: BlockBookRepository? = null
    private var nodeRepository: NodeRepository? = null
    private var testNetNodeRepository: NodeRepository? = null
    private var databaseRepository: DatabaseRepository? = null
    private var application: Application? = null
    private var nowNodesClientSecret: String? = ""
    private var nowNodesBlockBookApiUrl: String? = ""
    private var nowNodesNodeApiUrl: String? = ""
    private var nowNodesTestNetNodeApiUrl: String? = ""

    fun getPrefsInstance(): UserPreferences {
        if(userPreferences == null) {
            userPreferences = UserPreferences(provideUserPreferences(application!!), provideGson())
        }

        return userPreferences!!
    }

    fun getLocalStoreInstance(): LocalStoreRepository {
        if(localStoreRepository == null) {
            localStoreRepository = provideLocalRepository(
                getPrefsInstance(),
                getDatabaseRepositoryInstance()
            )
        }

        return localStoreRepository!!
    }

    fun getApiRepositoryInstance(): ApiRepository {
        if(apiRepository == null) {
            apiRepository = provideApiRepository(
                getLocalStoreInstance(),
                getNodeRepositoryInstance(),
                getTestNetNodeRepositoryInstance()
            )
        }

        return apiRepository!!
    }

    fun getBlockBookRepositoryInstance(): BlockBookRepository {
        if(blockBookRepository == null) {
            blockBookRepository = BlockBookRepository.create(
                provideBlockBooksApi(
                    provideNowNodesBlockBookRetrofit(
                        provideAuthenticatedHttpClient(
                            provideBaseHttpClient(
                                provideConnectivityInterceptor()
                            ),
                            provideApiKeyInterceptor()
                        ),
                        getGsonInstance()
                    )
                )
            )
        }

        return blockBookRepository!!
    }

    fun getNodeRepositoryInstance(): NodeRepository {
        if(nodeRepository == null) {
            nodeRepository = NodeRepository.create(
                provideNodeApi(
                    provideNowNodesNodeRetrofit(
                        provideAuthenticatedHttpClient(
                            provideBaseHttpClient(
                                provideConnectivityInterceptor()
                            ),
                            provideApiKeyInterceptor()
                        ),
                        getGsonInstance()
                    )
                )
            )
        }

        return nodeRepository!!
    }

    fun getTestNetNodeRepositoryInstance(): NodeRepository {
        if(testNetNodeRepository == null) {
            testNetNodeRepository = NodeRepository.create(
                provideNodeApi(
                    provideNowNodesTestNetNodeRetrofit(
                        provideAuthenticatedHttpClient(
                            provideBaseHttpClient(
                                provideConnectivityInterceptor()
                            ),
                            provideApiKeyInterceptor()
                        ),
                        getGsonInstance()
                    )
                )
            )
        }

        return testNetNodeRepository!!
    }

    fun getDatabaseRepositoryInstance(): DatabaseRepository {
        if(databaseRepository == null) {
            databaseRepository = provideDatabaseRepository(
                provideDatabase(application!!),
                provideSuggestedFeeRateDao(
                    application!!
                )
            )
        }

        return databaseRepository!!
    }

    fun getGsonInstance() = provideGson()

    fun getNowNodesClientSecret() = nowNodesClientSecret!!

    fun create(
        application: Application,
        nowNodesSecret: String,
        nowNodesBlockBookURL: String,
        nodeApiUrl: String,
        testNetNodeApiUrl: String
    ) {
        provideApplication(application)
        provideNowNodesSecret(nowNodesSecret)
        provideNowNodesBlockBookApiUrl(nowNodesBlockBookURL)
        provideNowNodesNodeApiUrl(nodeApiUrl)
        provideNowNodesTestNetNodeApiUrl(testNetNodeApiUrl)

        // create singleton instance of all classes
        getPrefsInstance()
        getLocalStoreInstance()
        getBlockBookRepositoryInstance()
        getNodeRepositoryInstance()
        getTestNetNodeRepositoryInstance()
        getDatabaseRepositoryInstance()
    }

    // providers
    private fun provideApplication(app: Application) {
        this.application = app
    }

    private fun provideNowNodesSecret(secret: String) {
        this.nowNodesClientSecret = secret
    }

    private fun provideNowNodesBlockBookApiUrl(url: String) {
        this.nowNodesBlockBookApiUrl = url
    }

    private fun provideNowNodesNodeApiUrl(url: String) {
        this.nowNodesNodeApiUrl = url
    }

    private fun provideNowNodesTestNetNodeApiUrl(url: String) {
        this.nowNodesTestNetNodeApiUrl = url
    }

    private fun provideDatabase(
        context: Context
    ): PlaidDatabase {
        return PlaidDatabase.getInstance(context)
    }

    private fun provideSuggestedFeeRateDao(
        context: Context
    ): SuggestedFeeRateDao {
        return PlaidDatabase.getInstance(context).suggestedFeeRateDao()
    }

    private fun provideDatabaseRepository(
        database: PlaidDatabase,
        suggestedFeeRateDao: SuggestedFeeRateDao
    ): DatabaseRepository {
        return DatabaseRepository_Impl(database, suggestedFeeRateDao)
    }

    private fun provideApiKeyInterceptor() =
        ApiKeyInterceptor()

    private fun provideConnectivityInterceptor() =
        ConnectivityInterceptor(provideConnectionMonitor(application!!), application!!)

    private fun provideLocalRepository(
        userPreferences: UserPreferences,
        databaseRepository: DatabaseRepository
    ): LocalStoreRepository {
        return LocalStoreRepository_Impl(userPreferences, databaseRepository)
    }

    private fun provideApiRepository(
        localStoreRepository: LocalStoreRepository,
        nodeRepository: NodeRepository,
        testNetNodeRepository: NodeRepository
    ): ApiRepository {
        return ApiRepository_Impl(localStoreRepository, nodeRepository, testNetNodeRepository)
    }

    private fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()
    }


    private fun provideUserPreferences(context: Context): SharedPreferences {
        return provideEncryptedPreference(context, UserPreferences.SHARED_PREFS_NAME)
    }

    private fun provideEncryptedPreference(context: Context, name: String): SharedPreferences {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
        return EncryptedSharedPreferences.create(
            "plaid_shared_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun provideNowNodesBlockBookRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(nowNodesBlockBookApiUrl!!)
//        .addConverterFactory(NullOnEmptyBodyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun provideNowNodesNodeRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(nowNodesNodeApiUrl!!)
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
            .baseUrl(nowNodesTestNetNodeApiUrl!!)
//        .addConverterFactory(NullOnEmptyBodyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun provideBaseRetrofit(baseHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .client(baseHttpClient)
            .baseUrl(nowNodesBlockBookApiUrl!!)
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

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(loggingInterceptor)
            clientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        }

        return clientBuilder.build()
    }

    private fun provideConnectionMonitor(context: Context): ConnectivityInterceptor.ConnectionMonitor =
        AppConnectionMonitor(context)

    private fun provideBlockBooksApi(manager: Retrofit): BlockBookApi =
        manager.create(BlockBookApi::class.java)

    private fun provideNodeApi(manager: Retrofit): NodeApi =
        manager.create(NodeApi::class.java)
}