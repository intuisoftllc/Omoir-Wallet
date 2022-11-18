package com.intuisoft.plaid.common.local.memorycache

import com.intuisoft.plaid.common.model.CurrencyRangeLimitModel
import com.intuisoft.plaid.common.network.nownodes.response.CurrencyRangeLimitResponse

class MemoryCache(

) {
    private val rangeLimits: HashMap<String, Pair<Long, CurrencyRangeLimitModel>> = hashMapOf()

    fun getCurrencySwapRangeLimit(from: String, to: String, fixed: Boolean): CurrencyRangeLimitModel? {
        return rangeLimits.get(from + to + if(fixed) 1 else 0)?.second
    }

    fun getLastCurrencySwapRangeLimitUpdateTime(from: String, to: String, fixed: Boolean): Long? {
        return rangeLimits.get(from + to + if(fixed) 1 else 0)?.first
    }

    fun setCurrencySwapRangeLimit(from: String, to: String, fixed: Boolean, currentTime: Long, limit: CurrencyRangeLimitModel) {
        rangeLimits[from + to + if(fixed) 1 else 0] = currentTime to limit
    }
}