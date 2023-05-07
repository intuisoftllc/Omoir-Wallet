package com.intuisoft.plaid.androidwrappers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.androidwrappers.delegates.FragmentBottomBarBarDelegate
import com.intuisoft.plaid.common.model.StoredHiddenWalletsModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.delegates.wallet.WalletDelegate
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class BindingActivity<T: ViewBinding> : AppCompatActivity() {

    protected var _binding: T? = null
    protected val binding get() = _binding!!

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun withBinding(delegate: T.() -> Unit) {
        _binding?.delegate()
    }

    fun softRestart(walletManager: WalletDelegate, localStoreRepository: LocalStoreRepository) {
        (this as FragmentBottomBarBarDelegate).apply {
            runBlocking {
                walletManager.stop()
                localStoreRepository.clearCache()

                MainScope().launch {
                    val intent = Intent(baseContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra(Constants.Navigation.PASSPHRASES, Gson().toJson(
                        StoredHiddenWalletsModel(walletManager.getHiddenWallets().entries.toList().map { it.key to it.value }), StoredHiddenWalletsModel::class.java))
                    startActivity(intent)
                }
            }
        }
    }
}
