package com.intuisoft.plaid.util

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

    fun setFiatRate(fiatRate: Double) {
        this.fiatRate = fiatRate
    }

    fun setLocalRate(type: RateType, amount: Double) {
        when(type) {
            RateType.BTC_RATE -> {
                localBTC = (amount * Constants.Limit.SATS_PER_BTC).toLong()
            }

            RateType.SATOSHI_RATE -> {
                localBTC = amount.toLong()
            }

            RateType.FIAT_RATE -> {
                localBTC = ((amount / fiatRate) * Constants.Limit.SATS_PER_BTC).toLong()
            }
        }
    }

    fun from(type: RateType, shortenSats: Boolean = true) : Pair<String, String> {
        when(type) {
            RateType.SATOSHI_RATE -> {
                val postfix = if(localBTC == 1L) {
                    "Sat"
                } else "Sats"

                val basic: String
                val postfixed: String

                if(shortenSats) {
                    basic = SimpleCoinNumberFormat.formatSatsShort(localBTC)
                    postfixed =
                        SimpleCoinNumberFormat.formatSatsShort(localBTC) + " " + postfix
                } else {
                    basic = SimpleCoinNumberFormat.formatBasic(localBTC.toDouble())!!
                    postfixed =
                        SimpleCoinNumberFormat.format(localBTC.toDouble()) + " " + postfix
                }

                return Pair(basic, postfixed)
            }

            RateType.BTC_RATE -> {
                val basic = SimpleCoinNumberFormat.formatBasic(localBTC.toDouble() / Constants.Limit.SATS_PER_BTC)!!
                val postfixed = SimpleCoinNumberFormat.format(localBTC.toDouble() / Constants.Limit.SATS_PER_BTC) + " BTC"

                return Pair(basic, postfixed)
            }

            RateType.FIAT_RATE -> {
                val basic = SimpleCoinNumberFormat.formatBasic((localBTC.toDouble() / Constants.Limit.SATS_PER_BTC) * fiatRate)!!
                val postfixed = "$ " + SimpleCoinNumberFormat.formatCurrency((localBTC.toDouble() / Constants.Limit.SATS_PER_BTC) * fiatRate)

                return Pair(basic, postfixed)
            }
        }
    }

    enum class RateType {
        FIAT_RATE,
        SATOSHI_RATE,
        BTC_RATE
    }
}