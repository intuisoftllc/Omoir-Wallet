package com.intuisoft.plaid.common.local

import com.google.gson.Gson
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.coroutines.PlaidScope
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.util.AESUtils
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Limit.MIN_CONFIRMATIONS
import com.intuisoft.plaid.common.util.extensions.readFromFile
import com.intuisoft.plaid.common.util.extensions.writeToPrivateFile
import kotlinx.coroutines.*
import java.io.File

class UserData {
    var minConfirmations: Int = MIN_CONFIRMATIONS
        set(value) {
            field = value
            save()
        }

    var bitcoinDisplayUnit: BitcoinDisplayUnit = BitcoinDisplayUnit.SATS
        set(value) {
            field = value
            save()
        }

    var baseWalletSeed: String? = null
        set(value) {
            field = value
            save()
        }

    var savedAddressInfo: SavedAddressInfo = SavedAddressInfo(mutableListOf())
        set(value) {
            field = value
            save()
        }

    var savedAccountInfo: SavedAccountInfo =
        SavedAccountInfo(
            mutableListOf( // set initial pre-loaded account options
                SavedAccountModel(
                    accountName = "Default",
                    account = 0,
                    canDelete = false
                ),
                SavedAccountModel(
                    accountName = "Passport H.W. - Post Mix",
                    account = 2147483646,
                    canDelete = true
                ),
                SavedAccountModel(
                    accountName = "Samourai - Pre Mix",
                    account = 2147483645,
                    canDelete = true
                ),
                SavedAccountModel(
                    accountName = "Samourai - Post Mix",
                    account = 2147483646,
                    canDelete = true
                ),
                SavedAccountModel(
                    accountName = "Samourai - Bad Bank",
                    account = 2147483644,
                    canDelete = true
                ),
                SavedAccountModel(
                    accountName = "Samourai - Ricochet",
                    account = 2147483647,
                    canDelete = true
                ),
                SavedAccountModel(
                    accountName = "Sparrow Wallet - Pre Mix",
                    account = 2147483645,
                    canDelete = true
                ),
                SavedAccountModel(
                    accountName = "Sparrow Wallet - Post Mix",
                    account = 2147483646,
                    canDelete = true
                ),
                SavedAccountModel(
                    accountName = "Sparrow Wallet - Bad Bank",
                    account = 2147483644,
                    canDelete = true
                )
            )
        )
        set(value) {
            field = value
            save()
        }

    var lastCheckPin: Long = 0
        set(value) {
            field = value
            save()
        }

    var defaultFeeType: FeeType = FeeType.MED
        set(value) {
            field = value
            save()
        }

    var pinTimeout: Int = Constants.Limit.DEFAULT_PIN_TIMEOUT
        set(value) {
            field = value
            save()
        }

    var lastFeeRateUpdateTime: Long = 0
        set(value) {
            field = value
            save()
        }

    var lastCurrencyRateUpdateTime: Long = 0
        set(value) {
            field = value
            save()
        }

    var lastSupportedCurrenciesUpdateTime: Long = 0
        set(value) {
            field = value
            save()
        }

    var lastBaseMarketDataUpdateTime: Long = 0
        set(value) {
            field = value
            save()
        }

    var lastExtendedMarketDataUpdateTime: Long = 0
        set(value) {
            field = value
            save()
        }

    var versionTappedCount: Int = 0
        set(value) {
            field = value
            save()
        }

    var localCurrency: String = Constants.LocalCurrency.USD
        set(value) {
            field = value
            save()
        }

    var storedWalletInfo: StoredWalletInfo = StoredWalletInfo(mutableListOf())
        set(value) {
            field = value
            save()
        }

    var isProEnabled: Boolean = false
        set(value) {
            field = value
            save()
        }

    var exchangeSendBTC: Boolean = true
        set(value) {
            field = value
            save()
        }

    var lastExchangeCurrency: String = "eth"
        set(value) {
            field = value
            save()
        }

    var batchGap: Int = 0
        set(value) {
            field = value
            save()
        }

    var batchSize: Int = 1
        set(value) {
            field = value
            save()
        }

    var feeSpreadLow: Int = 1
        set(value) {
            field = value
            save()
        }

    var feeSpreadHigh: Int = 1
        set(value) {
            field = value
            save()
        }

    var dynamicBatchNetworkFee: Boolean = false
        set(value) {
            field = value
            save()
        }

    fun save() {
        PlaidScope.IoScope.launch {
            synchronized(this@UserData::class.java) {
                val json = Gson().toJson(this@UserData, UserData::class.java)
                AESUtils.encrypt(json, CommonService.getUserPin(), CommonService.getWalletSecret())?.let { usrData ->
                    val dir: File = CommonService.getApplication().filesDir
                    File(dir, FILE_NAME).writeToPrivateFile(
                        usrData,
                        CommonService.getApplication()
                    )
                }
            }
        }
    }

    companion object {
        private const val FILE_NAME = "plaid_wallet_usr_data"
        private const val SECONDARY_FILE_NAME = "plaid_wallet_data" // possible use for a "plausable-deniability" feature in the future

        fun load(pin: String): UserData? {
            synchronized(this::class.java) {
                try {
                    val dir: File = CommonService.getApplication().filesDir
                    val data = File(dir, FILE_NAME).readFromFile(CommonService.getApplication())

                    if (data != null && data.isNotEmpty()) {
                        val json = AESUtils.decrypt(data.trim(), pin, CommonService.getWalletSecret())

                        if (json != null) {
                            return Gson().fromJson(json, UserData::class.java)
                        } else {
                            return null
                        }
                    } else {
                        return UserData()
                    }
                } catch (t: Throwable) {
                    return null
                }
            }
        }

        fun wipeData() {
            synchronized(this::class.java) {
                val dir: File = CommonService.getApplication().filesDir
                val file = File(dir, FILE_NAME)
                file.delete()
            }
        }
    }
}
