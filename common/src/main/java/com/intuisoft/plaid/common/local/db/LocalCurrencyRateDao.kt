package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocalCurrencyRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rates: List<LocalCurrencyRate>)

    @Query("""
        SELECT * from local_currency_rate
         WHERE currency_code = :currencyCode
         ORDER BY id ASC LIMIT 1
    """)
    fun getRateFor(currencyCode: String) : LocalCurrencyRate?

    @Query("""
        SELECT * from local_currency_rate
         ORDER BY id ASC
    """)
    fun getAllRates() : List<LocalCurrencyRate>

    @Query("DELETE FROM local_currency_rate")
    fun deleteTable()
}