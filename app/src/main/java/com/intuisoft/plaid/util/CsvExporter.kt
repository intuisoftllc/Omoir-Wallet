package com.intuisoft.plaid.util

import android.app.Application
import android.os.Environment
import com.intuisoft.plaid.R
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.extensions.writeToFile
import com.intuisoft.plaid.common.util.extensions.writeToPrivateFile
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionType
import java.io.File
import java.util.*

class CsvExporter(
    private val application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletName: String,
    private val transactions: List<TransactionInfo>
) {

    companion object {
        private val FILE_NAME = "transactions.csv"
    }

    suspend fun export() : String {
        val data = generate()
        val dir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = SimpleTimeFormat.getFileFormattedDate(System.currentTimeMillis(), Locale.US) +
                "-" + walletName.replace("\\s".toRegex(), "_") + "-" + FILE_NAME
        val csv = File(dir, fileName)
        csv.writeToFile(data, application)
        return fileName
    }

    private suspend fun generate() : String {
        val rows: MutableList<List<String>> = mutableListOf()
        rows.add(
            listOf(
                application.getString(R.string.export_csv_column_1),
                application.getString(R.string.export_csv_column_2),
                application.getString(R.string.export_csv_column_3),
                application.getString(R.string.export_csv_column_4),
                application.getString(R.string.export_csv_column_5),
                application.getString(R.string.export_csv_column_6)
            )
        )

        transactions.forEach {
            rows.add(
                listOf(
                    SimpleTimeFormat.getCsvDateByLocale(it.timestamp * Constants.Time.MILLS_PER_SEC, Locale.US)
                        ?: application.getString(R.string.not_applicable),
                    when(it.type) {
                        TransactionType.Incoming -> application.getString(R.string.export_csv_transaction_type_1)
                        TransactionType.Outgoing -> application.getString(R.string.export_csv_transaction_type_2)
                        TransactionType.SentToSelf -> application.getString(R.string.export_csv_transaction_type_3)
                    },
                    SimpleCoinNumberFormat.format(it.amount.toDouble() / Constants.Limit.SATS_PER_BTC)
                        ?: application.getString(R.string.not_applicable),
                    SimpleCoinNumberFormat.format(it.fee?.toDouble()?.div(Constants.Limit.SATS_PER_BTC) ?: 0.0)
                        ?: application.getString(R.string.not_applicable),
                    SimpleCoinNumberFormat.format((it.amount + (it.fee?: 0)).toDouble() / Constants.Limit.SATS_PER_BTC)
                        ?: application.getString(R.string.not_applicable),
                    localStoreRepository.getTransactionMemo(it.transactionHash)?.memo ?: ""
                )
            )
        }

        val builder = StringBuilder()
        rows.forEach {
            builder.append(it.joinToString(",") + "\n")
        }

        return builder.toString()
    }
}