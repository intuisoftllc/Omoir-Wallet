package com.intuisoft.plaid.di

import com.intuisoft.plaid.common.CommonService
import org.koin.dsl.module

val blockBooksModule = module {
    factory { CommonService.getBlockBookRepositoryInstance() }
}
