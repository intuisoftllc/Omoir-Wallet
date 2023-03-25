package com.intuisoft.plaid.common.util

import java.text.NumberFormat
import java.util.*


object SimpleCurrencyFormat {

    fun formatTypeBasic(localCurrency: String) : String {
        val currency = Currency.getInstance(localCurrency)
        return "${currency.displayName} (${currency.symbol})"
    }

    fun formatValue(localCurrency: String, amount: Double, removeSymbol: Boolean = false) : String {
        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val currency = Currency.getInstance(localCurrency)
        format.maximumFractionDigits = 2
        format.currency = currency

        if(removeSymbol) {
            return format.format(amount).replace(Currency.getInstance(localCurrency).symbol, "")
        } else {
            return format.format(amount)
                .replace(
                    Currency.getInstance(localCurrency).symbol,
                    trimSymbol(localCurrency) + " "
                )
        }
    }

    fun normalizeSeparators(number: String): Double {
        val nf_in = NumberFormat.getNumberInstance(Locale.getDefault())
        return nf_in.parse(number)?.toDouble() ?: 0.0
    }

    fun getSymbol(localCurrency: String): String {
        return trimSymbol(localCurrency)
    }

    private fun trimSymbol(localCurrency: String): String
    {
        if(localCurrency == Constants.LocalCurrency.CANADA) {
            return Currency.getInstance(localCurrency).symbol.replace("CA", "")
        } else if(localCurrency == Constants.LocalCurrency.AUD) {
            return Currency.getInstance(localCurrency).symbol.replace("A", "")
        } else if(localCurrency == Constants.LocalCurrency.CNY) {
            return Currency.getInstance(localCurrency).symbol.replace("CN", "")
        } else return Currency.getInstance(localCurrency).symbol
    }
    fun getCurrencyCodeId(currencyCode: String): Int {
        return when(currencyCode) {
            Constants.LocalCurrency.USD -> {
                0
            }
            Constants.LocalCurrency.CANADA -> {
                1
            }
            Constants.LocalCurrency.EURO -> {
                2
            }
            Constants.LocalCurrency.AED -> {
                3
            }
            Constants.LocalCurrency.ARS -> {
                4
            }
            Constants.LocalCurrency.AUD -> {
                5
            }
            Constants.LocalCurrency.BDT -> {
                6
            }
            Constants.LocalCurrency.BHD -> {
                7
            }
            Constants.LocalCurrency.CHF -> {
                8
            }
            Constants.LocalCurrency.CNY -> {
                9
            }
            Constants.LocalCurrency.CZK -> {
                10
            }
            Constants.LocalCurrency.GBP -> {
                11
            }
            Constants.LocalCurrency.KRW -> {
                12
            }
            Constants.LocalCurrency.RUB -> {
                13
            }
            Constants.LocalCurrency.PHP -> {
                14
            }
            Constants.LocalCurrency.PKR -> {
                15
            }
            Constants.LocalCurrency.CLP -> {
                16
            }
            else -> 0
        }
    }
}