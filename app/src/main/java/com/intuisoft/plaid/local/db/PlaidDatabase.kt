package com.intuisoft.emojiigame.framework.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.intuisoft.plaid.local.db.DatabaseListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.util.Constants

@TypeConverters(value = [WalletTypeConverter::class])
@Database(entities = arrayOf(LocalWallet::class), version = Constants.Database.DB_VERSION)
abstract class PlaidDatabase : RoomDatabase() {
    private var listener: DatabaseListener? = null

    fun setDatabaseListener(databaseListener: DatabaseListener) {
        listener = databaseListener
    }

    fun onUpdate() {
        listener?.onDatabaseUpdated()
    }

    companion object {

        private const val DATABASE_NAME = Constants.Database.DB_NAME

        private var instance: PlaidDatabase? = null

        private fun create(context: Context): PlaidDatabase =
            Room.databaseBuilder(context, PlaidDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()

        fun getInstance(context: Context): PlaidDatabase =
            (instance ?: create(context)).also { instance = it }

        fun toDb(localWallet: LocalWalletModel) =
            LocalWallet(
                name = localWallet.name,
                type = localWallet.type,
                testNetWallet = localWallet.testNetWallet
            )

        fun fromDb(localWallet: LocalWallet) =
            LocalWalletModel(
                name = localWallet.name,
                type = localWallet.type,
                testNetWallet = localWallet.testNetWallet
            )
    }

    abstract fun walletDao(): LocalWalletDao
}