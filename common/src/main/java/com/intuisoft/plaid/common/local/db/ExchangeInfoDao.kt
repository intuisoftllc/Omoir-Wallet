package com.intuisoft.plaid.common.local.db

import androidx.room.*

@Dao
interface ExchangeInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: ExchangeInfoData)

    @Query("""
        SELECT * from exchange_info
         WHERE wallet_uuid = :walletId
         ORDER BY id ASC
    """)
    fun getAllExchanges(walletId: String) : List<ExchangeInfoData>

    @Query("""
        SELECT * from exchange_info
         WHERE id = :exchangeId
         ORDER BY id ASC LIMIT 1
    """)
    fun getExchangeById(exchangeId: String) : ExchangeInfoData?

    @Query("DELETE FROM exchange_info")
    fun deleteTable()
}