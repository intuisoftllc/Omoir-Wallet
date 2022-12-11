package com.intuisoft.plaid.util.entensions

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job

@OptIn(InternalCoroutinesApi::class)
fun Job.ensureActive(): Unit {
    if (!isActive) {
        throw getCancellationException()
    }
}
