package com.intuisoft.plaid.common.model

data class SavedAccountModel(
    var accountName: String,
    var account: Int,
    var canDelete: Boolean
)
