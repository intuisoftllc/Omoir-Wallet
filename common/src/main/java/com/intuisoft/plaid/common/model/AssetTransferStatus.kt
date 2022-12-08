package com.intuisoft.plaid.common.model

enum class AssetTransferStatus(val id: Int) {
    NOT_STARTED(0),
    IN_PROGRESS(1),
    PARTIALLY_COMPLETED(2),
    COMPLETED(3),
    FAILED(4),
    CANCELLED(5)
}