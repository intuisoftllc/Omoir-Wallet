package com.intuisoft.plaid.common.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

object PlaidScope {

    val IoScope = CoroutineScope(Dispatchers.IO)

    val MainScope = CoroutineScope(Dispatchers.Main)

    val GlobalScope = kotlinx.coroutines.GlobalScope
}