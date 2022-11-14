package com.intuisoft.plaid

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.core.performance.DevicePerformance
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.local.UserPreferences
import com.intuisoft.plaid.common.model.DevicePerformanceLevel
import com.intuisoft.plaid.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PlaidApp : Application(), Application.ActivityLifecycleCallbacks, KoinComponent {
    private val preferences: UserPreferences by inject()
    private lateinit var devicePerformance: DevicePerformance

    override fun onCreate() {
        super.onCreate()
        CommonService.create(
            this,
            BuildConfig.NOW_NODES_CLIENT_SECRET,
            BuildConfig.BLOCK_BOOK_SERVER_URL,
            BuildConfig.NODE_SERVER_URL,
            BuildConfig.TEST_NET_NODE_SERVER_URL
        )

        devicePerformance = DevicePerformance.create(this)

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
                    blockBooksModule
                )
            )
        }

        registerActivityLifecycleCallbacks(this)
        setupPerformanceLevel()
    }

    fun setupPerformanceLevel() {
        if(preferences.devicePerformanceLevel == null) {
            when {
                devicePerformance.mediaPerformanceClass >= Build.VERSION_CODES.S -> {
                    preferences.devicePerformanceLevel = DevicePerformanceLevel.HIGH
                    // Performance class level 12 and above
                    // Provide the most premium experience for highest performing devices
                }
                devicePerformance.mediaPerformanceClass == Build.VERSION_CODES.R -> {
                    preferences.devicePerformanceLevel = DevicePerformanceLevel.MED
                    // Performance class level 11
                    // Provide a high quality experience
                }
                else -> {
                    preferences.devicePerformanceLevel = DevicePerformanceLevel.DEFAULT
                    // Performance class level undefined
                }
            }
        }
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
        if(preferences.pinTimeout == com.intuisoft.plaid.common.util.Constants.Time.INSTANT_TIME_OFFSET) {
            preferences.lastCheckPin = 0
        }
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }
}