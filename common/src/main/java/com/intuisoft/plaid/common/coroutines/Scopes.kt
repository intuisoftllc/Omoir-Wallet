package com.intuisoft.plaid.common.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob

object PlaidScope {
    val applicationScope = CoroutineScope(SupervisorJob())

    val MainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}