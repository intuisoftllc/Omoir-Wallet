package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.intuisoft.plaid.R
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateSettingsScreen()
        viewModel.bitcoinDisplayUnitSetting.observe(viewLifecycleOwner, Observer {
            binding.btcUnit.showCheck(it == BitcoinDisplayUnit.BTC)
            binding.satUnit.showCheck(it == BitcoinDisplayUnit.SATS)
        })

        binding.btcUnit.setOnClickListener {
            viewModel.saveDisplayUnit(BitcoinDisplayUnit.BTC)
        }

        binding.satUnit.setOnClickListener {
            viewModel.saveDisplayUnit(BitcoinDisplayUnit.SATS)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showActionBar(): Boolean {
        return true
    }

    override fun actionBarTitle(): Int {
        return R.string.display_unit_fragment_label
    }

    override fun navigationId(): Int {
        return R.id.displayUnitFragment
    }
}