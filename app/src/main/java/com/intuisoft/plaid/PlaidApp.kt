package com.intuisoft.plaid

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.local.AppPrefs
import com.intuisoft.plaid.common.local.UserData
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.di.*
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PlaidApp : Application(), Application.ActivityLifecycleCallbacks, KoinComponent {
    private val usrData: UserData?
        get() = CommonService.getUserData()
    var ignorePinCheck = false
    private val billing: BillingManager by inject()
    private val eventTracker: EventTracker by inject()

    override fun onCreate() {
        super.onCreate()
        CommonService.create(
            this,
            BuildConfig.BLOCKCHAIR_CLIENT_SECRET,
            BuildConfig.CHANGE_NOW_CLIENT_SECRET,
            BuildConfig.BLOCKSTREAM_INFO_SERVER_URL,
            BuildConfig.BLOCKSTREAM_INFO_TEST_NET_SERVER_URL,
            BuildConfig.BLOCKCHAIR_SERVER_URL,
            BuildConfig.BLOCKCHAIN_INFO_SERVER_URL,
            BuildConfig.COIN_GECKO_SERVER_URL,
            BuildConfig.CHANGE_NOW_SERVER_URL,
            BuildConfig.WALLET_SECRET,
            BuildConfig.PREMIUM_OVERRIDE,
            BuildConfig.DEVELOPER_ACCESS,
            BuildConfig.COINGECKO_CLIENT_SECRET
        )

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@PlaidApp)
            modules(
                listOf(
                    viewModelModule,
                    preferencesModule,
                    localRepositoriesModule,
                    apiRepositoriesModule,
                    walletManagerModule,
                    analyticsModule,
                    billingModule,
                    delegateManagerModule
                )
            )
        }

        Purchases.debugLogsEnabled = BuildConfig.LOGGING_ENABLED
        registerActivityLifecycleCallbacks(this)
        Purchases.configure(PurchasesConfiguration.Builder(this, BuildConfig.REVENUE_CAT_SECRET).build())
        eventTracker.applyDataTrackingConsent()
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
        if(p0 is MainActivity) {
            billing.onForeground()
            invalidatePinCheckTime()
            ignorePinCheck = false
        }
    }

    override fun onActivityPaused(p0: Activity) {
        if(p0 is MainActivity) {
            billing.onBackground()
            invalidatePinCheckTime()
        }
    }

    fun invalidatePinCheckTime() {
        val time = System.currentTimeMillis() / Constants.Time.MILLS_PER_SEC

        if(!ignorePinCheck &&
            (usrData?.pinTimeout == Constants.Time.INSTANT_TIME_OFFSET
                    || (CommonService.getUserData() != null && (time - CommonService.getUserData()!!.lastCheckPin) > CommonService.getUserData()!!.pinTimeout))) {
            usrData?.lastCheckPin = 0
        }
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }
}