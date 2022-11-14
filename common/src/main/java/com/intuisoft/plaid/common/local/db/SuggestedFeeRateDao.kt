package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SuggestedFeeRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: SuggestedFeeRate)

    @Query("""
        SELECT * from suggested_fee_rate
         WHERE testnet_rate = :testnetRate
         ORDER BY id ASC LIMIT 1
    """)
    fun getFeeRate(testnetRate: Boolean) : SuggestedFeeRate?

    @Query("DELETE FROM suggested_fee_rate")
    fun deleteTable()
}