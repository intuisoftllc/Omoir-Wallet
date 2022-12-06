package com.intuisoft.plaid.common.util

import androidx.navigation.navOptions
import com.intuisoft.plaid.common.R

class Constants {

    object Database {
        const val DB_NAME = "plaid.db"
        const val DB_VERSION = 22
    }

    object Files {

    }

    object Navigation {
        const val WALLET_UUID_BUNDLE_ID = "wallet_id"
        const val FRAGMENT_CONFIG = "fragment_config"
        const val FROM_SETTINGS = "from_settings"
        const val HOME_PASS_THROUGH = "hom_pass_through"
        const val PIN_SETUP = "pin_setup"

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
        const val USE_BIOMETRIC_REASON_6 = "Scan your fingerprint to send transaction."
        const val SKIP_FOR_NOW = "Skip for now"
        const val USE_PIN = "Use pin"
        const val COPIED_TO_CLIPBOARD = "Copied To Clipboard!"
        const val BLOCKCHAIN_COM_TX_URL = "https://www.blockchain.com/btc/tx/"
        const val BLOCK_CYPHER_TX_URL = "https://live.blockcypher.com/btc-testnet/tx/"
        const val TEST_WALLET_1 = "yard impulse luxury drive today throw farm pepper survey wreck glass federal"
        const val TEST_WALLET_2 = "wrong cousin spell stadium snake enact author piano venue outer question chair"
        const val TEST_WALLET_3 = "patient sort can island cute saddle shield crunch knock tourist butter budget"
        const val BASE_WALLET = "BASE_WALLET_ID"
        const val BTC_TICKER = "btc"
    }

    object ServerStrings {
        const val TRANSACTION_DETAILS = "txs"
        const val TOKEN_BALANCES = "tokenBalances"
        const val TOKENS = "tokens"
    }

    object Time {
        const val MILLS_PER_SEC = 1000
        const val ONE_MINUTE = 60
        const val SECONDS_PER_DAY = 86400
        const val TWO_MINUTES = 2 * ONE_MINUTE
        const val FIVE_MINUTES = 5 * ONE_MINUTE
        const val TEN_MINUTES = 10 * ONE_MINUTE
        const val MIN_SYNC_TIME = ONE_MINUTE * MILLS_PER_SEC
        const val INSTANT = -1
        const val INSTANT_TIME_OFFSET = ((ONE_MINUTE * 60) * 24) * 10000
        const val SYNC_TIMEOUT = MILLS_PER_SEC * 10
        const val ITEM_COPY_DELAY = 600
        const val ITEM_COPY_DELAY_LONG = 1000
        const val AUTO_SYNC_TIME: Long = (MILLS_PER_SEC * TWO_MINUTES).toLong()
        const val GENERAL_CACHE_UPDATE_TIME: Long = (MILLS_PER_SEC * TEN_MINUTES).toLong()
        const val GENERAL_CACHE_UPDATE_TIME_MED: Long = (MILLS_PER_SEC * FIVE_MINUTES).toLong()
        const val GENERAL_CACHE_UPDATE_TIME_SHORT: Long = (MILLS_PER_SEC * TWO_MINUTES).toLong()
        const val GENERAL_CACHE_UPDATE_TIME_XTRA_SHORT: Long = (MILLS_PER_SEC * ONE_MINUTE).toLong()
    }

    object ActivityResult {
        const val BARCODE_EXTRA = "barcode_result"
    }

    object Limit {
        const val MAX_ALIAS_LENGTH = 25
        const val DEFAULT_MAX_PIN_ATTEMPTS = 15
        const val MIN_RECOMMENDED_PIN_ATTEMPTS = 4
        const val VERSION_CODE_TAPPED_LIMIT = 4
        const val SATS_PER_BTC = 100000000
        const val MAX_PEERS = 10
        const val PAGE_LIMIT = 50
        const val MIN_CONFIRMATIONS = 3
        const val FREE_MAX_WALLETS = 4
        const val PRO_MAX_WALLETS = 10
        const val SYNC_RESTART_LIMIT = 6
        const val ADDRESS_HINT_LENGTH = 16
        const val BITCOIN_SUPPLY_CAP = 21000000
        const val DEFAULT_PIN_TIMEOUT =
            Time.INSTANT_TIME_OFFSET // check for pin only when user leaves the app by default
    }

    object LocalCurrency {
        const val USD = "USD"
        const val EURO = "EUR"
        const val CANADA = "CAD"
    }

    object CongestionRating {
        val LIGHT = (0..3500)
        val NORMAL = (3501..10000)
        val MED = (10001..15000)
        val BUSY = (15001..20000)
    }
}
