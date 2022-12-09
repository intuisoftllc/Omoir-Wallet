package com.intuisoft.plaid.di

import android.app.Application
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.AtpManager
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

    single { provideWalletManager(get(), get(), get(), get()) }
    single { provideWalletSyncer(get(), get()) }
    single { provideWalletAtpManager(get(), get(), get(), get()) }
}

fun provideWalletSyncer(
    application: Application,
    localStoreRepository: LocalStoreRepository
): SyncManager {
    return SyncManager(application, localStoreRepository)
}

fun provideWalletAtpManager(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    apiRepository: ApiRepository,
    syncer: SyncManager
): AtpManager {
    return AtpManager(application, localStoreRepository, apiRepository, syncer)
}

fun provideWalletManager(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    syncer: SyncManager,
    atpManager: AtpManager
): AbstractWalletManager {
    return WalletManager(application, localStoreRepository, syncer, atpManager)
}
