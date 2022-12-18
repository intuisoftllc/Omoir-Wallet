package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.model.BasicPriceDataModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat

@Entity(tableName = "basic_price_data")
data class BasicPriceData(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "rate") var rate: Double,
    @ColumnInfo(name = "market_cap") var marketCap: Double,
    @ColumnInfo(name = "volume_24hr") var volume24Hr: Double,
    @ColumnInfo(name = "currency_code") var currencyCode: String
) {
    fun from() =
        BasicPriceDataModel(
            currentPrice = rate,
            marketCap = marketCap,
            volume24Hr = volume24Hr,
            currencyCode = currencyCode
        )

    companion object {

        fun consume(marketCap: Double, volume24Hr: Double, currencyCode: String, rate: Double): BasicPriceData {
            var id = SimpleCurrencyFormat.getCurrencyCodeId(currencyCode)

            return BasicPriceData(
                id = id,
                rate = rate,
                marketCap = marketCap,
                volume24Hr = volume24Hr,
                currencyCode = currencyCode
            )
        }
    }
}