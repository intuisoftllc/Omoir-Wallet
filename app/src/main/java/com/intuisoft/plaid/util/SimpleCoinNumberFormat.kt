package com.intuisoft.plaid.util

import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.repositories.LocalStoreRepository
import java.text.DecimalFormat

object SimpleCoinNumberFormat {

    fun format(localStoreRepository: LocalStoreRepository, sats: Long, showFullBalance: Boolean = false) : String {
        // todo: add usd checking support add showUSDIfEnabled: Boolean as a parmeter
        when(localStoreRepository.getBitcoinDisplayUnit()) {
            BitcoinDisplayUnit.BTC -> {
                return "" + format(sats.toDouble() / Constants.Limit.SATS_PER_BTC.toDouble()) + " BTC"
            }

            BitcoinDisplayUnit.SATS -> {
                val postfix = if(sats == 1L) "Sat" else "Sats"

                if(showFullBalance) {
                    return formatFullBalance(sats) + " " + postfix
                } else {
                    return format(sats) + " " + postfix
                }
            }
        }
    }

    private fun format(number: Long) : String {
        if(number <= 0) return "0"

        val df = DecimalFormat("###,###.##")
        when {
            (0..999).contains(number) -> {
                return "" + number
            }
            (1000..9_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1000) + " K"
            }
            (10_000..99_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000) + " K"
            }
            (100_000..999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000) + " K"
            }
            (1_000_000..9_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000) + " Mil"
            }
            (10_000_000..99_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000) + " Mil"
            }
            (100_000_000..999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000) + " Mil"
            }
            (1_000_000_000..9_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000_000) + " Bil"
            }
            (10_000_000_000..99_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000_000) + " Bil"
            }
            (100_000_000_000..999_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000_000) + " Bil"
            }
            (1_000_000_000_000..9_999_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000_000_000) + " Tril"
            }
            (10_000_000_000_000..99_999_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000_000_000) + " Tril"
            }
            (100_000_000_000_000..999_999_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000_000_000) + " Tril"
            }
            else -> {
                return "" + number
            }
        }
    }

    fun format(value: Double): String? {
        val df = DecimalFormat("###,###,###,###.########")
        return df.format(value)
    }

    fun formatBasic(value: Double): String? {
        val df = DecimalFormat("########.########")
        return df.format(value)
    }

    fun formatFullBalance(value: Long): String? {
        val df = DecimalFormat("###,###,###,###,###")
        return df.format(value)
    }
}