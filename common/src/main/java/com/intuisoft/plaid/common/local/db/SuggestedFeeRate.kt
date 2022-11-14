package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.model.NetworkFeeRate

@Entity(tableName = "suggested_fee_rate")
data class SuggestedFeeRate(
    @ColumnInfo(name = "testnet_rate") var testNetRate: Boolean,
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: Int = if(testNetRate) 1 else 0,
    @ColumnInfo(name = "low_fee") var low: Int,
    @ColumnInfo(name = "med_fee") var med: Int,
    @ColumnInfo(name = "high_fee") var high: Int
) {
    fun from() =
        NetworkFeeRate(
            lowFee = low,
            medFee = med,
            highFee = high
        )

    companion object {

        fun consume(feeRate: NetworkFeeRate, testNetWallet: Boolean) =
            SuggestedFeeRate(
                testNetRate = testNetWallet,
                low = feeRate.lowFee,
                med = feeRate.medFee,
                high = feeRate.highFee
            )
    }
}