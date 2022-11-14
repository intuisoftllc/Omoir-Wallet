package com.intuisoft.plaid.common.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.NetworkFeeRate
import com.intuisoft.plaid.common.util.Constants

@Database(entities = arrayOf(SuggestedFeeRate::class), version = Constants.Database.DB_VERSION)
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
    }

    abstract fun suggestedFeeRateDao(): SuggestedFeeRateDao
}
