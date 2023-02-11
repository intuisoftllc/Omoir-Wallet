package com.intuisoft.plaid.di

import android.app.Application
import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.local.AppPrefs
import org.koin.dsl.module

val billingModule = module {
    single { provideBillingManager(CommonService.getAppPrefs(), get()) }
}

fun provideBillingManager(
    appPrefs: AppPrefs,
    application: Application
): BillingManager {
    return BillingManager(appPrefs, application)
}
