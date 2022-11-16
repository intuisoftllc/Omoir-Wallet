package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BaseMarketDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: BasicNetworkData)

    @Query("""
        SELECT * from base_market_data
         ORDER BY id ASC LIMIT 1
    """)
    fun getNetworkData() : BasicNetworkData?

    @Query("DELETE FROM base_market_data")
    fun deleteTable()
}