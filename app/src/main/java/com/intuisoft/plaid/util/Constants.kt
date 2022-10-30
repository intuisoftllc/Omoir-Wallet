package com.intuisoft.plaid.util

import androidx.navigation.navOptions
import com.intuisoft.plaid.R

class Constants {

    object Database {
        const val DB_NAME = "plaid.db"
        const val DB_VERSION = 2
    }

    object Files {
        const val CHECKPOINTS_ASSET = "checkpoints.txt"
    }

    object Navigation {
        const val WALLET_UUID_BUNDLE_ID = "wallet_id"
        const val FRAGMENT_CONFIG = "fragment_config"

        val ANIMATED_SLIDE_UP_OPTION =
            navOptions {
                anim {
                    enter = R.anim.slide_up
                    popEnter = android.R.anim.slide_in_left
                }
            }

        val ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION =
            navOptions {
                anim {
                    enter = R.anim.slide_in_right
                    popEnter = android.R.anim.slide_in_left
                }
            }

        val ANIMATED_ENTER_EXIT_LEFT_NAV_OPTION =
            navOptions {
                anim {
                    enter = android.R.anim.slide_in_left
                    popEnter = R.anim.slide_in_right
                }
            }


        val ANIMATED_FADE_IN_NAV_OPTION =
            navOptions {
                anim {
                    enter = android.R.anim.fade_in
                }
            }

        val ANIMATED_FADE_IN_EXIT_NAV_OPTION =
            navOptions {
                anim {
                    enter = android.R.anim.fade_in
                    popEnter = android.R.anim.slide_in_left
                }
            }
    }

    object Strings {
        const val USE_BIOMETRIC_AUTH = "Use Biometric Authentication"
        const val DISABLE_BIOMETRIC_AUTH = "Disable Biometric Authentication"
        const val SCAN_TO_ERASE_DATA = "Scan To Erase Data"
        const val USE_BIOMETRIC_REASON_1 = "Use biometrics to add an additional layer of security to your wallet. You will be asked to scan your fingerprint when signing transactions or interacting with sensitive app data."
        const val USE_BIOMETRIC_REASON_2 = "Unlock Plaid Crypto Wallet™ using biometrics."
        const val USE_BIOMETRIC_REASON_3 = "Scan your fingerprint to reset app data."
        const val USE_BIOMETRIC_REASON_4 = "Scan your fingerprint to remove biometric authentication."
        const val USE_BIOMETRIC_REASON_5 = "Scan your fingerprint to delete your wallet."
        const val SKIP_FOR_NOW = "Skip for now"
        const val USE_PIN = "Use pin"
        const val SYNCING = "Syncing..."
        const val WALLET_FILE = "local_wallets"
        const val ENTROPY_STRENGTH_DEFAULT = "12 words"
        const val ENTROPY_STRENGTH_LOW = "15 wordss"
        const val ENTROPY_STRENGTH_MEDIUM = "18 wordss"
        const val ENTROPY_STRENGTH_HIGH = "21 wordss"
        const val ENTROPY_STRENGTH_VERY_HIGH = "24 wordss"
        const val BIP_TYPE_44 = "Bip 44 Wallet (p2pkh)"
        const val BIP_TYPE_49 = "Bip 49 Wallet (p2wpkhsh)"
        const val BIP_TYPE_84 = "Bip 84 Wallet (p2wpkh bech32)"
        const val TEST_NET_WALLET = "Test Net"
        const val MAIN_NET_WALLET = "Main Net"
        const val COPIED_TO_CLIPBOARD = "Copied To Clipboard!"
        const val TRANSACTION_INPUTS_TITLE = "Transaction Inputs"
        const val TRANSACTION_OUTPUTS_TITLE = "Transaction Outputs"
        const val BLOCKCHAIN_COM_TX_URL = "https://www.blockchain.com/btc/tx/"
        const val TEST_WALLET_1 = "yard impulse luxury drive today throw farm pepper survey wreck glass federal"
        const val TEST_WALLET_2 = "wrong cousin spell stadium snake enact author piano venue outer question chair"
        const val NO_INTERNET = "No Internet Connection!"
        const val BASE_WALLET = "BASE_WALLET"
    }

    object ServerStrings {
        const val TRANSACTION_DETAILS = "txs"
        const val TOKEN_BALANCES = "tokenBalances"
        const val TOKENS = "tokens"
    }

    object Time {
        const val ONE_MINUTE = 60
        const val TWO_MINUTES = 2 * ONE_MINUTE
        const val FIVE_MINUTES = 5 * ONE_MINUTE
        const val TEN_MINUTES = 10 * ONE_MINUTE
        const val INSTANT = -1
        const val INSTANT_TIME_OFFSET = ((ONE_MINUTE * 60) * 24) * 10000
    }

    object ActivityResult {
        const val BARCODE_RESULT = 120

        const val BARCODE_EXTRA = "barcode_result"
    }

    object Limit {
        const val MAX_ALIAS_LENGTH = 25
        const val DEFAULT_MAX_PIN_ATTEMPTS = 15
        const val MIN_RECOMMENDED_PIN_ATTEMPTS = 4
        const val VERSION_CODE_TAPPED_LIMIT = 4
        const val MIN_WALLET_UPDATE_TIME = 5L
        const val MAX_WALLET_UPDATE_LIMIT = 10L
        const val SATS_PER_BTC = 100000000
        const val MAX_PEERS = 10
        const val PAGE_LIMIT = 50
        const val MIN_CONFIRMATIONS = 1
        const val DEFAULT_PIN_TIMEOUT = Time.INSTANT_TIME_OFFSET // check for pin only when user leaves the app by default
    }
}
