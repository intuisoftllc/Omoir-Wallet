package com.intuisoft.plaid.common.util

import kotlin.math.roundToLong

class RateConverter(
    private var fiatRate: Double
) {
    private var localBTC : Long = 0

    fun getRawRate() = localBTC

    fun clone(): RateConverter {
        val con = RateConverter(fiatRate)
        con.setLocalRate(RateType.SATOSHI_RATE, localBTC.toDouble())
        return con
    }

    fun getRawBtcRate() =
        localBTC.toDouble() / Constants.Limit.SATS_PER_BTC

    fun getRawFiatRate() =
        (localBTC.toDouble() / Constants.Limit.SATS_PER_BTC) * fiatRate

    fun getFiatRate() = fiatRate

    fun setFiatRate(fiatRate: Double) {
        this.fiatRate = fiatRate
    }

    fun copyRate(converter: RateConverter) {
        this.localBTC = converter.localBTC
    }


    fun setLocalRate(type: RateType, amount: Double) {
        when(type) {
            RateType.BTC_RATE -> {
                localBTC = (amount * Constants.Limit.SATS_PER_BTC).roundToLong()
            }

            RateType.SATOSHI_RATE -> {
                localBTC = amount.toLong()
            }

            RateType.FIAT_RATE -> {
                localBTC = ((amount / fiatRate) * Constants.Limit.SATS_PER_BTC).roundToLong()
            }
        }
    }

    fun from(type: RateType, localCurrency: String, shortenSats: Boolean = true) : Pair<String, String> {
        when(type) {
            RateType.SATOSHI_RATE -> {
                val basic: String
                val postfixed: String

                if(shortenSats) {
                    basic = SimpleCoinNumberFormat.formatSatsShort(localBTC)
                    postfixed =
                        prefixPostfixValue(SimpleCoinNumberFormat.formatSatsShort(localBTC), type)
                } else {
                    basic = SimpleCoinNumberFormat.format(localBTC)!!
                    postfixed =
                        prefixPostfixValue(SimpleCoinNumberFormat.format(localBTC)!!, type)
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