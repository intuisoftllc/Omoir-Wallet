package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: BatchData)

    @Query("""
        SELECT * from batch_data
        WHERE transfer_id = :transferId
         ORDER BY batch_number ASC 
    """)
    fun getBatchesForTransfer(transferId: String) : List<BatchData>

    @Query("DELETE FROM batch_data")
    fun deleteTable()
}