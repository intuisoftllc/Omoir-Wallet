package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.databinding.FragmentSettingsBinding
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.util.Constants
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SettingsFragment : PinProtectedFragment<FragmentSettingsBinding>() {
    private val viewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateSettingsScreen()

        viewModel.bitcoinDisplayUnitSetting.observe(viewLifecycleOwner, Observer {
            when(it) {
                BitcoinDisplayUnit.BTC -> {
                    binding.bitcoinUnitSetting.showSubtitleIcon(R.drawable.ic_bitcoin)
                    binding.bitcoinUnitSetting.setSubTitleText(getString(R.string.bitcoin))
                }

                BitcoinDisplayUnit.SATS -> {
                    binding.bitcoinUnitSetting.showSubtitleIcon(R.drawable.ic_satoshi)
                    binding.bitcoinUnitSetting.setSubTitleText(getString(R.string.satoshi))
                }

                else -> {
                    binding.bitcoinUnitSetting.showSubtitleIcon(R.drawable.ic_bitcoin)
                    binding.bitcoinUnitSetting.setSubTitleText(getString(R.string.bitcoin))
                }
            }
        })

        binding.bitcoinUnitSetting.onClick {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToDisplayUnitFragment(),
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateSettingsScreen()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showActionBar(): Boolean {
        return true
    }

    override fun actionBarTitle(): Int {
        return R.string.settings_fragment_label
    }
}