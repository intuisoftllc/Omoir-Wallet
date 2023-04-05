package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.model.BitcoinStatsDataModel
import com.intuisoft.plaid.common.model.BlockStatsDataModel

@Entity(tableName = "bitcoin_stats_data")
data class BitcoinStatsData(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "avg_conf_time") var avgConfTime: Double
) {
    fun from() =
        BitcoinStatsDataModel(
            avgConfTime = avgConfTime
        )

    companion object {

        fun consume(data: BitcoinStatsDataModel): BitcoinStatsData {

            return BitcoinStatsData(
                avgConfTime = data.avgConfTime
            )
        }
    }
}