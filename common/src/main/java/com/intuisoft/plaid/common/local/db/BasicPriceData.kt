package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.model.BasicPriceDataModel
import com.intuisoft.plaid.common.util.Constants

@Entity(tableName = "basic_price_data")
data class BasicPriceData(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "rate") var rate: Double,
    @ColumnInfo(name = "market_cap") var marketCap: Double,
    @ColumnInfo(name = "currency_code") var currencyCode: String
) {
    fun from() =
        BasicPriceDataModel(
            currentPrice = rate,
            marketCap = marketCap,
            currencyCode = currencyCode
        )

    companion object {

        fun consume(marketCap: Double, currencyCode: String, rate: Double): BasicPriceData {
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

            return BasicPriceData(
                id = id,
                rate = rate,
                marketCap = marketCap,
                currencyCode = currencyCode
            )
        }
    }
}