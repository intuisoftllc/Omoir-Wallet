package com.intuisoft.plaid.common.network.coingecko.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CryptoInfoDataResponse(

    @SerializedName("id")
    val id: String,

    @SerializedName("market_data")
    val marketData: MarketDataResponse
): Parcelable

@Parcelize
data class MarketDataResponse(

    @SerializedName("current_price")
    val currentPrice: PriceDataResponse,

    @SerializedName("market_cap")
    val marketCap: PriceDataResponse,

    @SerializedName("total_volume")
    val totalVolume: PriceDataResponse,

    @SerializedName("max_supply")
    val maxSupply: Double,

    @SerializedName("circulating_supply")
    val circulatingSupply: Double,

): Parcelable


@Parcelize
data class PriceDataResponse(
    @SerializedName("usd")
    val usd: Double,

    @SerializedName("cad")
    val cad: Double,

    @SerializedName("eur")
    val eur: Double,

    @SerializedName("ars")
    val ars: Double,

    @SerializedName("aud")
    val aud: Double,

    @SerializedName("bdt")
    val bdt: Double,

    @SerializedName("bhd")
    val bhd: Double,

    @SerializedName("chf")
    val chf: Double,

    @SerializedName("cny")
    val cny: Double,

    @SerializedName("czk")
    val czk: Double,

    @SerializedName("gbp")
    val gbp: Double,

    @SerializedName("krw")
    val krw: Double,

    @SerializedName("rub")
    val rub: Double,

    @SerializedName("php")
    val php: Double,

    @SerializedName("pkr")
    val pkr: Double,

    @SerializedName("clp")
    val clp: Double,

    @SerializedName("aed")
    val aed: Double
): Parcelable