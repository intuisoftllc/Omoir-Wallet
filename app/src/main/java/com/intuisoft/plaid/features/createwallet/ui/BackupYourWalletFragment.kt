package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.ConfigurableFragment
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.FragmentConfigurationType
import com.intuisoft.plaid.androidwrappers.navigate
import com.intuisoft.plaid.databinding.FragmentBackupBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.WalletConfigurationData
import org.koin.androidx.viewmodel.ext.android.viewModel


class BackupYourWalletFragment : ConfigurableFragment<FragmentBackupBinding>(pinProtection = true) {
    protected val viewModel: CreateWalletViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_WALLET_DATA
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        viewModel.setConfiguration(configuration!!.configData as WalletConfigurationData)

        binding.seedPhraseBackupSubtitle.setText(getString(R.string.backup_seed_phrase_description, viewModel.entropyStrengthToString(requireContext())))

        binding.continueButton.enableButton(false)
        binding.backupAcknowledgement1.setOnCheckedChangeListener { compoundButton, isChecked ->
            binding.continueButton.enableButton(allAcknowledgementsChecked())
        }

        binding.backupAcknowledgement2.setOnCheckedChangeListener { compoundButton, isChecked ->
            binding.continueButton.enableButton(allAcknowledgementsChecked())
        }

        binding.backupAcknowledgement3.setOnCheckedChangeListener { compoundButton, isChecked ->
            binding.continueButton.enableButton(allAcknowledgementsChecked())
        }

        binding.continueButton.onClick {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    configurationType = FragmentConfigurationType.CONFIGURATION_WALLET_DATA,
                    configData = viewModel.getConfiguration()
                )
            )

            navigate(
                R.id.seedPhraseFragment,
                bundle,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }
    }

    fun allAcknowledgementsChecked() : Boolean {
        return binding.backupAcknowledgement1.isChecked && binding.backupAcknowledgement2.isChecked
                && binding.backupAcknowledgement3.isChecked
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.backupWalletFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}