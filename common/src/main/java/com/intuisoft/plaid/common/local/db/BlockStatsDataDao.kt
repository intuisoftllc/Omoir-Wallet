package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BlockStatsDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(extendedData: BlockStatsData)

    @Query("""
        SELECT * from block_stats_data
         WHERE testnet_data = :testnetData AND coin = :coin
         ORDER BY id ASC LIMIT 1
    """)
    fun getBlockStatsData(testnetData: Boolean, coin: String) : BlockStatsData?

    @Query("DELETE FROM block_stats_data")
    fun deleteTable()
}