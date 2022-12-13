
import android.app.Application
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
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

    fun updateWalletSettings() {
        showWalletName()
        showWalletBip()
        showWalletNetwork()
    }
}