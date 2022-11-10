package com.intuisoft.plaid.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository_Impl
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager
import com.intuisoft.plaid.walletmanager.SyncManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val preferencesModule = module {
    factory { CommonService.getPrefsInstance() }
}

val localRepositoriesModule = module {

    factory { CommonService.getLocalStoreInstance() }
}

val apiRepositoriesModule = module {

    factory { CommonService.getApiRepositoryInstance() }
}

val walletManagerModule = module {

    single { provideWalletManager(get(), get(), get()) }
    single { provideWalletSyncer(get()) }
}

fun provideWalletSyncer(
    application: Application
): SyncManager {
    return SyncManager(application)
}

fun provideWalletManager(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    syncer: SyncManager
): AbstractWalletManager {
    return WalletManager(application, localStoreRepository, syncer)
}
