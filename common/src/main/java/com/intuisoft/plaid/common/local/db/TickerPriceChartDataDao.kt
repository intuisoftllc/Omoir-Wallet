package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TickerPriceChartDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: TickerPriceChartData)

    @Query("""
        SELECT * from ticker_price_chart_data
         WHERE interval_type = :intervalType AND currency_code = :currencyCode
         ORDER BY id ASC LIMIT 1
    """)
    fun getChartDataFor(intervalType: Int, currencyCode: String) : TickerPriceChartData?

    @Query("""
        SELECT * from ticker_price_chart_data
         ORDER BY id ASC
    """)
    fun getAllChartData() : List<TickerPriceChartData>

    @Query("DELETE FROM ticker_price_chart_data")
    fun deleteTable()
}