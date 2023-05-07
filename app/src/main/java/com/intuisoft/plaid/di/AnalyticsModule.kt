package com.intuisoft.plaid.di

import com.intuisoft.plaid.common.CommonService
import org.koin.dsl.module

val analyticsModule = module {
    factory { CommonService.getEventTrackerInstance() }
}

