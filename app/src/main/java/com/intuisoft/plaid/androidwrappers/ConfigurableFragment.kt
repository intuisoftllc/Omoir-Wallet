package com.intuisoft.plaid.androidwrappers

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.features.pin.viewmodel.PinViewModel
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.util.errors.ClosedWalletErr
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


abstract class ConfigurableFragment<T: ViewBinding>(
    private val pinProtection: Boolean = false,
    private val secureScreen: Boolean = false,
    private val requiresWallet: Boolean = true
) : BindingFragment<T>() {
    protected var baseVM: BaseViewModel? = null
    private var configTypes = listOf<FragmentConfigurationType>()
    protected val pinViewModel: PinViewModel by sharedViewModel()

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
                super.onViewCreated(view, savedInstanceState)
                onConfiguration(baseVM?.currentConfig)
            }
        } else {
            super.onViewCreated(view, savedInstanceState)
            onConfiguration(baseVM?.currentConfig)
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
                baseVM!!.softRestart(this)
                return null
            }
        } else {
            throw IllegalStateException("requireWallet(): base vm != WalletViewModel")
        }
    }

    private fun checkPin() {
        if(pinProtection) {
            pinViewModel.checkPinStatus {
                (activity as? MainActivity)?.apply {
                    val fragment =
                        supportFragmentManager.currentNavigationFragment as? FragmentBottomBarBarDelegate

                    if(fragment?.navigationId() != R.id.pinFragment) { // do not request pin twice
                        clearDialogStack()
                        navigate(
                            R.id.pinFragment,
                            Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION
                        )
                    }
                }
            }
        }
    }

    fun configSet(): Boolean {
        return baseVM!!.currentConfig != null
    }

    fun onNavigateBottomBarSecondaryFragmentBackwards(localStoreRepository: LocalStoreRepository) {
        if(localStoreRepository.isProEnabled())
            findNavController().popBackStack(R.id.walletProDashboardFragment, false)
        else
            findNavController().popBackStack(R.id.walletDashboardFragment, false)
    }

    fun onNavigateBottomBarPrimaryFragmentBackwards(localStoreRepository: LocalStoreRepository) {
        if(localStoreRepository.isProEnabled())
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