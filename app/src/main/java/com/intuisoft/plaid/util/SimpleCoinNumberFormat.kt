package com.intuisoft.plaid.util

import java.text.DecimalFormat

object SimpleCoinNumberFormat {

    fun format(number: Long) : String {
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
                return "" + df.format(number.toDouble() / 10_000) + " K"
            }
            (100_000..999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 100_000) + " K"
            }
            (1_000_000..9_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000) + " M"
            }
            (10_000_000..99_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 10_000_000) + " M"
            }
            (100_000_000..999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 100_000_000) + " M"
            }
            (1_000_000_000..9_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000_000) + " Bil"
            }
            (10_000_000_000..99_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 10_000_000_000) + " Bil"
            }
            (100_000_000_000..999_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 100_000_000_000) + " Bil"
            }
            (1_000_000_000_000..9_999_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 1_000_000_000_000) + " Tril"
            }
            (10_000_000_000_000..99_999_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 10_000_000_000_000) + " Tril"
            }
            (100_000_000_000_000..999_999_999_999_999).contains(number) -> {
                return "" + df.format(number.toDouble() / 100_000_000_000_000) + " Tril"
            }
            else -> {
                return "" + number
            }
        }
    }

    fun format(value: Double): String? {
        val df = DecimalFormat("###,###.########")
        return df.format(value)
    }
}