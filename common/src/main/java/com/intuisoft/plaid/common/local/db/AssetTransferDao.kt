package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AssetTransferDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: AssetTransfer)

    @Query("""
        SELECT * from asset_transfers
        WHERE wallet_id = :walletId
         ORDER BY created_at DESC 
    """)
    fun getAllAssetTransfers(walletId: String) : List<AssetTransfer>

    @Query("DELETE FROM asset_transfers")
    fun deleteTable()
}