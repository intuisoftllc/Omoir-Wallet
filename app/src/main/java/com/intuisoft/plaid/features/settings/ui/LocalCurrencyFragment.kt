package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.ConfigurableFragment
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.databinding.FragmentLocalCurrencyBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class LocalCurrencyFragment : ConfigurableFragment<FragmentLocalCurrencyBinding>(pinProtection = true) {
    private val viewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLocalCurrencyBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        viewModel.updateLocalCurrencySetting()

        binding.usd.setTitleText(SimpleCurrencyFormat.formatTypeBasic(Constants.LocalCurrency.USD))
        binding.cad.setTitleText(SimpleCurrencyFormat.formatTypeBasic(Constants.LocalCurrency.CANADA))
        binding.euro.setTitleText(SimpleCurrencyFormat.formatTypeBasic(Constants.LocalCurrency.EURO))


        viewModel.localCurrencySetting.observe(viewLifecycleOwner, Observer {
            binding.usd.checkRadio(it == Constants.LocalCurrency.USD)
            binding.cad.checkRadio(it == Constants.LocalCurrency.CANADA)
            binding.euro.checkRadio(it == Constants.LocalCurrency.EURO)
        })

        binding.usd.onRadioClicked { settingsItemView, checked ->
            if(checked) {
                viewModel.saveLocalCurrency(Constants.LocalCurrency.USD)
            }
        }

        binding.cad.onRadioClicked { settingsItemView, checked ->
            if(checked) {
                viewModel.saveLocalCurrency(Constants.LocalCurrency.CANADA)
            }
        }

        binding.euro.onRadioClicked { settingsItemView, checked ->
            if(checked) {
                viewModel.saveLocalCurrency(Constants.LocalCurrency.EURO)
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
        return R.string.appearance_fragment_label
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun navigationId(): Int {
        return R.id.appearanceFragment
    }
}