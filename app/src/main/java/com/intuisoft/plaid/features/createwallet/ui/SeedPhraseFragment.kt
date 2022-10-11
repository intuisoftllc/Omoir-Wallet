package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.FragmentConfigurationType
import com.intuisoft.plaid.databinding.FragmentSeedPhraseBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.ConfigSeedData
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SeedPhraseFragment : PinProtectedFragment<FragmentSeedPhraseBinding>() {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSeedPhraseBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf(FragmentConfigurationType.CONFIGURATION_BASIC_SEED_SCREEN))
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.seedPhrase.resetView()

        when(configuration?.configurationType ?: FragmentConfigurationType.CONFIGURATION_NONE) {
            FragmentConfigurationType.CONFIGURATION_BASIC_SEED_SCREEN -> {
                binding.confirm.isVisible = false

                val config = viewModel.currentConfig!!.configData as ConfigSeedData
                viewModel.setLocalPassphrase(config.passphrase)
                viewModel.setLocalSeedPhrase(config.seedPhrase)

                viewModel.showLocalSeedPhrase()
                viewModel.showLocalPassPhrase()
            }

            else  -> {
                viewModel.generateNewWallet()
            }
        }

        viewModel.seedPhraseGenerated.observe(viewLifecycleOwner, Observer {
            it.forEach {  word ->
                binding.seedPhrase.nextWord(word)
            }

            viewModel.setLocalSeedPhrase(it)
        })

        binding.confirm.onClick {
            findNavController().navigate(
                SeedPhraseFragmentDirections.actionSeedPhraseFragmentToNameWalletFragment(),
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        viewModel.userPassphrase.observe(viewLifecycleOwner, Observer {
            binding.passphrase.isVisible = it.isNotEmpty()
            binding.passphraseTitle.isVisible = it.isNotEmpty()

            binding.passphrase.text = it
            viewModel.setLocalPassphrase(it)
        })
    }

    override fun showActionBar(): Boolean {
        if(configSet())
            return super.showActionBar()

        return false
    }

    override fun actionBarTitle(): Int {
        if(configSet())
            return super.actionBarTitle()

        return 0
    }

    override fun navigationId(): Int {
        return R.id.seedPhraseFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}