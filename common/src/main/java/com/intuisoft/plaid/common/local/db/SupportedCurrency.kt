package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.network.nownodes.response.SupportedCurrencyModel

@Entity(tableName = "supported_currency")
data class SupportedCurrency(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "ticker") var ticker: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "image") var image: String,
    @ColumnInfo(name = "fixed") var fixed: Boolean,
) {
    fun from() =
        SupportedCurrencyModel(
            ticker = ticker,
            name = name,
            image = image
        )

    companion object {

        fun consume(ticker: String, name: String, image: String, fixed: Boolean) =
            SupportedCurrency(
                id = ticker + if(fixed) 1 else 0,
                ticker = ticker,
                name = name,
                image = image,
                fixed = fixed
            )
    }
}