package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransactionMemoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: TransactionMemo)

    @Query("""
        SELECT * from transaction_memo
         WHERE transaction_id = :transactionId
         ORDER BY transaction_id ASC LIMIT 1
    """)
    fun getMemoFor(transactionId: String) : TransactionMemo?

    @Query("""
        SELECT * from transaction_memo
         ORDER BY transaction_id ASC
    """)
    fun getAllMemos() : List<TransactionMemo>

    @Query("DELETE FROM transaction_memo")
    fun deleteTable()
}