package com.intuisoft.emojiigame.framework.db

import androidx.room.*

@TypeConverters(value = [WalletTypeConverter::class])
@Dao
interface LocalWalletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: LocalWallet)

    @Query("""
        SELECT * from local_wallet
         ORDER BY id ASC
    """)
    fun getAllWallets() : List<LocalWallet>?

    @Query("""
        SELECT * from local_wallet
         WHERE name = :name
         ORDER BY id ASC
         LIMIT 1
    """)
    fun getWalletByName(name: String) : LocalWallet?

    @Query("DELETE FROM local_wallet")
    fun deleteTable()
}