package com.intuisoft.plaid.di

import android.app.Application
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager
import com.intuisoft.plaid.walletmanager.SyncManager
import org.koin.dsl.module

val preferencesModule = module {
    factory { CommonService.getUserData() }
}

val localRepositoriesModule = module {

    factory { CommonService.getLocalStoreInstance() }
}

val apiRepositoriesModule = module {

    factory { CommonService.getApiRepositoryInstance() }
}

val walletManagerModule = module {

    single { provideWalletManager(get(), get(), get()) }
    single { provideWalletSyncer(get(), get()) }
}

fun provideWalletSyncer(
    application: Application,
    localStoreRepository: LocalStoreRepository
): SyncManager {
    return SyncManager(application, localStoreRepository)
}

fun provideWalletManager(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    syncer: SyncManager
): AbstractWalletManager {
    return WalletManager(application, localStoreRepository, syncer)
}
