package com.intuisoft.plaid.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intuisoft.plaid.BuildConfig
import com.intuisoft.plaid.network.adapters.LocalDateAdapter
import com.intuisoft.plaid.network.interceptors.ApiKeyInterceptor
import com.intuisoft.plaid.network.interceptors.AppConnectionMonitor
import com.intuisoft.plaid.network.interceptors.ConnectivityInterceptor
import com.intuisoft.plaid.network.sync.api.SyncApi
import com.intuisoft.plaid.network.sync.repository.SyncRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.util.concurrent.TimeUnit

val okhttpModule = module {
    factory { provideGson() }
    single(named("BaseHttpClient")) { provideBaseHttpClient(get()) }
    single(named("AuthenticatedHttpClient")) {
        provideAuthenticatedHttpClient(
            get(named("BaseHttpClient")),
            get()
        )
    }
}

val InterceptorModule = module {
    factory { ConnectivityInterceptor(get(), get()) }
    factory { provideConnectionMonitor(get()) }
    factory { ApiKeyInterceptor() }
}

val retrofitModule = module {
    single(named("BaseRetrofit")) {
        provideBaseRetrofit(get(named("BaseHttpClient")), get())
    }
    single(named("NowNodesRetrofit")) {
        provideNowNodesRetrofit(get(named("AuthenticatedHttpClient")), get())
    }
}

val syncModule = module {
    factory { provideSyncApi(get(named("NowNodesRetrofit"))) }
    factory { SyncRepository.create(get()) }
}

fun provideGson(): Gson {
    return GsonBuilder()
        .setLenient()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()
}

fun provideNowNodesRetrofit(
    okHttpClient: OkHttpClient,
    gson: Gson
): Retrofit {
    return Retrofit.Builder()
        .client(okHttpClient)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .baseUrl(BuildConfig.BLOCK_BOOK_SERVER_URL)
//        .addConverterFactory(NullOnEmptyBodyConverterFactory())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

fun provideBaseRetrofit(baseHttpClient: OkHttpClient, gson: Gson): Retrofit {
    return Retrofit.Builder()
        .client(baseHttpClient)
        .baseUrl(BuildConfig.BLOCK_BOOK_SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

fun provideAuthenticatedHttpClient(
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

fun provideBaseHttpClient(
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

fun provideConnectionMonitor(context: Context): ConnectivityInterceptor.ConnectionMonitor =
    AppConnectionMonitor(context)

fun provideSyncApi(manager: Retrofit): SyncApi =
    manager.create(SyncApi::class.java)
