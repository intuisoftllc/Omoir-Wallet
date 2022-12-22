package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventCreateWalletAdvancedOptions
import com.intuisoft.plaid.common.analytics.events.EventPublicKeyImport
import com.intuisoft.plaid.common.analytics.events.EventRecoveryPhraseImport
import com.intuisoft.plaid.databinding.FragmentImportWalletBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.WalletConfigurationData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class ImportWalletFragment : ConfigurableFragment<FragmentImportWalletBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    protected val viewModel: CreateWalletViewModel by viewModel()
    protected val eventTracker: EventTracker by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentImportWalletBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_WALLET_DATA
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        viewModel.setConfiguration(configuration!!.configData as WalletConfigurationData)

        binding.publicKeyImport.onClick {
            eventTracker.log(EventPublicKeyImport())
            navigate(
                R.id.publicKeyImportFragment,
                Constants.Navigation.ANIMATED_SLIDE_UP_OPTION
            )
        }

        binding.recoveryPhraseImport.onClick {
            eventTracker.log(EventRecoveryPhraseImport())
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    configurationType = FragmentConfigurationType.CONFIGURATION_WALLET_DATA,
                    configData = viewModel.getConfiguration()
                )
            )

            navigate(
                R.id.recoveryPhraseImportFragment,
                bundle,
                Constants.Navigation.ANIMATED_SLIDE_UP_OPTION
            )
        }
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.importWalletFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}