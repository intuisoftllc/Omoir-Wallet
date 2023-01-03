package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel

@Entity(tableName = "supported_currency")
data class SupportedCurrency(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "ticker") var ticker: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "image") var image: String,
    @ColumnInfo(name = "needs_memo") var needsMemo: Boolean,
    @ColumnInfo(name = "network") var network: String
) {
    fun from() =
        SupportedCurrencyModel(
            ticker = ticker,
            id = id,
            name = name,
            image = image,
            network = network,
            needsMemo = needsMemo
        )

    companion object {

        fun consume(ticker: String, name: String, image: String, network: String, needsMemo: Boolean) =
            SupportedCurrency(
                id = "$ticker:$name",
                ticker = ticker,
                name = name,
                image = image,
                network = network,
                needsMemo = needsMemo
            )
    }
}