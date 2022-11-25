package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.intuisoft.plaid.common.model.BasicNetworkDataModel
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import java.time.Instant

@TypeConverters(value = [InstantConverter::class])
@Entity(tableName = "exchange_info")
data class ExchangeInfoData(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "wallet_uuid") val uuid: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "timestamp") val timestamp: Instant,
    @ColumnInfo(name = "last_updated") val lastUpdated: Instant,
    @ColumnInfo(name = "currency_from") val from: String,
    @ColumnInfo(name = "currency_to") val to: String,
    @ColumnInfo(name = "send_amount") val sendAmount: Double,
    @ColumnInfo(name = "receive_amount") val receiveAmount: Double,
    @ColumnInfo(name = "payment_address") val paymentAddress: String,
    @ColumnInfo(name = "payment_address_memo") val paymentAddressMemo: String?,
    @ColumnInfo(name = "receive_address_memo") val receiveAddressMemo: String?,
    @ColumnInfo(name = "refund_address") val refundAddress: String,
    @ColumnInfo(name = "refund_address_memo") val refundAddressMemo: String,
    @ColumnInfo(name = "payment_tx_id") val paymentTxId: String,
    @ColumnInfo(name = "receive_tx_id") val receiveTxId: String,
    @ColumnInfo(name = "status") val status: String
) {
    fun from() =
        ExchangeInfoDataModel(
            id = id,
            type = type,
            timestamp = timestamp,
            lastUpdated = lastUpdated,
            from = from,
            to = to,
            sendAmount = sendAmount,
            receiveAmount = receiveAmount,
            paymentAddress = paymentAddress,
            paymentAddressMemo = paymentAddressMemo,
            receiveAddressMemo = receiveAddressMemo,
            refundAddress = refundAddress,
            refundAddressMemo = refundAddressMemo,
            paymentTxId = paymentTxId,
            receiveTxId = receiveTxId,
            status = status
        )

    companion object {

        fun consume(data: ExchangeInfoDataModel, walletId: String) =
            ExchangeInfoData(
                id = data.id,
                uuid = walletId,
                type = data.type,
                timestamp = data.timestamp,
                lastUpdated = data.lastUpdated,
                from = data.from,
                to = data.to,
                sendAmount = data.sendAmount,
                receiveAmount = data.receiveAmount,
                paymentAddress = data.paymentAddress,
                paymentAddressMemo = data.paymentAddressMemo,
                receiveAddressMemo = data.receiveAddressMemo,
                refundAddress = data.refundAddress,
                refundAddressMemo = data.refundAddressMemo,
                paymentTxId = data.paymentTxId,
                receiveTxId = data.receiveTxId,
                status = data.status
            )
    }
}