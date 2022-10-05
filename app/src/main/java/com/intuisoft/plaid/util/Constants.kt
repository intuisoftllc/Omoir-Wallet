package com.intuisoft.plaid.util

import androidx.navigation.navOptions
import com.intuisoft.plaid.R

class Constants {

    object Database {
        const val DB_NAME = "plaid.db"
        const val DB_VERSION = 0
    }

    object Navigation {
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
        const val STRING_IMMEDIATE_TIMEOUT = "Immediately, once I leave the app"
        const val STRING_1_MINUTE_TIMEOUT = "1 Minute"
        const val STRING_2_MINUTE_TIMEOUT = "2 Minutes"
        const val STRING_5_MINUTE_TIMEOUT = "5 Minutes"
        const val STRING_10_MINUTE_TIMEOUT = "10 Minutes"
        const val USE_BIOMETRIC_AUTH = "Use Biometric Authentication"
        const val DISABLE_BIOMETRIC_AUTH = "Disable Biometric Authentication"
        const val SCAN_TO_ERASE_DATA = "Scan To Erase Data"
        const val USE_BIOMETRIC_REASON_1 = "Use biometrics to add an additional layer of security to your wallet. You will be asked to scan your fingerprint when signing transactions or interacting with sensitive app data."
        const val USE_BIOMETRIC_REASON_2 = "Unlock Plaid Crypto Wallet™ using biometrics."
        const val USE_BIOMETRIC_REASON_3 = "Scan your fingerprint to reset app data."
        const val USE_BIOMETRIC_REASON_4 = "Scan your fingerprint to remove biometric authentication."
        const val SKIP_FOR_NOW = "Skip for now"
        const val USE_PIN = "Use pin"
    }

    object Time {
        const val ONE_MINUTE = 60
        const val TWO_MINUTES = 2 * ONE_MINUTE
        const val FIVE_MINUTES = 5 * ONE_MINUTE
        const val TEN_MINUTES = 10 * ONE_MINUTE
        const val INSTANT = -1
        const val INSTANT_TIME_OFFSET = ((ONE_MINUTE * 60) * 24) * 10000
    }

    object Limit {
        const val MAX_ONBOARDING_STEPS = 3
        const val MAX_ALIAS_LENGTH = 25
        const val MAX_PIN_ATTEMPTS = 10
        const val MIN_RECOMMENDED_PIN_ATTEMPTS = 4
        const val DEFAULT_PIN_TIMEOUT = 5 * 60 // check for pin every 5 minutes
    }
}
