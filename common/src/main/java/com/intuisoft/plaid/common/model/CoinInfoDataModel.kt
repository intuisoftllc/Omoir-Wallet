package com.intuisoft.plaid.common.model

import com.intuisoft.plaid.common.util.Constants

data class CoinInfoDataModel(
    val id: String,
    val marketData: CoinMarketData
)

data class CoinMarketData(
    val currentPrice: PriceData,
    val marketCap: PriceData,
    val totalVolume: PriceData,
    val maxSupply: Double,
    val circulatingSupply: Double,
)

data class PriceData(
    val usd: Double,
    val cad: Double,
    val eur: Double,
    val ars: Double,
    val aud: Double,
    val bdt: Double,
    val bhd: Double,
    val chf: Double,
    val cny: Double,
    val czk: Double,
    val gbp: Double,
    val krw: Double,
    val rub: Double,
    val php: Double,
    val pkr: Double,
    val clp: Double,
    val aed: Double
) {

    fun getPrice(localCurrency: String): Double {
        return when(localCurrency) {
            Constants.LocalCurrency.USD -> usd
            Constants.LocalCurrency.CANADA -> cad
            Constants.LocalCurrency.EURO -> eur
            Constants.LocalCurrency.ARS -> ars
            Constants.LocalCurrency.AUD -> aud
            Constants.LocalCurrency.BDT -> bdt
            Constants.LocalCurrency.BHD -> bhd
            Constants.LocalCurrency.CHF -> chf
            Constants.LocalCurrency.CNY -> cny
            Constants.LocalCurrency.CZK -> czk
            Constants.LocalCurrency.GBP -> gbp
            Constants.LocalCurrency.KRW -> krw
            Constants.LocalCurrency.RUB -> rub
            Constants.LocalCurrency.PHP -> php
            Constants.LocalCurrency.PKR -> pkr
            Constants.LocalCurrency.CLP -> clp
            Constants.LocalCurrency.AED -> aed
            else -> usd
        }
    }
}