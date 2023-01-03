package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SupportedCurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(currencies: List<SupportedCurrency>)

    @Query("""
        SELECT * from supported_currency
         ORDER BY name ASC
    """)
    fun getAllSupportedCurrencies() : List<SupportedCurrency>

    @Query("DELETE FROM supported_currency")
    fun deleteTable()
}