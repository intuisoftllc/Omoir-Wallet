package com.intuisoft.plaid

import android.app.Application
import com.intuisoft.plaid.di.okhttpModule
import com.intuisoft.plaid.di.preferencesModule
import com.intuisoft.plaid.di.retrofitModule
import com.intuisoft.plaid.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PlaidApp : Application() {

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
                    retrofitModule
                )
            )
        }
    }
}