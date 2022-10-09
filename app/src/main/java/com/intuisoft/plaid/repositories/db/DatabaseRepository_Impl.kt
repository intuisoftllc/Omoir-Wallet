package com.intuisoft.plaid.repositories.db

import com.intuisoft.emojiigame.framework.db.LocalWallet
import com.intuisoft.emojiigame.framework.db.LocalWalletDao
import com.intuisoft.emojiigame.framework.db.PlaidDatabase
import com.intuisoft.plaid.local.db.DatabaseListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.model.WalletType

class DatabaseRepository_Impl(
    private val database: PlaidDatabase,
    private val localWalletDao: LocalWalletDao
) : DatabaseRepository {

    override suspend fun doesWalletExist(walletName: String): Boolean =
        localWalletDao.getWalletByName(walletName) != null

    override suspend fun createNewWallet(walletName: String, type: WalletType, testNetWallet: Boolean) {
        localWalletDao.insert(PlaidDatabase.toDb(LocalWalletModel(walletName, type, testNetWallet)))
        database.onUpdate()
    }

    override suspend fun getWallet(walletName: String): LocalWallet? =
        localWalletDao.getWalletByName(walletName)

    override suspend fun getAllWallets(): List<LocalWallet>? =
        localWalletDao.getAllWallets()

    override suspend fun deleteAllData() {
        localWalletDao.deleteTable()
        database.onUpdate()
    }

    override fun setDatabaseListener(databaseListener: DatabaseListener) {
        database.setDatabaseListener(databaseListener)
    }
}