package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.model.BasicNetworkDataModel

@Entity(tableName = "base_market_data")
data class BasicNetworkData(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "circulating_supply") var circulatingSupply: Long,
    @ColumnInfo(name = "mem_pool_tx_count") var memPoolTxCount: Int
) {
    fun from() =
        BasicNetworkDataModel(
            circulatingSupply = circulatingSupply,
            memPoolTxCount = memPoolTxCount
        )

    companion object {

        fun consume(circulatingSupply: Long, memPoolTx: Int) =
            BasicNetworkData(
                circulatingSupply = circulatingSupply,
                memPoolTxCount = memPoolTx
            )
    }
}