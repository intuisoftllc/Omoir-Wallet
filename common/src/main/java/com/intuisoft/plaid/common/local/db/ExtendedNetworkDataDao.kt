package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExtendedNetworkDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(extendedData: ExtendedNetworkData)

    @Query("""
        SELECT * from extended_network_data
         WHERE testnet_data = :testnetData
         ORDER BY id ASC LIMIT 1
    """)
    fun getExtendedNetworkData(testnetData: Boolean) : ExtendedNetworkData?

    @Query("DELETE FROM extended_network_data")
    fun deleteTable()
}