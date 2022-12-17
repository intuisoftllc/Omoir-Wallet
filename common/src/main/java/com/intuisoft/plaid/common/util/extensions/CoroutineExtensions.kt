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

public suspend fun <T> ioContext(
    block: suspend CoroutineScope.() -> T
) {
    val none: T
    try {
        withContext(Dispatchers.IO, block)
    } catch (err: ClosedWalletErr) {
        // ignore
    }
}

public suspend fun <T> mainContext(
    block: suspend CoroutineScope.() -> T
) {
    val none: T
    try {
        withContext(Dispatchers.Main, block)
    } catch (err: ClosedWalletErr) {
        // ignore
    }
}