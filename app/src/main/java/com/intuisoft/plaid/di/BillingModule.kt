package com.intuisoft.plaid.di

import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.local.AppPrefs
import org.koin.dsl.module

val billingModule = module {
    single { provideBillingManager(CommonService.getAppPrefs()) }
}

fun provideBillingManager(
    appPrefs: AppPrefs
): BillingManager {
    return BillingManager(appPrefs)
}
