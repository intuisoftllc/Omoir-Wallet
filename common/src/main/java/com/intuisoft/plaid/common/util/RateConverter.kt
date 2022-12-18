package com.intuisoft.plaid.common.util

import com.intuisoft.plaid.common.util.extensions.ignoreNan
import com.intuisoft.plaid.common.util.extensions.roundTo
import kotlin.math.roundToLong

class RateConverter(
    private var fiatRate: Double
) {
    private var localBTC : Double = 0.0 // value represented in sats

    fun getRawRate() = localBTC.roundToLong()

    fun clone(): RateConverter {
        val con = RateConverter(fiatRate)
        con.setLocalRate(RateType.SATOSHI_RATE, localBTC)
        return con
    }

    fun getRawBtcRate() =
        localBTC / Constants.Limit.SATS_PER_BTC

    fun getRawFiatRate() =
        (localBTC / Constants.Limit.SATS_PER_BTC) * fiatRate

    fun getFiatRate() = fiatRate

    fun setFiatRate(fiatRate: Double) {
        this.fiatRate = fiatRate
    }

    fun copyRate(converter: RateConverter) {
        this.localBTC = converter.localBTC
    }


    fun setLocalRate(type: RateType, amount: Double): RateConverter {
        val _amount = amount.ignoreNan()

        if(_amount == 0.0) {
            localBTC = 0.0
        } else {
            when (type) {
                RateType.BTC_RATE -> {
                    localBTC = (_amount.roundTo(8) * Constants.Limit.SATS_PER_BTC)
                }

                RateType.SATOSHI_RATE -> {
                    localBTC = _amount
                }

                RateType.FIAT_RATE -> {
                    localBTC = ((_amount.roundTo(2) / fiatRate) * Constants.Limit.SATS_PER_BTC)
                }
            }
        }

        return this
    }

    fun from(type: RateType): Double {
        when(type) {
            RateType.BTC_RATE -> {
                return getRawBtcRate()
            }

            RateType.SATOSHI_RATE -> {
                return localBTC.roundToLong().toDouble()
            }

            RateType.FIAT_RATE -> {
                return getRawFiatRate()
            }
        }
    }
    fun from(type: RateType, localCurrency: String, shortenSats: Boolean = true) : Pair<String, String> {
        when(type) {
            RateType.SATOSHI_RATE -> {
                val basic: String
                val postfixed: String

                if(shortenSats) {
                    basic = SimpleCoinNumberFormat.formatSatsShort(localBTC.roundToLong())
                    postfixed =
                        prefixPostfixValue(SimpleCoinNumberFormat.formatSatsShort(localBTC.roundToLong()), type)
                } else {
                    basic = SimpleCoinNumberFormat.format(localBTC.roundToLong())!!
                    postfixed =
                        prefixPostfixValue(SimpleCoinNumberFormat.format(localBTC.roundToLong())!!, type)
                }

                return Pair(basic, postfixed)
            }

            RateType.BTC_RATE -> {
                val basic = SimpleCoinNumberFormat.format(getRawBtcRate())!!
                val postfixed = prefixPostfixValue(SimpleCoinNumberFormat.format(getRawBtcRate())!!, type)

                return Pair(basic, postfixed)
            }

            RateType.FIAT_RATE -> {
                val basic = SimpleCurrencyFormat.formatValue(localCurrency, getRawFiatRate(), true)!!
                val postfixed = prefixPostfixValue(SimpleCurrencyFormat.formatValue(localCurrency, getRawFiatRate())!!, type)

                return Pair(basic, postfixed)
            }
        }
    }

    companion object {
        fun prefixPostfixValue(value: String, type: RateType) : String {
            when(type) {
                RateType.SATOSHI_RATE -> {
                    val postfix = if(value == "1") {
                        "Sat"
                    } else "Sats"

                    return "$value $postfix"
                }

                RateType.BTC_RATE -> {
                    return "$value BTC"
                }

                RateType.FIAT_RATE -> {
                    return "$value"
                }
            }
        }
    }

    enum class RateType {
        FIAT_RATE,
        SATOSHI_RATE,
        BTC_RATE
    }
}