package com.intuisoft.plaid.androidwrappers

enum class FragmentConfigurationType(val value: Int) {
    CONFIGURATION_NONE(0),
    CONFIGURATION_BASIC_SEED_SCREEN(1),
    CONFIGURATION_DISPLAY_QR(2),
    CONFIGURATION_DISPLAY_SHAREABLE_QR(3),
    CONFIGURATION_TRANSACTION_DATA(4),
    CONFIGURATION_All_SET(5),
    CONFIGURATION_WITHDRAW(6),
    CONFIGURATION_WALLET_DATA(7),
    CONFIGURATION_INVOICE(8),
    CONFIGURATION_SWAP_DATA(9);
}