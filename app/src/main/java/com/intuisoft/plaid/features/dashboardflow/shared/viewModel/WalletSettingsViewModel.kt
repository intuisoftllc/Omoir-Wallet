
import android.app.Application
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.walletmanager.AbstractWalletManager


class WalletSettingsViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    var fromSettings: Boolean = false

    fun setPassphrase(passphrase: String) {
        setWalletPassphrase(passphrase)
    }

    fun canCreatePassphrase(passphrase: String) : Boolean {
        val hiddenWallets = walletManager.getHiddenWalletCount(localWallet!!)
        val requiresNewWallet = walletManager.requiresNewHiddenWallet(localWallet!!, passphrase)

        if(localStoreRepository.isProEnabled()) return true
        else {
            return (hiddenWallets < Constants.Limit.FREE_MAX_HIDDEN_WALLETS) ||
                    (hiddenWallets >= Constants.Limit.FREE_MAX_HIDDEN_WALLETS && !requiresNewWallet)
        }
    }

    fun updateWalletSettings() {
        showWalletName()
        showWalletBip()
        showWalletNetwork()
    }
}