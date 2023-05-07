package com.intuisoft.plaid.di

import android.app.Application
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.delegates.wallet.WalletDelegate
import com.intuisoft.plaid.delegates.wallet.btc.AtpManager
import com.intuisoft.plaid.delegates.wallet.btc.WalletManager
import com.intuisoft.plaid.delegates.wallet.btc.SyncManager
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
    single { provideWalletSyncer(get(), get(), get()) }
    single { provideWalletAtpManager(get(), get(), get()) }
}

val delegateManagerModule = module {

    single { CommonService.getDelegateManagerInstance() }
}

fun provideWalletSyncer(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    atpManager: AtpManager
): SyncManager {
    return SyncManager(application, localStoreRepository, atpManager)
}

fun provideWalletAtpManager(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    apiRepository: ApiRepository
): AtpManager {
    return AtpManager(application, localStoreRepository, apiRepository)
}

fun provideWalletManager(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    syncer: SyncManager
): WalletDelegate {
    return WalletManager(application, localStoreRepository, syncer)
}
