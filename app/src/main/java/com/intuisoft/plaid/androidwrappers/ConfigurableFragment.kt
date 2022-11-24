package com.intuisoft.plaid.androidwrappers

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.intuisoft.plaid.R
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants


abstract class ConfigurableFragment<T: ViewBinding> : BindingFragment<T>() {
    protected var baseVM: BaseViewModel? = null
    private var configTypes = listOf<FragmentConfigurationType>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onConfiguration(baseVM?.currentConfig)
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

        if(arguments != null && requireArguments().getString(Constants.Navigation.WALLET_UUID_BUNDLE_ID) != null) {
            walletVM.checkOpenedWallet(this)
        }
    }

    fun configSet(): Boolean {
        return baseVM!!.currentConfig != null
    }

    fun onNavigateBottomBarSecondaryFragmentBackwards(localStoreRepository: LocalStoreRepository) {
        if(localStoreRepository.isProEnabled())
            findNavController().popBackStack(R.id.walletDashboardFragment, false) // todo: change to pro dashboard
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