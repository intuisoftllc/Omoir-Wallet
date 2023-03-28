package com.intuisoft.plaid.common.util

import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import java.text.DecimalFormat
import kotlin.math.absoluteValue

object SimpleCoinNumberFormat {

    // todo: make more generic to support different currencies
    fun format(localStoreRepository: LocalStoreRepository, sats: Long, shortenSats: Boolean = true) : String {
        val converter = RateConverter(localStoreRepository.getBasicCoinInfo("BTC")?.marketData?.currentPrice?.getPrice(localStoreRepository.getLocalCurrency()) ?: 0.0)
        converter.setLocalRate(RateConverter.RateType.SATOSHI_RATE, sats.toDouble())

        when(localStoreRepository.getBitcoinDisplayUnit()) {
            BitcoinDisplayUnit.BTC -> {
                return converter.from(RateConverter.RateType.BTC_RATE, localStoreRepository.getLocalCurrency()).second
            }

            BitcoinDisplayUnit.SATS -> {
                return converter.from(RateConverter.RateType.SATOSHI_RATE,
                    localStoreRepository.getLocalCurrency(), shortenSats).second
            }

            BitcoinDisplayUnit.FIAT -> {
                return converter.from(RateConverter.RateType.FIAT_RATE,
                    localStoreRepository.getLocalCurrency(),true).second
            }
        }
    }

    fun formatSatsShort(number: Long) : String {
        val _number = number.absoluteValue
        val isNegative = number < 0
        val df = DecimalFormat("###,###.##")

        when {
            (0..999).contains(_number) -> {
                return (if(isNegative) "-" else "") + _number
            }
            (1000..9_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1000) + " K"
            }
            (10_000..99_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000) + " K"
            }
            (100_000..999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000) + " K"
            }
            (1_000_000..9_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000) + " Mil"
            }
            (10_000_000..99_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000) + " Mil"
            }
            (100_000_000..999_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000) + " Mil"
            }
            (1_000_000_000..9_999_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000_000) + " Bil"
            }
            (10_000_000_000..99_999_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000_000) + " Bil"
            }
            (100_000_000_000..999_999_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000_000) + " Bil"
            }
            (1_000_000_000_000..9_999_999_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000_000_000) + " Tril"
            }
            (10_000_000_000_000..99_999_999_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000_000_000) + " Tril"
            }
            (100_000_000_000_000..999_999_999_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000_000_000) + " Tril"
            }
            (1_000_000_000_000_000..9_999_999_999_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000_000_000_000) + " Quad"
            }
            (10_000_000_000_000_000..99_999_999_999_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000_000_000_000) + " Quad"
            }
            (100_000_000_000_000_000..999_999_999_999_999_999).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000_000_000_000) + " Quad"
            }
            (1_000_000_000_000_000_000..Long.MAX_VALUE).contains(_number) -> {
                return (if(isNegative) "-" else "") + df.format(_number.toDouble() / 1_000_000_000_000_000_000) + " Quint+"
            }
            else -> {
                return (if(isNegative) "-" else "") + _number
            }
        }
    }

    fun format(value: Double): String? {
        val df = DecimalFormat("###,###,###,###,###,###.########")
        return df.format(value)
    }

    fun formatCrypto(value: Double): String? {
        val df = DecimalFormat("###,###,###,###,###,###,###,###.################")
        return df.format(value)
    }

    fun formatCurrency(value: Double): String? {
        val df = DecimalFormat("###,###,###,###,###,###.##")
        return df.format(value)
    }

    fun format(value: Long): String? {
        val df = DecimalFormat("###,###,###,###,###,###")
        return df.format(value)
    }
}