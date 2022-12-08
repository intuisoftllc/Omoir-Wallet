package com.intuisoft.plaid.common.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.util.Constants

@TypeConverters(
    value = [
        LongListItemConverter::class,
        FloatListItemConverter::class,
        InstantConverter::class,
        AssetTransferStatusConverter::class,
        UtxoTransferConverter::class
    ]
)
@Database(
    entities = arrayOf(
        SuggestedFeeRate::class,
        BasicPriceData::class,
        BasicNetworkData::class,
        ExtendedNetworkData::class,
        TickerPriceChartData::class,
        SupportedCurrency::class,
        TransactionMemo::class,
        ExchangeInfoData::class,
        AssetTransfer::class,
        BatchData::class,
        AddressBlacklist::class,
        TransactionBlacklist::class
    ),
    version = Constants.Database.DB_VERSION
)
abstract class PlaidDatabase : RoomDatabase() {
    private var listener: DatabaseListener? = null

    fun setDatabaseListener(databaseListener: DatabaseListener) {
        listener = databaseListener
    }

    fun onUpdate(dao: Any?) {
        listener?.onDatabaseUpdated(dao)
    }

    companion object {

        private const val DATABASE_NAME = Constants.Database.DB_NAME

        private var instance: PlaidDatabase? = null

        private fun create(context: Context): PlaidDatabase =
            Room.databaseBuilder(context, PlaidDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()

        fun getInstance(context: Context): PlaidDatabase =
            (instance ?: create(context)).also { instance = it }
    }

    abstract fun suggestedFeeRateDao(): SuggestedFeeRateDao

    abstract fun localCurrencyRateDao(): BasicPriceDataDao

    abstract fun baseMarketDataDao(): BaseMarketDataDao

    abstract fun extendedMarketDataDao(): ExtendedNetworkDataDao

    abstract fun tickerPriceChartDataDao(): TickerPriceChartDataDao

    abstract fun supportedCurrencyDao(): SupportedCurrencyDao

    abstract fun transactionMemoDao(): TransactionMemoDao

    abstract fun exchangeInfoDao(): ExchangeInfoDao

    abstract fun assetTransfersDao(): AssetTransferDao

    abstract fun batchDataDao(): BatchDao

    abstract fun addressBlacklistDao(): AddressBlacklistDao

    abstract fun transactionBlacklistDao(): TransactionBlacklistDao
}
