package com.intuisoft.plaid.features.createwallet.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.emojiigame.framework.db.LocalWalletDao
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.local.UserPreferences
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.model.WalletType
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.AesEncryptor
import com.intuisoft.plaid.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit


class CreateWalletViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val aesEncryptor: AesEncryptor
): BaseViewModel(application, localStoreRepository, aesEncryptor) {

//    private var wallet: Wallet? = null
    private var walletType: WalletType? = null

    private val _walletAlreadyExists = SingleLiveData<Boolean>()
    val walletAlreadyExists: LiveData<Boolean> = _walletAlreadyExists

    private val _seedPhrase = SingleLiveData<List<String>>()
    val seedPhrase: LiveData<List<String>> = _seedPhrase

    var useTestNet = false
        private set

    fun generateNewWallet() {
        viewModelScope.launch {
            walletType = WalletType.READ_WRITE

            // Since we are not using Bitcoinj for network control we set it to the maintain regardless
//            wallet = Wallet.createDeterministic(org.bitcoinj.core.Context(MainNetParams.get()), ScriptType.P2WPKH)
//            _seedPhrase.postValue(wallet!!.getKeyChainSeed().mnemonicCode)
        }
    }

    fun setUseTestNet(use: Boolean) {
        useTestNet = use
    }

    /**
     * Save wallet to dick every 1 minute
     *
     */
    fun commitWalletToDisk(context: Context, walletName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (doesWalletExist(walletName)) {
                    _walletAlreadyExists.postValue(true)
                } else {
                    _walletAlreadyExists.postValue(false)

//                    wallet?.let {
//                        val file = File(
//                            context.filesDir,
//                            Constants.Strings.USER_WALLET_FILENAME_PREFIX + walletName
//                        )

//                        it.autosaveToFile(
//                            file,
//                            Constants.Limit.MIN_WALLET_UPDATE_TIME,
//                            TimeUnit.MINUTES,
//                            null
//                        )
//                        it.encrypt(getWalletPassword())
//
//                        localStoreRepository.createWallet(walletName, walletType!!, useTestNet)
//                    }
                }
            }
        }
    }
}