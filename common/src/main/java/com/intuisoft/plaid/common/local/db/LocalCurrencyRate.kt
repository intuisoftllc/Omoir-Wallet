package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.model.LocalCurrencyRateModel
import com.intuisoft.plaid.common.model.NetworkFeeRate
import com.intuisoft.plaid.common.util.Constants

@Entity(tableName = "local_currency_rate")
data class LocalCurrencyRate(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "rate") var rate: Double,
    @ColumnInfo(name = "currency_code") var currencyCode: String
) {
    fun from() =
        LocalCurrencyRateModel(
            rate = rate,
            currencyCode = currencyCode
        )

    companion object {

        fun consume(currencyCode: String, rate: Double): LocalCurrencyRate {
            var id = 0
            when(currencyCode) {
                Constants.LocalCurrency.USD -> {
                    id = 0
                }
                Constants.LocalCurrency.CANADA -> {
                    id = 1
                }
                Constants.LocalCurrency.EURO -> {
                    id = 2
                }
            }

            return LocalCurrencyRate(
                id = id,
                rate = rate,
                currencyCode = currencyCode
            )
        }
    }
}