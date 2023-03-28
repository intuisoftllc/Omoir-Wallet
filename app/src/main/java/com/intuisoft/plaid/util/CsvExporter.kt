package com.intuisoft.plaid.util

import android.app.Application
import android.os.Environment
import com.intuisoft.plaid.R
import com.intuisoft.plaid.common.delegates.DelegateManager
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.SimpleTimeFormat
import com.intuisoft.plaid.common.util.extensions.writeToFile
import com.intuisoft.plaid.model.ExportDataType
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionType
import java.io.File
import java.util.*

class CsvExporter(
    private val application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val delegateManager: DelegateManager,
    private val walletName: String,
    private val transactions: List<TransactionInfo>,
    private val dataType: ExportDataType,
) {

    companion object {
        private val FILE_NAME = "transactions.csv"
    }

    suspend fun export() : String? {
        val data = generate()
        val dir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = SimpleTimeFormat.getFileFormattedDate(System.currentTimeMillis(), Locale.US) +
                "-" + walletName.replace("\\s".toRegex(), "_") + "-" + FILE_NAME
        val csv = File(dir, fileName)

        if(csv.writeToFile(data, application)) {
            return csv.absolutePath
        } else {
            return null
        }
    }

    private suspend fun generate() : String {
        val filteredTransactions = transactions.filter {
            when(dataType) {
                ExportDataType.RAW -> {
                    true
                }

                ExportDataType.INCOMING -> {
                    it.type == TransactionType.Incoming
                }

                ExportDataType.OUTGOING -> {
                    it.type == TransactionType.Outgoing
                }
            }
        }
        
        
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
        
        val feeConverter =
            RateConverter(delegateManager.current().marketDelegate.getLocalBasicTickerData().price)
        val amountConverter = feeConverter.clone()
        val totalValueConverter = feeConverter.clone()
        filteredTransactions.forEach {
            feeConverter.setLocalRate(RateConverter.RateType.SATOSHI_RATE, it.fee?.toDouble() ?: 0.0)
            amountConverter.setLocalRate(RateConverter.RateType.SATOSHI_RATE, it.amount.toDouble())
            totalValueConverter.setLocalRate(RateConverter.RateType.SATOSHI_RATE, (it.amount + (it.fee?: 0)).toDouble() )

            val amount: String
            val fee: String
            val total: String

            when(localStoreRepository.getBitcoinDisplayUnit()) {
                BitcoinDisplayUnit.BTC -> {
                    amount = amountConverter.from(RateConverter.RateType.BTC_RATE, localStoreRepository.getLocalCurrency()).first
                    fee = feeConverter.from(RateConverter.RateType.BTC_RATE, localStoreRepository.getLocalCurrency()).first
                    total = totalValueConverter.from(RateConverter.RateType.BTC_RATE, localStoreRepository.getLocalCurrency()).first
                }

                BitcoinDisplayUnit.SATS -> {
                    amount = amountConverter.from(RateConverter.RateType.SATOSHI_RATE, localStoreRepository.getLocalCurrency()).first
                    fee = feeConverter.from(RateConverter.RateType.SATOSHI_RATE, localStoreRepository.getLocalCurrency()).first
                    total = totalValueConverter.from(RateConverter.RateType.SATOSHI_RATE, localStoreRepository.getLocalCurrency()).first
                }

                BitcoinDisplayUnit.FIAT -> {
                    amount = amountConverter.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).first
                    fee = feeConverter.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).first
                    total = totalValueConverter.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).first
                }
            }

            rows.add(
                listOf(
                    SimpleTimeFormat.getCsvDateByLocale(it.timestamp * Constants.Time.MILLS_PER_SEC, Locale.US)
                        ?: application.getString(R.string.not_applicable),
                    when(it.type) {
                        TransactionType.Incoming -> application.getString(R.string.export_csv_transaction_type_1)
                        TransactionType.Outgoing -> application.getString(R.string.export_csv_transaction_type_2)
                        TransactionType.SentToSelf -> application.getString(R.string.export_csv_transaction_type_3)
                    },
                    amount,
                    fee,
                    total,
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