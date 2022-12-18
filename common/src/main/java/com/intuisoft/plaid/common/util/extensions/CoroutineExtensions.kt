package com.intuisoft.plaid.util.entensions

import com.intuisoft.plaid.common.util.errors.ClosedWalletErr
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@OptIn(InternalCoroutinesApi::class)
fun Job.ensureActive(): Unit {
    if (!isActive) {
        throw getCancellationException()
    }
}