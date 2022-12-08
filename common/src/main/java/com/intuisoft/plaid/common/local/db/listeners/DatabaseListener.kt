package com.intuisoft.plaid.common.local.db.listeners

interface DatabaseListener {
    fun onDatabaseUpdated(dao: Any?)
}