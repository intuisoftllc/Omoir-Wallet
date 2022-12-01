package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.FragmentConfigurationType
import com.intuisoft.plaid.androidwrappers.navigate
import com.intuisoft.plaid.databinding.FragmentSeedPhraseBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.ConfigSeedData
import com.intuisoft.plaid.util.fragmentconfig.WalletConfigurationData
import org.koin.androidx.viewmodel.ext.android.viewModel


class SeedPhraseFragment : PinProtectedFragment<FragmentSeedPhraseBinding>() {
    protected val viewModel: CreateWalletViewModel by viewModel()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSeedPhraseBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_BASIC_SEED_SCREEN,
                FragmentConfigurationType.CONFIGURATION_WALLET_DATA
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.seedPhrase.resetView()

        when(configuration?.configurationType ?: FragmentConfigurationType.CONFIGURATION_NONE) {
            FragmentConfigurationType.CONFIGURATION_BASIC_SEED_SCREEN -> {
                binding.continueButton.isVisible = false

                val config = viewModel.currentConfig!!.configData as ConfigSeedData
                viewModel.setLocalSeedPhrase(config.seedPhrase)
                viewModel.showLocalSeedPhrase()
            }
            else  -> {
                viewModel.setConfiguration(configuration!!.configData as WalletConfigurationData)
                viewModel.generateNewWallet()
            }
        }

        viewModel.seedPhraseGenerated.observe(viewLifecycleOwner, Observer {
            it.forEach {  word ->
                binding.seedPhrase.nextWord(word)
            }

            viewModel.setLocalSeedPhrase(it)
        })

        binding.continueButton.onClick {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    configurationType = FragmentConfigurationType.CONFIGURATION_WALLET_DATA,
                    configData = viewModel.getConfiguration()
                )
            )

            navigate(
                R.id.nameWalletFragment,
                bundle,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }
    }

    override fun actionBarTitle(): Int {
        if(configSet())
            return super.actionBarTitle()

        return 0
    }

    override fun navigationId(): Int {
        return R.id.seedPhraseFragment
    }

    override fun onActionLeft() {
        if(baseVM?.currentConfig?.configurationType ==
            FragmentConfigurationType.CONFIGURATION_BASIC_SEED_SCREEN) {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}