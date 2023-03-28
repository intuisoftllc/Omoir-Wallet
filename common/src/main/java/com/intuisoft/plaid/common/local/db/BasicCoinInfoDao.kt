package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BasicCoinInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: BasicCoinInfo)

    @Query("""
        SELECT * from basic_coin_info
         WHERE id = :id
         ORDER BY id ASC LIMIT 1
    """)
    fun getBasicCoinInfo(id: String) : BasicCoinInfo?

    @Query("DELETE FROM basic_coin_info")
    fun deleteTable()
}