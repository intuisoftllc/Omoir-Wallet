package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BitcoinStatsDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: BitcoinStatsData)

    @Query("""
        SELECT * from bitcoin_stats_data
         ORDER BY id ASC LIMIT 1
    """)
    fun getStatsData() : BitcoinStatsData?

    @Query("DELETE FROM bitcoin_stats_data")
    fun deleteTable()
}