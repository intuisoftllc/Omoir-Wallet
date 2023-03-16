
import android.app.Application
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.androidwrappers.SingleLiveData
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

    protected val _seedPhraseSettingEnabled = SingleLiveData<Boolean?>()
    val seedPhraseSettingEnabled: LiveData<Boolean?> = _seedPhraseSettingEnabled

    protected val _hiddenWalletEnabled = SingleLiveData<Boolean>()
    val hiddenWalletEnabled: LiveData<Boolean> = _hiddenWalletEnabled

    protected val _showPrivKeySetting = SingleLiveData<Boolean>()
    val showPrivKeySetting: LiveData<Boolean> = _showPrivKeySetting

    var fromSettings: Boolean = false

    fun showPrivKeySetting() {
        _showPrivKeySetting.postValue(isPrivKeyWallet())
    }

    fun enableHiddenWallet() {
        _hiddenWalletEnabled.postValue(!isPrivKeyWallet() && !isReadOnly())
    }
    fun enableSeedPhraseSetting() {
        if(isPrivKeyWallet())
            _seedPhraseSettingEnabled.postValue(null)
        else {
            _seedPhraseSettingEnabled.postValue(!isReadOnly())
        }
    }

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

        if(localStoreRepository.isPremiumUser()) return true
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