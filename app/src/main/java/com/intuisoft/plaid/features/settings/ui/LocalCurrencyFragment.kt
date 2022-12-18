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
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.common.util.extensions.prepend
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.FragmentLocalCurrencyBinding
import com.intuisoft.plaid.features.homescreen.adapters.SupportedCryptoCurrenciesAdapter
import com.intuisoft.plaid.features.settings.adapters.SupportedCurrenciesAdapter
import kotlinx.android.synthetic.main.list_item_supported_currency.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class LocalCurrencyFragment : ConfigurableFragment<FragmentLocalCurrencyBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    private val viewModel: SettingsViewModel by sharedViewModel()
    private val localStoreRepository: LocalStoreRepository by inject()

    private val adapter = SupportedCurrenciesAdapter(
        currencySelected = ::onCurrencySelected
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLocalCurrencyBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.supportedCurrencies.adapter = adapter
        adapter.addCurrencies(
            items = listOf(
                Constants.LocalCurrency.CANADA,
                Constants.LocalCurrency.EURO,
                Constants.LocalCurrency.AED,
                Constants.LocalCurrency.ARS,
                Constants.LocalCurrency.AUD,
                Constants.LocalCurrency.BDT,
                Constants.LocalCurrency.BHD,
                Constants.LocalCurrency.CHF,
                Constants.LocalCurrency.CNY,
                Constants.LocalCurrency.CZK,
                Constants.LocalCurrency.GBP,
                Constants.LocalCurrency.KRW,
                Constants.LocalCurrency.RUB,
                Constants.LocalCurrency.PHP,
                Constants.LocalCurrency.PKR,
                Constants.LocalCurrency.CLP,
            )
                .sorted()
                .toMutableList()
                .prepend(Constants.LocalCurrency.USD)
                .toArrayList(),
            initialCurrency = localStoreRepository.getLocalCurrency()
        )
    }

    private fun onCurrencySelected(currencyCode: String) {
        viewModel.saveLocalCurrency(currencyCode)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN
    }

    override fun actionBarTitle(): Int {
        return R.string.local_currency_fragment_label
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun navigationId(): Int {
        return R.id.localCurrencyFragment
    }
}