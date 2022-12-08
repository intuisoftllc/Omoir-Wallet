package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.intuisoft.plaid.common.model.BasicNetworkDataModel
import com.intuisoft.plaid.common.model.BlacklistedAddressModel
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import java.time.Instant

@Entity(tableName = "address_blacklist")
data class AddressBlacklist(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "address") var address: String
) {
    fun from() =
        BlacklistedAddressModel(
            address = address
        )

    companion object {

        fun consume(address: String) =
            AddressBlacklist(
                address = address
            )
    }
}