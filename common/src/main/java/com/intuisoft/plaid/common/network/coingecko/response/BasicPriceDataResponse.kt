package com.intuisoft.plaid.common.network.blockchair.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BasicPriceDataResponse(
    val bitcoin: BitcoinPriceDataResponse
): Parcelable

@Parcelize
data class BitcoinPriceDataResponse(
    val usd: Double,
    val usd_market_cap: Double,
    val usd_24h_vol: Double,
    val cad: Double,
    val cad_market_cap: Double,
    val cad_24h_vol: Double,
    val eur: Double,
    val eur_market_cap: Double,
    val eur_24h_vol: Double,
    val ars: Double,
    val ars_market_cap: Double,
    val ars_24h_vol: Double,
    val aud: Double,
    val aud_market_cap: Double,
    val aud_24h_vol: Double,
    val bdt: Double,
    val bdt_market_cap: Double,
    val bdt_24h_vol: Double,
    val bhd: Double,
    val bhd_market_cap: Double,
    val bhd_24h_vol: Double,
    val chf: Double,
    val chf_market_cap: Double,
    val chf_24h_vol: Double,
    val cny: Double,
    val cny_market_cap: Double,
    val cny_24h_vol: Double,
    val czk: Double,
    val czk_market_cap: Double,
    val czk_24h_vol: Double,
    val gbp: Double,
    val gbp_market_cap: Double,
    val gbp_24h_vol: Double,
    val krw: Double,
    val krw_market_cap: Double,
    val krw_24h_vol: Double,
    val rub: Double,
    val rub_market_cap: Double,
    val rub_24h_vol: Double,
    val php: Double,
    val php_market_cap: Double,
    val php_24h_vol: Double,
    val pkr: Double,
    val pkr_market_cap: Double,
    val pkr_24h_vol: Double,
    val clp: Double,
    val clp_market_cap: Double,
    val clp_24h_vol: Double,
    val aed: Double,
    val aed_market_cap: Double,
    val aed_24h_vol: Double,
): Parcelable