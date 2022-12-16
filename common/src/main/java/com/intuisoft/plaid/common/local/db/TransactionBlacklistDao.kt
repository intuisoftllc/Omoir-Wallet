package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransactionBlacklistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: TransactionBlacklist)

    @Query("""
        SELECT * from transaction_blacklist
         WHERE wallet_id = :walletId
         ORDER BY id ASC 
    """)
    fun getBlacklistedTransaction(walletId: String) : List<TransactionBlacklist>

    @Query("""
        SELECT * from transaction_blacklist
         ORDER BY id ASC 
    """)
    fun getBlacklistedTransaction() : List<TransactionBlacklist>

    @Query("""
        DELETE FROM transaction_blacklist
        WHERE id = :txId
    """)
    fun removeFromBlacklist(txId: String)

    @Query("DELETE FROM transaction_blacklist")
    fun deleteTable()
}