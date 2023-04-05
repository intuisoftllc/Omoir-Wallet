package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.intuisoft.plaid.common.delegates.market.MarketDataDelegate
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat

@TypeConverters(value = [LongListItemConverter::class, FloatListItemConverter::class])
@Entity(tableName = "ticker_price_chart_data")
data class TickerPriceChartData(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "times") var times: List<Long>,
    @ColumnInfo(name = "values") var values: List<Float>,
    @ColumnInfo(name = "currency_code") var currencyCode: String,
    @ColumnInfo(name = "coin") var coin: String,
    @ColumnInfo(name = "interval_type") var intervalType: Int
) {
    fun from(): List<ChartDataModel> {
        val list = mutableListOf<ChartDataModel>()

        times.forEachIndexed { index, time ->
            list.add(ChartDataModel(time, values[index]))
        }

        return list
    }

    companion object {

        fun consume(intervalType: ChartIntervalType, data: List<ChartDataModel>, currencyCode: String, coin: String): TickerPriceChartData {
            var id = intervalType.name + currencyCode + coin

            return TickerPriceChartData(
                id = id,
                times = data.map { it.time },
                values = data.map { it.value },
                currencyCode = currencyCode,
                coin = coin,
                intervalType = intervalType.ordinal
            )
        }
    }
}