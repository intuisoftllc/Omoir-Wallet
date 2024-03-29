package com.intuisoft.plaid.androidwrappers

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.local.UserData
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.util.errors.ClosedWalletErr
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject


abstract class ConfigurableFragment<T: ViewBinding>(
    private val pinProtection: Boolean = false,
    private val secureScreen: Boolean = false,
    private var requiresWallet: Boolean = true,
    private var requiresUsrData: Boolean = true,
    private var premiumContent: Boolean = false
) : BindingFragment<T>() {
    protected var baseVM: BaseViewModel? = null
    private var configTypes = listOf<FragmentConfigurationType>()
    private val _manager: AbstractWalletManager by inject()
    private val _localStore: LocalStoreRepository by inject()
    private val _billing: BillingManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(secureScreen) {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            requireActivity().window.clearFlags(
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        if(requiresWallet) {
            requireWallet()?.let {
                if(requiresUsrData) {
                    requireUsrData()?.let {
                        super.onViewCreated(view, savedInstanceState)
                        onConfiguration(baseVM?.currentConfig)
                    }
                } else {
                    super.onViewCreated(view, savedInstanceState)
                    onConfiguration(baseVM?.currentConfig)
                }
            }
        } else {
            if(requiresUsrData) {
                requireUsrData()?.let {
                    super.onViewCreated(view, savedInstanceState)
                    onConfiguration(baseVM?.currentConfig)
                }
            }else {
                super.onViewCreated(view, savedInstanceState)
                onConfiguration(baseVM?.currentConfig)
            }
        }

        if(premiumContent) {
            _billing.shouldShowPremiumContent { show ->
                if(!show) {
                    Toast.makeText(requireContext(), getString(R.string.premium_subscription_switch_to_free), Toast.LENGTH_LONG).show()
                    softRestart(_manager, _localStore)
                }
            }
        }
    }

    abstract fun onConfiguration(configuration: FragmentConfiguration?)

    /**
     * Call in onCreateView()
     */
    fun setupConfiguration(baseVM: BaseViewModel, configTypes: List<FragmentConfigurationType> = listOf()) {
        this.baseVM = baseVM
        this.configTypes = configTypes

        if(arguments != null && requireArguments().getParcelable<FragmentConfiguration>(Constants.Navigation.FRAGMENT_CONFIG) != null) {
            baseVM.currentConfig = requireArguments().getParcelable(Constants.Navigation.FRAGMENT_CONFIG)
        } else {
            baseVM.currentConfig = null
        }
    }

    /**
     * Call in onCreateView()
     */
    fun setupConfiguration(walletVM: WalletViewModel, configTypes: List<FragmentConfigurationType> = listOf()) {
        this.baseVM = walletVM
        this.configTypes = configTypes

        if(arguments != null && requireArguments().getParcelable<FragmentConfiguration>(Constants.Navigation.FRAGMENT_CONFIG) != null) {
            baseVM!!.currentConfig = requireArguments().getParcelable(Constants.Navigation.FRAGMENT_CONFIG)

            baseVM!!.currentConfig?.let {
                if(configTypes.find { it == baseVM!!.currentConfig!!.configurationType } == null) {
                    throw UnsupportedOperationException("Unsupported configuration type: ${baseVM!!.currentConfig!!.configurationType}")
                }
            }
        } else {
            baseVM!!.currentConfig = null
        }
    }

    override fun onResume() {
        super.onResume()
        checkPin()
    }

    private fun requireWallet(): LocalWalletModel? {
        if(baseVM is WalletViewModel) {
            try {
                return (baseVM!! as WalletViewModel).getWallet()
            } catch(err: ClosedWalletErr) {
                softRestart(_manager, _localStore)
                return null
            }
        } else {
            throw IllegalStateException("requireWallet(): base vm != WalletViewModel")
        }
    }

    fun requireUsrData(): UserData? {
        if(CommonService.getUserData() != null) {
            return CommonService.getUserData()
        } else {
            softRestart(_manager, _localStore)
            return null
        }
    }

    fun softRestart() {
        softRestart(_manager, _localStore)
    }

    private fun checkPin() {
        if(pinProtection) {
            requireUsrData()?.let {
                (activity as? MainActivity)?.apply {
                    val time = System.currentTimeMillis() / Constants.Time.MILLS_PER_SEC

                    if (_localStore.getLastCheckedPinTime() == 0L
                        || (time - it.lastCheckPin) > it.pinTimeout) {
                        clearDialogStack()
                        activatePin(false, false)
                    } else {
                        hidePinIfNeeded()
                    }
                }
            }
        }
    }

    fun configSet(): Boolean {
        return baseVM!!.currentConfig != null
    }

    fun onNavigateBottomBarSecondaryFragmentBackwards() {
        if(findNavController().isFragmentInBackStack(R.id.walletProDashboardFragment))
            findNavController().popBackStack(R.id.walletProDashboardFragment, false)
        else
            findNavController().popBackStack(R.id.walletDashboardFragment, false)
    }

    fun onNavigateBottomBarPrimaryFragmentBackwards() {
        if(findNavController().isFragmentInBackStack(R.id.proHomescreenFragment))
            findNavController().popBackStack(R.id.proHomescreenFragment, false)
        else
            findNavController().popBackStack(R.id.homescreenFragment, false)
    }

    override fun actionBarSubtitle(): Int {
        configTypes.forEach {
            if(baseVM!!.hasConfiguration(it))
                return baseVM!!.currentConfig!!.actionBarSubtitle
        }

        return 0
    }

    override fun actionBarActionLeft(): Int {
        configTypes.forEach {
            if(baseVM!!.hasConfiguration(it))
                return baseVM!!.currentConfig!!.actionLeft
        }

        return 0
    }

    override fun actionBarActionRight(): Int {
        configTypes.forEach {
            if(baseVM!!.hasConfiguration(it))
                return baseVM!!.currentConfig!!.actionRight
        }

        return 0
    }

    override fun actionBarVariant(): Int {
        configTypes.forEach {
            if(baseVM!!.hasConfiguration(it))
                return baseVM!!.currentConfig!!.actionBarVariant
        }

        return TopBarView.NO_BAR
    }

    override fun actionBarTitle(): Int {
        configTypes.forEach {
            if(baseVM!!.hasConfiguration(it))
                return baseVM!!.currentConfig!!.actionBarTitle
        }

        return 0
    }

    override fun onNavigateTo(destination: Int) {
        // ignore
    }

    override fun onSubtitleClicked() {
        // do nothing
    }

    override fun onActionLeft() {
        // do nothing
    }

    override fun onActionRight() {
        // do nothing
    }

    override fun navigationId(): Int {
        return 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}