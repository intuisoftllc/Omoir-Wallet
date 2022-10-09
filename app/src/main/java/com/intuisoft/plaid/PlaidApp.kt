package com.intuisoft.plaid

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.intuisoft.plaid.di.*
import com.intuisoft.plaid.local.UserPreferences
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.walletmanager.WalletManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PlaidApp : Application(), Application.ActivityLifecycleCallbacks, KoinComponent {
    private val preferences: UserPreferences by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@PlaidApp)
            modules(
                listOf(
                    viewModelModule,
                    preferencesModule,
                    okhttpModule,
                    retrofitModule,
                    databaseModule,
                    InterceptorModule,
                    localRepositoriesModule,
                    walletManagerModule,
                    syncModule,
                    aesEncryptionModule
                )
            )
        }

        registerActivityLifecycleCallbacks(this);
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
        if(preferences.pinTimeout == Constants.Time.INSTANT_TIME_OFFSET) {
            preferences.lastCheckPin = 0
        }
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }
}