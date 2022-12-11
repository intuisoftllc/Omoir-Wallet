package com.intuisoft.plaid.common.model

enum class AssetTransferStatus(val id: Int) {
    NOT_STARTED(0),
    WAITING(1),
    IN_PROGRESS(2),
    PARTIALLY_COMPLETED(3),
    COMPLETED(4),
    FAILED(5),
    CANCELLED(6)
}