package com.intuisoft.plaid.util

object SimpleCoinNumberFormat {

    fun format(number: Long) : String {
        if(number <= 0) return "0"

        val compactPatterns = arrayOf(
            "", "", "K", "K", "K", "Mil", "Mil", "Mil",
            "Bil", "Bil", "Bil", "Trill", "Tril", "Tril"
        )

        val divisor = arrayOf(
            1, 1, 1000, 10000, 100000, 1000000, 10000000,
            100000000, 1000000000, 10000000000, 100000000000,
            1000000000000, 10000000000000, 100000000000000
        )

        var notation = 10
        var divisorIndex = 0
        while(number > notation) {
            if(notation >= 1_000_000_000_000) {
                divisorIndex = divisor.size - 1
                break
            }

            divisorIndex++
            notation *= 10
        }

        return "${number.toDouble() / divisorIndex.toDouble()} ${compactPatterns[divisorIndex]}"
    }
}