package com.intuisoft.plaid.common.model

import com.intuisoft.plaid.common.util.RateConverter

enum class BitcoinDisplayUnit(val typeId: Int) {
    SATS(0),
    BTC(1),
    FIAT(2);

    fun toRateType() : RateConverter.RateType {
        when(this) {
            SATS -> {
                return RateConverter.RateType.SATOSHI_RATE
            }
            BTC -> {
                return RateConverter.RateType.BTC_RATE
            }
            FIAT -> {
                return RateConverter.RateType.FIAT_RATE
            }
        }
    }
}