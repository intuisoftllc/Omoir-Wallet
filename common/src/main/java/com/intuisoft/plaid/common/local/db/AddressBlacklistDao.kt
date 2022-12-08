package com.intuisoft.plaid.common.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AddressBlacklistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: AddressBlacklist)

    @Query("""
        SELECT * from address_blacklist
         ORDER BY address ASC 
    """)
    fun getBlacklistedAddresses() : List<AddressBlacklist>

    @Query("""
        DELETE FROM address_blacklist
        WHERE address = :address
    """)
    fun removeFromBlacklist(address: String)

    @Query("DELETE FROM address_blacklist")
    fun deleteTable()
}