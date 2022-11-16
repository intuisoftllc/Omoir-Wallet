package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BasicPriceDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rates: List<BasicPriceData>)

    @Query("""
        SELECT * from basic_price_data
         WHERE currency_code = :currencyCode
         ORDER BY id ASC LIMIT 1
    """)
    fun getRateFor(currencyCode: String) : BasicPriceData?

    @Query("""
        SELECT * from basic_price_data
         ORDER BY id ASC
    """)
    fun getAllRates() : List<BasicPriceData>

    @Query("DELETE FROM basic_price_data")
    fun deleteTable()
}