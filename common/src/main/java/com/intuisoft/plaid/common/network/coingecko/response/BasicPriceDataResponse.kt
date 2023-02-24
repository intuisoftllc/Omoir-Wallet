package com.intuisoft.plaid.common.network.coingecko.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BasicPriceDataResponse(

    @SerializedName("bitcoin")
    val bitcoin: BitcoinPriceDataResponse
): Parcelable

@Parcelize
data class BitcoinPriceDataResponse(

    @SerializedName("usd")
    val usd: Double,

    @SerializedName("usd_market_cap")
    val usd_market_cap: Double,

    @SerializedName("usd_24h_vol")
    val usd_24h_vol: Double,

    @SerializedName("cad")
    val cad: Double,

    @SerializedName("cad_market_cap")
    val cad_market_cap: Double,

    @SerializedName("cad_24h_vol")
    val cad_24h_vol: Double,

    @SerializedName("eur")
    val eur: Double,

    @SerializedName("eur_market_cap")
    val eur_market_cap: Double,

    @SerializedName("eur_24h_vol")
    val eur_24h_vol: Double,

    @SerializedName("ars")
    val ars: Double,

    @SerializedName("ars_market_cap")
    val ars_market_cap: Double,

    @SerializedName("ars_24h_vol")
    val ars_24h_vol: Double,

    @SerializedName("aud")
    val aud: Double,

    @SerializedName("aud_market_cap")
    val aud_market_cap: Double,

    @SerializedName("aud_24h_vol")
    val aud_24h_vol: Double,

    @SerializedName("bdt")
    val bdt: Double,

    @SerializedName("bdt_market_cap")
    val bdt_market_cap: Double,

    @SerializedName("bdt_24h_vol")
    val bdt_24h_vol: Double,

    @SerializedName("bhd")
    val bhd: Double,

    @SerializedName("bhd_market_cap")
    val bhd_market_cap: Double,

    @SerializedName("bhd_24h_vol")
    val bhd_24h_vol: Double,

    @SerializedName("chf")
    val chf: Double,

    @SerializedName("chf_market_cap")
    val chf_market_cap: Double,

    @SerializedName("chf_24h_vol")
    val chf_24h_vol: Double,

    @SerializedName("cny")
    val cny: Double,

    @SerializedName("cny_market_cap")
    val cny_market_cap: Double,

    @SerializedName("cny_24h_vol")
    val cny_24h_vol: Double,

    @SerializedName("czk")
    val czk: Double,

    @SerializedName("czk_market_cap")
    val czk_market_cap: Double,

    @SerializedName("czk_24h_vol")
    val czk_24h_vol: Double,

    @SerializedName("gbp")
    val gbp: Double,

    @SerializedName("gbp_market_cap")
    val gbp_market_cap: Double,

    @SerializedName("gbp_24h_vol")
    val gbp_24h_vol: Double,

    @SerializedName("krw")
    val krw: Double,

    @SerializedName("krw_market_cap")
    val krw_market_cap: Double,

    @SerializedName("krw_24h_vol")
    val krw_24h_vol: Double,

    @SerializedName("rub")
    val rub: Double,

    @SerializedName("rub_market_cap")
    val rub_market_cap: Double,

    @SerializedName("rub_24h_vol")
    val rub_24h_vol: Double,

    @SerializedName("php")
    val php: Double,

    @SerializedName("php_market_cap")
    val php_market_cap: Double,

    @SerializedName("php_24h_vol")
    val php_24h_vol: Double,

    @SerializedName("pkr")
    val pkr: Double,

    @SerializedName("pkr_market_cap")
    val pkr_market_cap: Double,

    @SerializedName("pkr_24h_vol")
    val pkr_24h_vol: Double,

    @SerializedName("clp")
    val clp: Double,

    @SerializedName("clp_market_cap")
    val clp_market_cap: Double,

    @SerializedName("clp_24h_vol")
    val clp_24h_vol: Double,

    @SerializedName("aed")
    val aed: Double,

    @SerializedName("aed_market_cap")
    val aed_market_cap: Double,

    @SerializedName("aed_24h_vol")
    val aed_24h_vol: Double,
): Parcelable