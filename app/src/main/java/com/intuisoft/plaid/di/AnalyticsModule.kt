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

val analyticsModule = module {
    factory { CommonService.getEventTrackerInstance() }
}

