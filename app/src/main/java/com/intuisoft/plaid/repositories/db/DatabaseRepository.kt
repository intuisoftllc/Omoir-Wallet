package com.intuisoft.plaid.repositories.db

import com.intuisoft.emojiigame.framework.db.LocalWallet
import com.intuisoft.plaid.local.db.DatabaseListener
import com.intuisoft.plaid.model.WalletType

interface DatabaseRepository {

    suspend fun doesWalletExist(walletName: String) : Boolean

    suspend fun createNewWallet(walletName: String, type: WalletType, testNetWallet: Boolean)

    suspend fun getWallet(walletName: String) : LocalWallet?

    suspend fun getAllWallets() : List<LocalWallet>?

    suspend fun deleteAllData()

    fun setDatabaseListener(databaseListener: DatabaseListener)
}