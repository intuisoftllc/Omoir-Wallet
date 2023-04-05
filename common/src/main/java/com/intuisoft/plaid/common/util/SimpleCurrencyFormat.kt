package com.intuisoft.plaid.common.util

import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
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
}