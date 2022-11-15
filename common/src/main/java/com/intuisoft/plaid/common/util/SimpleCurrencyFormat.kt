package com.intuisoft.plaid.common.util

import java.text.NumberFormat
import java.util.*

object SimpleCurrencyFormat {

    fun formatBasicName(localCurrency: String) : String {
        val currency = Currency.getInstance(localCurrency)
        return "${currency.displayName} (${currency.symbol})"
    }

    fun formatValue(localCurrency: String, amount: Double, removeSymbol: Boolean = false) : String {
        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.setMaximumFractionDigits(2)
        format.setCurrency(Currency.getInstance(localCurrency))

        if(removeSymbol) {
            return format.format(amount).replace(Currency.getInstance(localCurrency).symbol, "")
        } else {
            if(localCurrency == Constants.LocalCurrency.CANADA) {
                return format.format(amount).replace("CA", "")
            } else return format.format(amount)
        }
    }

    fun getSymbol(localCurrency: String): String {
        if(localCurrency == Constants.LocalCurrency.CANADA)
            return "$"
        else return Currency.getInstance(localCurrency).symbol
    }
}