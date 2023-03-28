package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.model.CoinInfoDataModel
import com.intuisoft.plaid.common.model.CoinMarketData
import com.intuisoft.plaid.common.model.PriceData
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat

@Entity(tableName = "basic_coin_info")
data class BasicCoinInfo(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "price_data") var priceData: String,
    @ColumnInfo(name = "market_cap_data") var marketCapData: String,
    @ColumnInfo(name = "volume_data") var volumeData: String,
    @ColumnInfo(name = "max_supply") var maxSupply: Double,
    @ColumnInfo(name = "circulating_supply") var circulatingSupply: Double
) {
    fun from(): CoinInfoDataModel {
        fun parsePriceData(data: String): PriceData {
            val pairs = data.split(",").map {
                val data = it.split(":")
                Pair(data[0], data[1].toDouble())
            }

            return PriceData(
                usd = pairs.find { it.first == "usd" }?.second ?: 0.0,
                cad = pairs.find { it.first == "cad" }?.second ?: 0.0,
                eur = pairs.find { it.first == "eur" }?.second ?: 0.0,
                ars = pairs.find { it.first == "ars" }?.second ?: 0.0,
                aud = pairs.find { it.first == "aud" }?.second ?: 0.0,
                bdt = pairs.find { it.first == "bdt" }?.second ?: 0.0,
                bhd = pairs.find { it.first == "bhd" }?.second ?: 0.0,
                chf = pairs.find { it.first == "chf" }?.second ?: 0.0,
                cny = pairs.find { it.first == "cny" }?.second ?: 0.0,
                czk = pairs.find { it.first == "czk" }?.second ?: 0.0,
                gbp = pairs.find { it.first == "gbp" }?.second ?: 0.0,
                krw = pairs.find { it.first == "krw" }?.second ?: 0.0,
                rub = pairs.find { it.first == "rub" }?.second ?: 0.0,
                php = pairs.find { it.first == "php" }?.second ?: 0.0,
                pkr = pairs.find { it.first == "pkr" }?.second ?: 0.0,
                clp = pairs.find { it.first == "clp" }?.second ?: 0.0,
                aed = pairs.find { it.first == "aed" }?.second ?: 0.0
            )
        }

        return CoinInfoDataModel(
            id = id,
            marketData = CoinMarketData(
                currentPrice = parsePriceData(priceData),
                marketCap = parsePriceData(marketCapData),
                totalVolume = parsePriceData(volumeData),
                maxSupply = maxSupply,
                circulatingSupply = circulatingSupply
            )
        )
    }

    companion object {

        fun consume(info: CoinInfoDataModel): BasicCoinInfo {
            var id = info.id

            val priceDataToStr = { data: PriceData -> (String)
                "usd:${data.usd},cad:${data.cad},eur:${data.eur},ars:${data.ars},aud:${data.aud},bdt:${data.bdt}" +
                ",bhd:${data.bhd},chf:${data.chf},cny:${data.cny},czk:${data.czk},gbp:${data.gbp},krw:${data.krw}" +
                ",rub:${data.rub},php:${data.php},pkr:${data.pkr},clp:${data.clp},aed:${data.aed}"
            }

            return BasicCoinInfo(
                id = id,
                priceData = priceDataToStr(info.marketData.currentPrice),
                marketCapData = priceDataToStr(info.marketData.marketCap),
                volumeData = priceDataToStr(info.marketData.totalVolume),
                maxSupply = info.marketData.maxSupply,
                circulatingSupply = info.marketData.circulatingSupply
            )
        }
    }
}