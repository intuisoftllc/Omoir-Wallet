package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.databinding.FragmentBitcoinUnitBinding
import com.intuisoft.plaid.databinding.FragmentSettingsBinding
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class BitcoinUnitFragment : PinProtectedFragment<FragmentBitcoinUnitBinding>() {
    private val viewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentBitcoinUnitBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        viewModel.updateSettingsScreen()
        viewModel.bitcoinDisplayUnitSetting.observe(viewLifecycleOwner, Observer {
            binding.btcUnit1.checkRadio(it == BitcoinDisplayUnit.BTC)
            binding.btcUnit2.checkRadio(it == BitcoinDisplayUnit.SATS)
        })

        binding.btcUnit1.onRadioClicked { view, checked ->

            if(checked || viewModel.getDisplayUnit() == BitcoinDisplayUnit.BTC) {
                viewModel.saveDisplayUnit(BitcoinDisplayUnit.BTC)
            }
        }

        binding.btcUnit2.onRadioClicked { view, checked ->

            if(checked || viewModel.getDisplayUnit() == BitcoinDisplayUnit.SATS) {
                viewModel.saveDisplayUnit(BitcoinDisplayUnit.SATS)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN
    }

    override fun actionBarTitle(): Int {
        return R.string.display_unit_fragment_label
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun navigationId(): Int {
        return R.id.displayUnitFragment
    }
}