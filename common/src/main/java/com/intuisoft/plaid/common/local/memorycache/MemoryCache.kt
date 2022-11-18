package com.intuisoft.plaid.common.local.memorycache

import com.intuisoft.plaid.common.model.CurrencyRangeLimitModel
import com.intuisoft.plaid.common.model.WholeCoinConversionModel
import com.intuisoft.plaid.common.network.nownodes.response.CurrencyRangeLimitResponse

class MemoryCache(

) {
    private val rangeLimits: HashMap<String, Pair<Long, CurrencyRangeLimitModel>> = hashMapOf()
    private var wholeCoinConversionFixed: Pair<WholeCoinConversionModel, Long>? = null
    private var wholeCoinConversionFloating: Pair<WholeCoinConversionModel, Long>? = null

    fun getCurrencySwapRangeLimit(from: String, to: String, fixed: Boolean): CurrencyRangeLimitModel? {
        return rangeLimits.get(from + to + if(fixed) 1 else 0)?.second
    }

    fun getLastCurrencySwapRangeLimitUpdateTime(from: String, to: String, fixed: Boolean): Long? {
        return rangeLimits.get(from + to + if(fixed) 1 else 0)?.first
    }

    fun setCurrencySwapRangeLimit(from: String, to: String, fixed: Boolean, currentTime: Long, limit: CurrencyRangeLimitModel) {
        rangeLimits[from + to + if(fixed) 1 else 0] = currentTime to limit
    }

    fun setWholeCoinConversion(from: String, to: String, value: Double, fixed: Boolean, currentTime: Long) {
        if(fixed) {
            wholeCoinConversionFixed = WholeCoinConversionModel(from, to, value) to currentTime
        } else {
            wholeCoinConversionFloating = WholeCoinConversionModel(from, to, value) to currentTime
        }
    }

    fun getWholeCoinConversion(from: String, to: String, fixed: Boolean): Double? {
        if(fixed) {
            if(from == wholeCoinConversionFixed?.first?.from && to == wholeCoinConversionFixed?.first?.to)
                return wholeCoinConversionFixed?.first?.conversion
            else if(from == wholeCoinConversionFixed?.first?.to && to == wholeCoinConversionFixed?.first?.from)
                return 1.0 / (wholeCoinConversionFixed?.first?.conversion ?: 0.0)
            else {
                wholeCoinConversionFixed = null
            }
        } else {
            if(from == wholeCoinConversionFloating?.first?.from && to == wholeCoinConversionFloating?.first?.to)
                return wholeCoinConversionFloating?.first?.conversion
            else if(from == wholeCoinConversionFloating?.first?.to && to == wholeCoinConversionFloating?.first?.from)
                return 1.0 / (wholeCoinConversionFloating?.first?.conversion ?: 0.0)
            else {
                wholeCoinConversionFloating = null
            }
        }

        return null
    }

    fun getLastWholeCoinConversionUpdateTime(fixed: Boolean): Long {
        return if(fixed) wholeCoinConversionFixed?.second ?: 0
        else wholeCoinConversionFloating?.second ?: 0
    }
}