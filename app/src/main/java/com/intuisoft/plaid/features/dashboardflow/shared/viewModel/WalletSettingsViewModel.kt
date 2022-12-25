
import android.app.Application
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.SavedAccountModel
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

    fun setHiddenWalletParams(passphrase: String, account: SavedAccountModel, restartRequired: () -> Unit) {
        val currentHiddenWallet = walletManager.getCurrentHiddenWallet(getWallet()!!)
        if(currentHiddenWallet == null || (currentHiddenWallet.passphrase != passphrase || currentHiddenWallet.account.account != account.account)) {
            restartRequired()
            setHiddenWalletParams(passphrase, account)
        } else {
            setHiddenWalletParams(passphrase, account)
        }
    }

    fun canCreatePassphrase(passphrase: String, account: SavedAccountModel) : Boolean {
        val hiddenWallets = walletManager.getHiddenWalletCount(localWallet!!)
        val requiresNewWallet = walletManager.requiresNewHiddenWallet(localWallet!!, passphrase, account)

        if(localStoreRepository.isProEnabled()) return true
        else {
            return (hiddenWallets < Constants.Limit.FREE_MAX_HIDDEN_WALLETS) ||
                    (hiddenWallets >= Constants.Limit.FREE_MAX_HIDDEN_WALLETS && !requiresNewWallet)
        }
    }

    fun showDerivationPathChangeWarning() = localStoreRepository.showDerivationPathChangeWarning()
    fun hideDerivationPathChangeWarning() = localStoreRepository.setShowDerivationPathChangeWarning(false)

    fun updateWalletSettings() {
        showWalletName()
        showWalletBip()
        showWalletNetwork()
    }
}