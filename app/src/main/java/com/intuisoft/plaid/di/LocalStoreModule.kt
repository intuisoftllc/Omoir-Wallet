package com.intuisoft.plaid.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import com.intuisoft.plaid.local.UserPreferences
import com.intuisoft.plaid.network.sync.repository.SyncRepository
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.repositories.LocalStoreRepository_Impl
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val preferencesModule = module {
    single(named("userPreferences")) { provideUserPreferences(androidContext()) }
    factory { UserPreferences(get(named("userPreferences")), get()) }
}

val localRepositoriesModule = module {

    single { provideLocalRepository(get()) }
}

val walletManagerModule = module {

    single { provideWalletManager(get(), get(), get()) }
}

fun provideWalletManager(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    syncRepository: SyncRepository
): AbstractWalletManager {
    return WalletManager(application, localStoreRepository, syncRepository)
}

fun provideLocalRepository(
    userPreferences: UserPreferences
): LocalStoreRepository {
    return LocalStoreRepository_Impl(userPreferences)
}

fun provideUserPreferences(context: Context): SharedPreferences {
    return provideEncryptedPreference(context, UserPreferences.SHARED_PREFS_NAME)
}

fun provideEncryptedPreference(context: Context, name: String): SharedPreferences {
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
