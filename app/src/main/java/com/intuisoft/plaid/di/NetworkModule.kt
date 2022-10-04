package com.intuisoft.plaid.di

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intuisoft.plaid.BuildConfig
import com.intuisoft.plaid.network.adapters.LocalDateAdapter
import com.intuisoft.plaid.network.interceptors.ConnectivityInterceptor
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
            get(named("BaseHttpClient"))
        )
    }
}

val retrofitModule = module {
    single(named("BaseRetrofit")) {
        provideBaseRetrofit(get(named("BaseHttpClient")), get())
    }
    single(named("Retrofit")) {
        provideConsumerRetrofit(get(named("AuthenticatedHttpClient")), get())
    }
}

fun provideGson(): Gson {
    return GsonBuilder()
        .setLenient()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()
}

fun provideConsumerRetrofit(
    okHttpClient: OkHttpClient,
    gson: Gson
): Retrofit {
    return Retrofit.Builder()
        .client(okHttpClient)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .baseUrl(BuildConfig.BASE_SERVER_URL)
//        .addConverterFactory(NullOnEmptyBodyConverterFactory())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

fun provideBaseRetrofit(baseHttpClient: OkHttpClient, gson: Gson): Retrofit {
    return Retrofit.Builder()
        .client(baseHttpClient)
        .baseUrl(BuildConfig.BASE_SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

fun provideAuthenticatedHttpClient(
    baseHttpClient: OkHttpClient
): OkHttpClient {
    val logging = HttpLoggingInterceptor()
    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
    val httpClient = baseHttpClient.newBuilder()
    httpClient.addInterceptor(logging)

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
